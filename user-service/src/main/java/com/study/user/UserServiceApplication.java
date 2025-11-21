package com.study.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * User Service - 사용자 관리 서비스
 *
 * 학습 목표:
 * 1. Spring Data JPA를 활용한 데이터베이스 연동
 *    - Entity, Repository, JPQL, QueryDSL
 *    - N+1 문제, FetchType, 영속성 컨텍스트
 * 2. Spring Security를 활용한 인증/인가
 *    - JWT 기반 인증
 *    - UserDetailsService, SecurityFilterChain
 *    - Password Encoding
 * 3. RESTful API 설계
 * 4. DTO 패턴, 예외 처리
 */
@EnableJpaAuditing
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"com.study.user", "com.study.common"})
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
