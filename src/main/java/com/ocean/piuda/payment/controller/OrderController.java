//package com.ocean.piuda.payment.controller;
//
//import com.ocean.piuda.global.api.dto.ApiData;
//import com.ocean.piuda.payment.dto.request.OrderCreateRequest;
//import com.ocean.piuda.payment.dto.response.OrderDetailResponse;
//import com.ocean.piuda.payment.dto.response.OrderResponse;
//import com.ocean.piuda.payment.service.OrderQueryService;
//import com.ocean.piuda.payment.service.OrderService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.*;
//
//
//import java.util.List;
//
//@Tag(name = "주문", description = "주문 API")
//@RestController
//@RequestMapping("/api/orders")
//@RequiredArgsConstructor
//public class OrderController {
//    private final OrderService orderService;
//    private final OrderQueryService orderQueryService;
//
//    @Operation(
//            summary = "주문 생성",
//            description = "사용자가 선택만 메뉴를 바탕으로 주문을 생성합니다"
//    )
//    @PostMapping
//    public ApiData<OrderResponse> create(@Valid @RequestBody OrderCreateRequest request) {
//        return ApiData.ok(orderService.createOrder(request));
//    }
//
//    @Operation(
//            summary = "내 주문 조회",
//            description = "사용자의 모든 주문 기록을 확인합니다"
//    )
//    @GetMapping("/me")
//    public ApiData<List<OrderResponse>> myOrders() {
//        return ApiData.ok(orderQueryService.getMyOrders());
//    }
//
//    @Operation(
//            summary = "주문 상세조회",
//            description = "특정 주문 내역을 조회합니다."
//    )
//    @GetMapping("/{orderId}")
//    public ApiData<OrderDetailResponse> detail(@PathVariable Long orderId) {
//        return ApiData.ok(orderQueryService.getOrderDetail(orderId));
//    }
//
//    @Operation(
//            summary = "주문 취소",
//            description = "주문을 취소합니다."
//    )
//    @PostMapping("/{orderId}/cancel")
//    public ApiData<Void> cancel(@PathVariable Long orderId) {
//        orderService.cancelOrder(orderId);
//        return ApiData.noContent();
//    }
//}
