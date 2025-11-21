package com.study.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Product Service - 상품 관리 서비스
 *
 * 학습 목표:
 * 1. MongoDB를 활용한 NoSQL 데이터베이스 연동
 *    - Document 기반 데이터 모델링
 *    - Embedded vs Reference
 *    - Index 설정
 * 2. Spring Data MongoDB
 *    - MongoRepository 활용
 *    - Query Methods
 *    - Aggregation Pipeline
 * 3. NoSQL vs RDBMS 차이점 이해
 * 4. 검색 기능 구현 (Text Index)
 */
@EnableMongoAuditing
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"com.study.product", "com.study.common"})
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
