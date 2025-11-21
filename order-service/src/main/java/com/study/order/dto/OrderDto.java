package com.study.order.dto;

import com.study.order.domain.Order;
import com.study.order.domain.OrderItem;
import com.study.order.domain.OrderStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Order DTO 모음 클래스
 */
public class OrderDto {

    /**
     * 주문 생성 요청 DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        @NotNull(message = "사용자 ID는 필수입니다")
        private Long userId;

        @NotEmpty(message = "주문 항목은 최소 1개 이상이어야 합니다")
        private List<OrderItemRequest> items;
    }

    /**
     * 주문 항목 요청 DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemRequest {
        @NotNull(message = "상품 ID는 필수입니다")
        private Long productId;

        @NotNull(message = "상품명은 필수입니다")
        private String productName;

        @NotNull(message = "수량은 필수입니다")
        @Min(value = 1, message = "수량은 1개 이상이어야 합니다")
        private Integer quantity;

        @NotNull(message = "가격은 필수입니다")
        @Min(value = 0, message = "가격은 0 이상이어야 합니다")
        private BigDecimal price;
    }

    /**
     * 주문 응답 DTO (Redis 캐시용 Serializable)
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response implements Serializable {
        private Long id;
        private Long userId;
        private OrderStatus status;
        private BigDecimal totalAmount;
        private List<OrderItemResponse> items;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Response from(Order order) {
            return Response.builder()
                    .id(order.getId())
                    .userId(order.getUserId())
                    .status(order.getStatus())
                    .totalAmount(order.getTotalAmount())
                    .items(order.getOrderItems().stream()
                            .map(OrderItemResponse::from)
                            .collect(Collectors.toList()))
                    .createdAt(order.getCreatedAt())
                    .updatedAt(order.getUpdatedAt())
                    .build();
        }
    }

    /**
     * 주문 항목 응답 DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemResponse implements Serializable {
        private Long id;
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal subtotal;

        public static OrderItemResponse from(OrderItem item) {
            return OrderItemResponse.builder()
                    .id(item.getId())
                    .productId(item.getProductId())
                    .productName(item.getProductName())
                    .quantity(item.getQuantity())
                    .price(item.getPrice())
                    .subtotal(item.getSubtotal())
                    .build();
        }
    }

    /**
     * 주문 상태 변경 요청 DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusUpdateRequest {
        @NotNull(message = "주문 상태는 필수입니다")
        private OrderStatus status;
    }
}
