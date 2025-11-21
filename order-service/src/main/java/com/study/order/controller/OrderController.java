package com.study.order.controller;

import com.study.common.dto.ApiResponse;
import com.study.order.dto.OrderDto;
import com.study.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Order Controller
 */
@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 주문 생성
     */
    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OrderDto.Response> createOrder(@Valid @RequestBody OrderDto.CreateRequest request) {
        log.info("주문 생성 요청: userId={}", request.getUserId());
        OrderDto.Response response = orderService.createOrder(request);
        return ApiResponse.success("주문이 생성되었습니다", response);
    }

    /**
     * 주문 조회
     */
    @GetMapping("/orders/{orderId}")
    public ApiResponse<OrderDto.Response> getOrder(@PathVariable Long orderId) {
        log.info("주문 조회 요청: orderId={}", orderId);
        OrderDto.Response response = orderService.getOrder(orderId);
        return ApiResponse.success(response);
    }

    /**
     * 전체 주문 조회
     */
    @GetMapping("/orders")
    public ApiResponse<List<OrderDto.Response>> getAllOrders() {
        log.info("전체 주문 조회 요청");
        List<OrderDto.Response> orders = orderService.getAllOrders();
        return ApiResponse.success(orders);
    }

    /**
     * 사용자별 주문 조회
     */
    @GetMapping("/users/{userId}/orders")
    public ApiResponse<List<OrderDto.Response>> getUserOrders(@PathVariable Long userId) {
        log.info("사용자 주문 조회 요청: userId={}", userId);
        List<OrderDto.Response> orders = orderService.getUserOrders(userId);
        return ApiResponse.success(orders);
    }

    /**
     * 주문 상태 변경
     */
    @PutMapping("/orders/{orderId}/status")
    public ApiResponse<OrderDto.Response> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderDto.StatusUpdateRequest request) {
        log.info("주문 상태 변경 요청: orderId={}, status={}", orderId, request.getStatus());
        OrderDto.Response response = orderService.updateOrderStatus(orderId, request);
        return ApiResponse.success("주문 상태가 변경되었습니다", response);
    }

    /**
     * 주문 취소
     */
    @DeleteMapping("/orders/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelOrder(@PathVariable Long orderId) {
        log.info("주문 취소 요청: orderId={}", orderId);
        orderService.cancelOrder(orderId);
    }
}
