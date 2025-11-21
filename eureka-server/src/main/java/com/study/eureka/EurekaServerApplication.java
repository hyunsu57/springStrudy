package com.study.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka Server - 서비스 디스커버리
 *
 * 학습 목표:
 * 1. MSA에서 서비스 등록과 발견의 개념 이해
 * 2. Eureka Server 설정 방법 학습
 * 3. 서비스 간 통신을 위한 Service Registry 패턴 학습
 */
@EnableEurekaServer
@SpringBootApplication
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
