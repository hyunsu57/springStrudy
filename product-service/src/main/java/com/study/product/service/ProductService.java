package com.study.product.service;

import com.study.common.exception.BusinessException;
import com.study.product.domain.Product;
import com.study.product.domain.Review;
import com.study.product.dto.ProductDto;
import com.study.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Product Service
 *
 * 학습 포인트:
 * - MongoDB CRUD 연산
 * - Document 수정 방식
 * - 검색 기능 구현
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * 상품 생성
     */
    public ProductDto.Response createProduct(ProductDto.CreateRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .tags(request.getTags())
                .active(true)
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("상품 생성: id={}, name={}", savedProduct.getId(), savedProduct.getName());

        return ProductDto.Response.from(savedProduct);
    }

    /**
     * 상품 조회
     */
    public ProductDto.Response getProduct(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("상품을 찾을 수 없습니다", "PRODUCT_NOT_FOUND"));
        return ProductDto.Response.from(product);
    }

    /**
     * 전체 상품 조회
     */
    public List<ProductDto.Response> getAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductDto.Response::from)
                .collect(Collectors.toList());
    }

    /**
     * 활성 상품 조회
     */
    public List<ProductDto.Response> getActiveProducts() {
        return productRepository.findByActiveTrue().stream()
                .map(ProductDto.Response::from)
                .collect(Collectors.toList());
    }

    /**
     * 카테고리별 조회
     */
    public List<ProductDto.Response> getProductsByCategory(String category) {
        return productRepository.findByCategoryAndActiveTrue(category).stream()
                .map(ProductDto.Response::from)
                .collect(Collectors.toList());
    }

    /**
     * 가격 범위로 조회
     */
    public List<ProductDto.Response> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findByPriceBetween(minPrice, maxPrice).stream()
                .map(ProductDto.Response::from)
                .collect(Collectors.toList());
    }

    /**
     * 텍스트 검색
     */
    public List<ProductDto.Response> searchProducts(String keyword) {
        return productRepository.searchByText(keyword).stream()
                .map(ProductDto.Response::from)
                .collect(Collectors.toList());
    }

    /**
     * 태그로 검색
     */
    public List<ProductDto.Response> getProductsByTag(String tag) {
        return productRepository.findByTagsContaining(tag).stream()
                .map(ProductDto.Response::from)
                .collect(Collectors.toList());
    }

    /**
     * 상품 수정
     */
    public ProductDto.Response updateProduct(String id, ProductDto.UpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("상품을 찾을 수 없습니다", "PRODUCT_NOT_FOUND"));

        product.updateInfo(
                request.getName(),
                request.getDescription(),
                request.getCategory(),
                request.getPrice(),
                request.getTags()
        );

        Product updatedProduct = productRepository.save(product);
        log.info("상품 수정: id={}, name={}", updatedProduct.getId(), updatedProduct.getName());

        return ProductDto.Response.from(updatedProduct);
    }

    /**
     * 재고 수정
     */
    public ProductDto.Response updateStock(String id, Integer quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("상품을 찾을 수 없습니다", "PRODUCT_NOT_FOUND"));

        product.updateStock(quantity);
        Product updatedProduct = productRepository.save(product);
        log.info("재고 수정: id={}, stock={}", updatedProduct.getId(), quantity);

        return ProductDto.Response.from(updatedProduct);
    }

    /**
     * 리뷰 추가
     */
    public ProductDto.Response addReview(String id, ProductDto.AddReviewRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("상품을 찾을 수 없습니다", "PRODUCT_NOT_FOUND"));

        Review review = Review.builder()
                .userId(request.getUserId())
                .username(request.getUsername())
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        product.addReview(review);
        Product updatedProduct = productRepository.save(product);
        log.info("리뷰 추가: productId={}, userId={}, rating={}",
                id, request.getUserId(), request.getRating());

        return ProductDto.Response.from(updatedProduct);
    }

    /**
     * 상품 비활성화
     */
    public void deactivateProduct(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("상품을 찾을 수 없습니다", "PRODUCT_NOT_FOUND"));

        product.deactivate();
        productRepository.save(product);
        log.info("상품 비활성화: id={}", id);
    }

    /**
     * 재고 부족 상품 조회
     */
    public List<ProductDto.Response> getLowStockProducts(Integer threshold) {
        return productRepository.findLowStockProducts(threshold).stream()
                .map(ProductDto.Response::from)
                .collect(Collectors.toList());
    }
}
