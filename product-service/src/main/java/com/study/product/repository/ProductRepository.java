package com.study.product.repository;

import com.study.product.domain.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.math.BigDecimal;
import java.util.List;

/**
 * Product Repository
 *
 * MongoDB 학습 포인트:
 * - MongoRepository 기본 메서드
 * - Query Methods (메서드 이름 기반 쿼리)
 * - @Query를 통한 MongoDB 쿼리 작성
 * - 텍스트 검색
 */
public interface ProductRepository extends MongoRepository<Product, String> {

    /**
     * 카테고리별 조회
     */
    List<Product> findByCategory(String category);

    /**
     * 활성화된 상품 조회
     */
    List<Product> findByActiveTrue();

    /**
     * 카테고리 + 활성화 조건
     */
    List<Product> findByCategoryAndActiveTrue(String category);

    /**
     * 가격 범위로 조회
     */
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * 상품명 검색 (부분 일치)
     */
    List<Product> findByNameContaining(String keyword);

    /**
     * 태그로 검색
     */
    List<Product> findByTagsContaining(String tag);

    /**
     * MongoDB Query - 텍스트 검색
     * name 또는 description에서 검색
     */
    @Query("{ $or: [ { 'name': { $regex: ?0, $options: 'i' } }, " +
           "{ 'description': { $regex: ?0, $options: 'i' } } ] }")
    List<Product> searchByText(String keyword);

    /**
     * MongoDB Query - 복합 조건
     */
    @Query("{ 'category': ?0, 'price': { $gte: ?1, $lte: ?2 }, 'active': true }")
    List<Product> findByCategoryAndPriceRange(String category, BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * 재고가 부족한 상품 조회
     */
    @Query("{ 'stockQuantity': { $lte: ?0 }, 'active': true }")
    List<Product> findLowStockProducts(Integer threshold);
}
