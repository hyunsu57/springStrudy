package com.study.product.domain;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Product Document
 *
 * MongoDB 학습 포인트:
 * - @Document: MongoDB Collection 매핑
 * - @Id: MongoDB의 ObjectId 자동 생성
 * - @Indexed: 인덱스 생성 (검색 성능 향상)
 * - @TextIndexed: 텍스트 검색용 인덱스
 * - Embedded Document (Review)
 */
@Document(collection = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Product {

    @Id
    private String id;  // MongoDB ObjectId (자동 생성)

    @TextIndexed  // 텍스트 검색 가능
    @Indexed
    private String name;

    @TextIndexed
    private String description;

    @Indexed
    private String category;

    private BigDecimal price;

    private Integer stockQuantity;

    @Builder.Default
    private List<String> tags = new ArrayList<>();

    // Embedded Document - MongoDB의 특징
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    @Builder.Default
    private Boolean active = true;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * 비즈니스 메서드
     */
    public void updateInfo(String name, String description, String category,
                          BigDecimal price, List<String> tags) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.tags = tags != null ? tags : new ArrayList<>();
    }

    public void updateStock(Integer quantity) {
        this.stockQuantity = quantity;
    }

    public void addReview(Review review) {
        this.reviews.add(review);
    }

    public void deactivate() {
        this.active = false;
    }

    /**
     * 평균 평점 계산
     */
    public Double getAverageRating() {
        if (reviews.isEmpty()) {
            return 0.0;
        }
        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }
}
