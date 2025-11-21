package com.study.order.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 주문 상태 Enum
 */
@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    PENDING("대기 중"),
    CONFIRMED("확인됨"),
    PROCESSING("처리 중"),
    SHIPPED("배송 중"),
    DELIVERED("배송 완료"),
    CANCELLED("취소됨");

    private final String description;
}
