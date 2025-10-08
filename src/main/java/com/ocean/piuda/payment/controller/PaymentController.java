//package com.ocean.piuda.payment.controller;
//
//import com.ocean.piuda.global.api.dto.ApiData;
//import com.ocean.piuda.global.api.exception.ExceptionType;
//import com.ocean.piuda.payment.dto.request.PaymentCompleteRequest;
//import com.ocean.piuda.payment.dto.response.PaymentResponse;
//import com.ocean.piuda.payment.service.PaymentService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.view.RedirectView;
//
//
//
//@Tag(name = "결제", description = "결제 API")
//@Slf4j
//@RestController
//@RequestMapping("/api/payment")
//@RequiredArgsConstructor
//public class PaymentController {
//    private final PaymentService paymentService;
//
//    @Operation(
//            summary = "결제 완료",
//            description = "결제 직후 호출하여 주문/결제 상태를 업데이트합니다"
//    )
//    @PostMapping("/complete")
//    public ApiData<PaymentResponse> completePayment(
//            @RequestBody PaymentCompleteRequest request
//    ) {
//        log.info("PaymentController.completePayment: {}", request);
//        PaymentResponse response = paymentService.syncPayment(request.paymentId());
//        return ApiData.ok(response);
//    }
//
//    @Operation(
//            summary = "결제 후 연결 주소",
//            description = "사용하지 않는 API입니다.(서버 개발시에 사용)"
//    )
//    @GetMapping("/redirect")
//    public RedirectView paymentRedirect(
//            @RequestParam String paymentId,
//            @RequestParam(required = false) String status
//    ) {
//        log.info("PaymentController.REDIRECT: paymentId={}, status={}", paymentId, status);
////        PaymentResponse response = paymentService.syncPayment(paymentId);
//        String url = "http://localhost:5500/paymentResult.html?paymentId=" + paymentId + "&status=" + status;
//        return new RedirectView(url);
//    }
//
//    // 포트원 웹훅 처리
//    @Operation(
//            summary = "결제 웹훅",
//            description = "결제 진행후 포트원으로부터 데이터를 받는 API입니다. (FE사용X)"
//    )
//    @PostMapping("/webhook")
//    public ApiData<?> handleWebhook(
//            @RequestBody String body,
//            @RequestHeader("webhook-id") String webhookId,
//            @RequestHeader("webhook-timestamp") String webhookTimestamp,
//            @RequestHeader("webhook-signature") String webhookSignature
//    ) {
//        try {
//            log.info("Webhook Body: {}", body);
//            log.info("WebhookId: {}", webhookId);
//            log.info("WebhookTimestamp: {}", webhookTimestamp);
//            log.info("WebhookSignature: {}", webhookSignature);
//
//            paymentService.handleWebhook(body, webhookId, webhookSignature, webhookTimestamp);
//            return ApiData.noContent();
//        } catch (Exception e) {
//            log.error("웹훅 처리 실패", e);
//            return ApiData.error(ExceptionType.UNEXPECTED_SERVER_ERROR);
//        }
//    }
//
//
//}