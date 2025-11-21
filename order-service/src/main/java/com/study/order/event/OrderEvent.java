package com.study.order.event;

import com.study.order.domain.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 주문 이벤트
 *
 * Kafka를 통해 발행되는 이벤트 객체
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEvent {
    private String eventType;  // ORDER_CREATED, ORDER_STATUS_UPDATED, ORDER_CANCELLED
    private Long orderId;
    private Long userId;
    private OrderStatus status;
    private BigDecimal totalAmount;

    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();
}
