package com.ocean.piuda.payment.service;

import com.ocean.piuda.global.api.exception.BusinessException;
import com.ocean.piuda.global.api.exception.ExceptionType;
import com.ocean.piuda.payment.dto.request.OrderCreateRequest;
import com.ocean.piuda.payment.dto.response.OrderResponse;
import com.ocean.piuda.payment.entity.Order;
import com.ocean.piuda.payment.entity.OrderItem;
import com.ocean.piuda.payment.entity.Payment;
import com.ocean.piuda.payment.entity.PaymentHistory;
import com.ocean.piuda.payment.enums.OrderStatus;
import com.ocean.piuda.payment.enums.PaymentEvent;
import com.ocean.piuda.payment.enums.PaymentStatus;
import com.ocean.piuda.payment.repository.OrderItemRepository;
import com.ocean.piuda.payment.repository.OrderRepository;
import com.ocean.piuda.payment.repository.PaymentHistoryRepository;
import com.ocean.piuda.payment.repository.PaymentRepository;
import com.ocean.piuda.security.jwt.service.TokenUserService;
import com.ocean.piuda.store.entity.Menu;
import com.ocean.piuda.store.entity.Store;
import com.ocean.piuda.store.repository.MenuRepository;
import com.ocean.piuda.store.repository.StoreRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;
    private final TokenUserService tokenUserService;
    private final PaymentRepository paymentRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;

    public OrderResponse createOrder(OrderCreateRequest req) {


        Store store = storeRepository.findById(req.storeId())
                .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));

        Long userId = tokenUserService.getCurrentUser().getId();

        BigDecimal totalOriginal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalFinal = BigDecimal.ZERO;

        // 우선 빈 주문 생성 (AFTER items insert, totals 업데이트)
        Order order = Order.builder()
                .store(store)
                .user(tokenUserService.getCurrentUser())
                .status(OrderStatus.BEFORE_PAY)
                .totalOriginalPrice(0L)
                .totalDiscountedPrice(0L)
                .totalPrice(0L)
                .build();

        order = orderRepository.save(order);

        for (OrderCreateRequest.Item it : req.items()) {
            Menu menu = menuRepository.findByIdAndIsActiveTrue(it.menuId())
                    .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));

            // 메뉴가 같은 가게 소속인지 검증
            if (!menu.getStore().getId().equals(store.getId())) {
                throw new BusinessException(ExceptionType.BINDING_ERROR);
            }

            int qty = it.quantity();

            BigDecimal base = menu.getBasePrice();
            BigDecimal discPrice = menu.getDiscountedPrice();

            BigDecimal itemOriginal = base.multiply(BigDecimal.valueOf(qty));
            BigDecimal itemFinal = discPrice.multiply(BigDecimal.valueOf(qty));
            BigDecimal itemDiscount = itemOriginal.subtract(itemFinal);

            totalOriginal = totalOriginal.add(itemOriginal);
            totalDiscount = totalDiscount.add(itemDiscount);
            totalFinal = totalFinal.add(itemFinal);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .menu(menu)
                    .menuNameSnapshot(menu.getName())
                    .menuBasePriceSnapshot(base.longValueExact())
                    .menuDiscountedPriceSnapshot(discPrice.longValueExact())
                    .quantity(qty)
                    .build();

            orderItemRepository.save(orderItem);
        }

        order = patchTotals(order, totalOriginal, totalDiscount, totalFinal);

        String merchantUid = UUID.randomUUID().toString();
        Payment payment = Payment.builder()
                .order(order)
                .merchantUid(merchantUid)
                .method(null)
                .status(PaymentStatus.CREATED)
                .totalCouponUsedPrice(0L)
                .totalPrice(order.getTotalPrice())
                .idempotencyKey(UUID.randomUUID().toString())
                .version(0)
                .build();

        paymentRepository.save(payment);

        PaymentHistory paymentHistory = PaymentHistory.builder()
                .payment(payment)
                .event(PaymentEvent.CREATED)
                .build();

        paymentHistoryRepository.save(paymentHistory);

        return new OrderResponse(
                order.getId(),
                order.getStore().getId(),
                userId,
                order.getStatus(),
                order.getTotalOriginalPrice(),
                order.getTotalDiscountedPrice(),
                order.getTotalPrice(),
                payment.getMerchantUid()
        );
    }

    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));

        // 본인 주문인지 확인
        Long userId = tokenUserService.getCurrentUser().getId();
        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException(ExceptionType.ACCESS_DENIED);
        }

        if (order.getStatus() != OrderStatus.BEFORE_PAY) {
            throw new BusinessException(ExceptionType.INVALID_VALUE_ERROR);
        }

        order = order.toBuilder().status(OrderStatus.CANCELED).build();
        orderRepository.save(order);
    }

    private Order patchTotals(Order order, BigDecimal totalOriginal, BigDecimal totalDiscount, BigDecimal totalFinal) {
        order = order.toBuilder()
                .totalOriginalPrice(totalOriginal.longValueExact())
                .totalDiscountedPrice(totalDiscount.longValueExact())
                .totalPrice(totalFinal.longValueExact())
                .build();
        return orderRepository.save(order);
    }
}
