package com.study.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API Gateway - 모든 서비스의 진입점
 *
 * 학습 목표:
 * 1. API Gateway 패턴의 개념과 필요성 이해
 * 2. Spring Cloud Gateway의 라우팅 설정 학습
 * 3. 필터를 통한 공통 기능(인증, 로깅 등) 구현 학습
 * 4. WebFlux 기반의 Reactive Programming 기초 학습
 */
@EnableDiscoveryClient
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
