package com.study.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Order Service - 주문 관리 서비스
 *
 * 학습 목표:
 * 1. Redis를 활용한 캐싱
 *    - @Cacheable, @CacheEvict, @CachePut
 *    - Cache 전략 (Look-Aside, Write-Through)
 *    - Redis Template 활용
 * 2. Kafka를 활용한 메시징
 *    - Producer/Consumer 패턴
 *    - Event-Driven Architecture
 *    - 비동기 처리
 * 3. 분산 시스템에서의 트랜잭션 처리
 * 4. 서비스 간 통신 (Inter-Service Communication)
 */
@EnableCaching
@EnableJpaAuditing
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"com.study.order", "com.study.common"})
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
