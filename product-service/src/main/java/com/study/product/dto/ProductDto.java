package com.study.product.dto;

import com.study.product.domain.Product;
import com.study.product.domain.Review;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Product DTO 모음 클래스
 */
public class ProductDto {

    /**
     * 상품 생성 요청 DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        @NotBlank(message = "상품명은 필수입니다")
        private String name;

        @NotBlank(message = "상품 설명은 필수입니다")
        private String description;

        @NotBlank(message = "카테고리는 필수입니다")
        private String category;

        @NotNull(message = "가격은 필수입니다")
        @Min(value = 0, message = "가격은 0 이상이어야 합니다")
        private BigDecimal price;

        @NotNull(message = "재고 수량은 필수입니다")
        @Min(value = 0, message = "재고는 0 이상이어야 합니다")
        private Integer stockQuantity;

        private List<String> tags;
    }

    /**
     * 상품 수정 요청 DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @NotBlank(message = "상품명은 필수입니다")
        private String name;

        @NotBlank(message = "상품 설명은 필수입니다")
        private String description;

        @NotBlank(message = "카테고리는 필수입니다")
        private String category;

        @NotNull(message = "가격은 필수입니다")
        @Min(value = 0, message = "가격은 0 이상이어야 합니다")
        private BigDecimal price;

        private List<String> tags;
    }

    /**
     * 리뷰 추가 요청 DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddReviewRequest {
        @NotNull(message = "사용자 ID는 필수입니다")
        private Long userId;

        @NotBlank(message = "사용자명은 필수입니다")
        private String username;

        @NotNull(message = "평점은 필수입니다")
        @Min(value = 1, message = "평점은 1 이상이어야 합니다")
        private Integer rating;

        private String comment;
    }

    /**
     * 상품 응답 DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private String id;
        private String name;
        private String description;
        private String category;
        private BigDecimal price;
        private Integer stockQuantity;
        private List<String> tags;
        private List<ReviewResponse> reviews;
        private Double averageRating;
        private Boolean active;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Response from(Product product) {
            return Response.builder()
                    .id(product.getId())
                    .name(product.getName())
                    .description(product.getDescription())
                    .category(product.getCategory())
                    .price(product.getPrice())
                    .stockQuantity(product.getStockQuantity())
                    .tags(product.getTags())
                    .reviews(product.getReviews().stream()
                            .map(ReviewResponse::from)
                            .collect(Collectors.toList()))
                    .averageRating(product.getAverageRating())
                    .active(product.getActive())
                    .createdAt(product.getCreatedAt())
                    .updatedAt(product.getUpdatedAt())
                    .build();
        }
    }

    /**
     * 리뷰 응답 DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewResponse {
        private Long userId;
        private String username;
        private Integer rating;
        private String comment;
        private LocalDateTime createdAt;

        public static ReviewResponse from(Review review) {
            return ReviewResponse.builder()
                    .userId(review.getUserId())
                    .username(review.getUsername())
                    .rating(review.getRating())
                    .comment(review.getComment())
                    .createdAt(review.getCreatedAt())
                    .build();
        }
    }
}
