package com.ocean.piuda.payment.service;

import com.ocean.piuda.global.api.exception.BusinessException;
import com.ocean.piuda.global.api.exception.ExceptionType;
import com.ocean.piuda.payment.dto.response.OrderDetailResponse;
import com.ocean.piuda.payment.dto.response.OrderItemResponse;
import com.ocean.piuda.payment.dto.response.OrderResponse;
import com.ocean.piuda.payment.entity.Order;
import com.ocean.piuda.payment.entity.OrderItem;
import com.ocean.piuda.payment.entity.Payment;
import com.ocean.piuda.payment.repository.OrderItemRepository;
import com.ocean.piuda.payment.repository.OrderRepository;
import com.ocean.piuda.payment.repository.PaymentRepository;
import com.ocean.piuda.security.jwt.enums.Role;
import com.ocean.piuda.security.jwt.service.TokenUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderQueryService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final TokenUserService tokenUserService;
    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders() {
        Long userId = tokenUserService.getCurrentUser().getId();
        return orderRepository.findByUserIdOrderByIdDesc(userId).stream()
                .map(o -> {
                    String merchantUid = paymentRepository.findByOrderId(o.getId())
                            .map(Payment::getMerchantUid)
                            .orElse(null);

                    return new OrderResponse(
                            o.getId(),
                            o.getStore().getId(),
                            o.getUser().getId(),
                            o.getStatus(),
                            o.getTotalOriginalPrice(),
                            o.getTotalDiscountedPrice(),
                            o.getTotalPrice(),
                            merchantUid
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(Long orderId) {
        Order o = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));

        Long userId = tokenUserService.getCurrentUser().getId();
        var currentUser = tokenUserService.getCurrentUser();

        boolean isOwner = o.getUser().getId().equals(userId);
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isMerchant = currentUser.getRole() == Role.MERCHANT
                && o.getStore().getOwner().getId().equals(userId);

        if (!(isOwner || isAdmin || isMerchant)) {
            throw  new BusinessException(ExceptionType.RESOURCE_NOT_FOUND);
        }

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        var itemDtos = items.stream().map(it -> new OrderItemResponse(
                it.getId(),
                it.getMenu() != null ? it.getMenu().getId() : null,
                it.getMenuNameSnapshot(),
                it.getMenuBasePriceSnapshot(),
                it.getMenuDiscountedPriceSnapshot(),
                it.getQuantity()
        )).toList();

        String merchantUid = paymentRepository.findByOrderId(orderId)
                .map(p -> p.getMerchantUid())
                .orElse(null);

        return new OrderDetailResponse(
                o.getId(),
                o.getStore().getId(),
                o.getUser().getId(),
                o.getStatus(),
                o.getTotalOriginalPrice(),
                o.getTotalDiscountedPrice(),
                o.getTotalPrice(),
                itemDtos,
                merchantUid
        );
    }
}