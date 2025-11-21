package com.study.product.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Review - Embedded Document
 *
 * MongoDB 학습 포인트:
 * - Embedded Document: Product 안에 포함되는 리뷰 정보
 * - RDBMS의 OneToMany 관계와 비교
 * - 장점: Join 없이 한 번의 쿼리로 조회 가능
 * - 단점: Document 크기 제한 (16MB)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    private Long userId;
    private String username;
    private Integer rating;  // 1-5
    private String comment;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
