package com.ocean.piuda.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocean.piuda.global.api.exception.BusinessException;
import com.ocean.piuda.global.api.exception.ExceptionType;
import com.ocean.piuda.payment.dto.response.PaymentResponse;
import com.ocean.piuda.payment.entity.Order;
import com.ocean.piuda.payment.entity.Payment;
import com.ocean.piuda.payment.entity.PaymentHistory;
import com.ocean.piuda.payment.enums.OrderStatus;
import com.ocean.piuda.payment.enums.PaymentEvent;
import com.ocean.piuda.payment.enums.PaymentStatus;
import com.ocean.piuda.payment.repository.OrderRepository;
import com.ocean.piuda.payment.repository.PaymentHistoryRepository;
import com.ocean.piuda.payment.repository.PaymentRepository;
import io.portone.sdk.server.payment.PaymentClient;
import io.portone.sdk.server.webhook.Webhook;
import io.portone.sdk.server.webhook.WebhookVerifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.ocean.piuda.payment.enums.PaymentStatus.PAID;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;

    private final PaymentClient portoneClient;     // PortOne SDK
    private final WebhookVerifier portoneWebhook;  // SDK Webhook 검증기



    // PortOne SDK Bean 주입 (Configuration에서 생성)
    public PaymentResponse syncPayment(String paymentId) {
        try {
            // PortOne SDK 호출 → Payment 조회
            System.out.println("Sync Start!");
            var actualPayment = portoneClient.getPayment(paymentId).join();
            System.out.println("actualPayment: " + actualPayment);
            switch (actualPayment) {
                case io.portone.sdk.server.payment.PaidPayment paid -> {
                    log.info("결제 성공: {}", paymentId);
                    Payment payment = paymentRepository.findByMerchantUid(paymentId)
                            .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));

                    // 이미 최종상태면 중복 처리 방지
                    if (payment.getStatus() == PAID) {
                        log.info("이미 PAID 처리된 결제입니다. paymentId={}", paymentId);
                        return PaymentResponse.from(payment);
                    }


                    payment.setStatus(PAID);

                    Order order = payment.getOrder();
                    order.setStatus(OrderStatus.AFTER_PAY);
                    orderRepository.save(order);

                    PaymentHistory history = PaymentHistory.builder()
                            .payment(payment)
                            .event(PaymentEvent.TRANSFER_SUCCEEDED)
                            .amountDelta(order.getTotalPrice())
                            .build();
                    paymentHistoryRepository.save(history);

                    //TODO : 결제 완료 시 수행할 로직들 작성

                    return PaymentResponse.from(paymentRepository.save(payment));
                }
                case io.portone.sdk.server.payment.VirtualAccountIssuedPayment issued -> {
                    Payment payment = paymentRepository.findByMerchantUid(paymentId)
                            .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));
                    payment.setStatus(PaymentStatus.RESERVED);

                    PaymentHistory history = PaymentHistory.builder()
                            .payment(payment)
                            .event(PaymentEvent.RESERVE_SUCCEEDED)
                            .build();
                    paymentHistoryRepository.save(history);

                    return PaymentResponse.from(paymentRepository.save(payment));
                }
                default -> {
                    log.warn("알 수 없는 결제 상태: {}", actualPayment);
                    return PaymentResponse.from(null);
                }
            }
        } catch (Exception e) {
            log.error("PortOne 결제 동기화 실패: {}", paymentId, e);
            throw new RuntimeException("결제 동기화 실패");
        }
    }

    @Transactional
    public void handleWebhook(String body, String webhookId, String webhookSignature, String webhookTimestamp) {
        Webhook webhook;
        try {
            webhook = portoneWebhook.verify(body, webhookId, webhookSignature, webhookTimestamp);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(body);
            String paymentId = root.get("payment_id").asText();

            log.info("Webhook 수신 → paymentId: {}", paymentId);
        } catch (Exception e) {
            throw new BusinessException(ExceptionType.UNEXPECTED_SERVER_ERROR);
        }
        if (webhook instanceof io.portone.sdk.server.webhook.WebhookTransaction tx) {
            syncPayment(tx.getData().getPaymentId());
        }
    }

}
