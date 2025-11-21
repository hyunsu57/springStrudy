package com.study.product.controller;

import com.study.common.dto.ApiResponse;
import com.study.product.dto.ProductDto;
import com.study.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Product Controller
 */
@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 상품 생성
     */
    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProductDto.Response> createProduct(@Valid @RequestBody ProductDto.CreateRequest request) {
        log.info("상품 생성 요청: name={}", request.getName());
        ProductDto.Response response = productService.createProduct(request);
        return ApiResponse.success("상품이 생성되었습니다", response);
    }

    /**
     * 상품 조회
     */
    @GetMapping("/products/{id}")
    public ApiResponse<ProductDto.Response> getProduct(@PathVariable String id) {
        log.info("상품 조회 요청: id={}", id);
        ProductDto.Response response = productService.getProduct(id);
        return ApiResponse.success(response);
    }

    /**
     * 전체 상품 조회
     */
    @GetMapping("/products")
    public ApiResponse<List<ProductDto.Response>> getAllProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String search) {

        List<ProductDto.Response> products;

        if (category != null) {
            log.info("카테고리별 상품 조회: category={}", category);
            products = productService.getProductsByCategory(category);
        } else if (tag != null) {
            log.info("태그별 상품 조회: tag={}", tag);
            products = productService.getProductsByTag(tag);
        } else if (search != null) {
            log.info("상품 검색: keyword={}", search);
            products = productService.searchProducts(search);
        } else {
            log.info("전체 상품 조회");
            products = productService.getActiveProducts();
        }

        return ApiResponse.success(products);
    }

    /**
     * 가격 범위로 조회
     */
    @GetMapping("/products/price-range")
    public ApiResponse<List<ProductDto.Response>> getProductsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        log.info("가격 범위 조회: {} ~ {}", minPrice, maxPrice);
        List<ProductDto.Response> products = productService.getProductsByPriceRange(minPrice, maxPrice);
        return ApiResponse.success(products);
    }

    /**
     * 재고 부족 상품 조회
     */
    @GetMapping("/products/low-stock")
    public ApiResponse<List<ProductDto.Response>> getLowStockProducts(
            @RequestParam(defaultValue = "10") Integer threshold) {
        log.info("재고 부족 상품 조회: threshold={}", threshold);
        List<ProductDto.Response> products = productService.getLowStockProducts(threshold);
        return ApiResponse.success(products);
    }

    /**
     * 상품 수정
     */
    @PutMapping("/products/{id}")
    public ApiResponse<ProductDto.Response> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody ProductDto.UpdateRequest request) {
        log.info("상품 수정 요청: id={}", id);
        ProductDto.Response response = productService.updateProduct(id, request);
        return ApiResponse.success("상품이 수정되었습니다", response);
    }

    /**
     * 재고 수정
     */
    @PatchMapping("/products/{id}/stock")
    public ApiResponse<ProductDto.Response> updateStock(
            @PathVariable String id,
            @RequestParam Integer quantity) {
        log.info("재고 수정 요청: id={}, quantity={}", id, quantity);
        ProductDto.Response response = productService.updateStock(id, quantity);
        return ApiResponse.success("재고가 수정되었습니다", response);
    }

    /**
     * 리뷰 추가
     */
    @PostMapping("/products/{id}/reviews")
    public ApiResponse<ProductDto.Response> addReview(
            @PathVariable String id,
            @Valid @RequestBody ProductDto.AddReviewRequest request) {
        log.info("리뷰 추가 요청: productId={}, userId={}", id, request.getUserId());
        ProductDto.Response response = productService.addReview(id, request);
        return ApiResponse.success("리뷰가 추가되었습니다", response);
    }

    /**
     * 상품 비활성화
     */
    @DeleteMapping("/products/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateProduct(@PathVariable String id) {
        log.info("상품 비활성화 요청: id={}", id);
        productService.deactivateProduct(id);
    }
}
