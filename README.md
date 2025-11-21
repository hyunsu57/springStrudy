# Spring Boot 학습용 MSA 프로젝트

# 목차
- [Spring Boot 학습용 MSA 프로젝트](#spring-boot-학습용-msa-프로젝트)
- [목차](#목차)
  - [프로젝트 구조](#프로젝트-구조)
  - [기술 스택](#기술-스택)
    - [공통](#공통)
    - [서비스별 주요 기술](#서비스별-주요-기술)
      - [1. Eureka Server (서비스 디스커버리)](#1-eureka-server-서비스-디스커버리)
      - [2. API Gateway](#2-api-gateway)
      - [3. User Service (사용자 관리)](#3-user-service-사용자-관리)
      - [4. Order Service (주문 관리)](#4-order-service-주문-관리)
      - [5. Product Service (상품 관리)](#5-product-service-상품-관리)
  - [실행 방법](#실행-방법)
    - [1. 필수 준비사항](#1-필수-준비사항)
    - [2. 프로젝트 빌드](#2-프로젝트-빌드)
    - [3. 서비스 실행 순서](#3-서비스-실행-순서)
      - [1) Eureka Server 실행](#1-eureka-server-실행)
      - [2) API Gateway 실행](#2-api-gateway-실행)
      - [3) 비즈니스 서비스 실행 (순서 무관)](#3-비즈니스-서비스-실행-순서-무관)
    - [4. 전체 서비스 동시 실행](#4-전체-서비스-동시-실행)
  - [API 테스트](#api-테스트)
    - [User Service (사용자 관리)](#user-service-사용자-관리)
      - [회원가입](#회원가입)
      - [사용자 조회](#사용자-조회)
      - [전체 사용자 조회](#전체-사용자-조회)
    - [Product Service (상품 관리)](#product-service-상품-관리)
      - [상품 생성](#상품-생성)
      - [상품 검색](#상품-검색)
      - [리뷰 추가](#리뷰-추가)
    - [Order Service (주문 관리)](#order-service-주문-관리)
      - [주문 생성](#주문-생성)
      - [주문 조회 (캐싱 테스트)](#주문-조회-캐싱-테스트)
      - [주문 상태 변경](#주문-상태-변경)
  - [학습 가이드](#학습-가이드)
    - [1단계: 단일 서비스 이해](#1단계-단일-서비스-이해)
    - [2단계: MSA 인프라 이해](#2단계-msa-인프라-이해)
    - [3단계: 고급 기능 학습](#3단계-고급-기능-학습)
    - [4단계: 실습 과제](#4단계-실습-과제)
  - [주요 학습 포인트](#주요-학습-포인트)
    - [JPA (User Service)](#jpa-user-service)
    - [Spring Security (User Service)](#spring-security-user-service)
    - [Redis (Order Service)](#redis-order-service)
    - [Kafka (Order Service)](#kafka-order-service)
    - [MongoDB (Product Service)](#mongodb-product-service)
    - [Spring Cloud (전체)](#spring-cloud-전체)
  - [문제 해결](#문제-해결)
    - [Redis 연결 실패](#redis-연결-실패)
    - [Kafka 연결 실패](#kafka-연결-실패)
    - [포트 충돌](#포트-충돌)
  - [라이센스](#라이센스)

Spring Framework 6, Spring Boot 3 기반의 마이크로서비스 아키텍처(MSA) 학습 프로젝트입니다.

## 프로젝트 구조

```
SpringStudy/
├── common/                 # 공통 라이브러리
├── eureka-server/         # 서비스 디스커버리 (포트: 8761)
├── api-gateway/           # API Gateway (포트: 8080)
├── user-service/          # 사용자 서비스 (포트: 8081)
├── order-service/         # 주문 서비스 (포트: 8082)
└── product-service/       # 상품 서비스 (포트: 8083)
```

## 기술 스택

### 공통
- Java 21
- Spring Boot 3.3.5
- Spring Framework 6.x
- Spring Cloud 2023.0.3
- Gradle 8.x

### 서비스별 주요 기술

#### 1. Eureka Server (서비스 디스커버리)
- **학습 목표**: MSA에서의 서비스 등록과 발견
- **기술**: Netflix Eureka
- **포트**: 8761
- **대시보드**: http://localhost:8761

#### 2. API Gateway
- **학습 목표**: API Gateway 패턴, 라우팅, 필터
- **기술**: Spring Cloud Gateway, WebFlux
- **포트**: 8080
- **주요 학습 내용**:
  - Reactive Programming (WebFlux)
  - 라우팅 설정
  - Global Filter 구현

#### 3. User Service (사용자 관리)
- **학습 목표**: JPA, Spring Security
- **기술**:
  - Spring Data JPA
  - Spring Security 6.x
  - H2 Database
  - JWT (학습용 구조 포함)
- **포트**: 8081
- **H2 콘솔**: http://localhost:8081/h2-console
- **주요 학습 내용**:
  - JPA Entity, Repository 패턴
  - N+1 문제, FetchType
  - Spring Security 설정
  - Password Encoding
  - RESTful API 설계

#### 4. Order Service (주문 관리)
- **학습 목표**: Redis 캐싱, Kafka 메시징
- **기술**:
  - Spring Data JPA
  - Spring Data Redis
  - Spring Kafka
  - H2 Database
- **포트**: 8082
- **H2 콘솔**: http://localhost:8082/h2-console
- **주요 학습 내용**:
  - Redis 캐싱 전략 (@Cacheable, @CacheEvict, @CachePut)
  - Kafka Producer/Consumer
  - Event-Driven Architecture
  - 분산 시스템에서의 트랜잭션

#### 5. Product Service (상품 관리)
- **학습 목표**: MongoDB (NoSQL)
- **기술**:
  - Spring Data MongoDB
  - Embedded MongoDB (학습용)
- **포트**: 8083
- **주요 학습 내용**:
  - Document 기반 데이터 모델링
  - Embedded Document vs Reference
  - MongoDB Query Methods
  - Text 검색, Index 활용
  - NoSQL vs RDBMS 비교

## 실행 방법

### 1. 필수 준비사항
- JDK 21 설치
- (선택) Redis 설치 및 실행 (localhost:6379)
- (선택) Kafka 설치 및 실행 (localhost:9092)

> Redis와 Kafka는 선택사항입니다. 없어도 애플리케이션은 실행됩니다.

### 2. 프로젝트 빌드
```bash
./gradlew build
```

### 3. 서비스 실행 순서

#### 1) Eureka Server 실행
```bash
./gradlew :eureka-server:bootRun
```
- 브라우저에서 http://localhost:8761 접속하여 대시보드 확인

#### 2) API Gateway 실행
```bash
./gradlew :api-gateway:bootRun
```

#### 3) 비즈니스 서비스 실행 (순서 무관)
```bash
# User Service
./gradlew :user-service:bootRun

# Order Service
./gradlew :order-service:bootRun

# Product Service
./gradlew :product-service:bootRun
```

### 4. 전체 서비스 동시 실행
```bash
# Windows
start cmd /k "gradlew :eureka-server:bootRun"
timeout /t 10
start cmd /k "gradlew :api-gateway:bootRun"
start cmd /k "gradlew :user-service:bootRun"
start cmd /k "gradlew :order-service:bootRun"
start cmd /k "gradlew :product-service:bootRun"

# Linux/Mac
./gradlew :eureka-server:bootRun & \
sleep 10 && \
./gradlew :api-gateway:bootRun & \
./gradlew :user-service:bootRun & \
./gradlew :order-service:bootRun & \
./gradlew :product-service:bootRun &
```

## API 테스트

### User Service (사용자 관리)

#### 회원가입
```bash
curl -X POST http://localhost:8080/api/users/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "name": "홍길동"
  }'
```

#### 사용자 조회
```bash
curl http://localhost:8080/api/users/users/1
```

#### 전체 사용자 조회
```bash
curl http://localhost:8080/api/users/users
```

### Product Service (상품 관리)

#### 상품 생성
```bash
curl -X POST http://localhost:8080/api/products/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "노트북",
    "description": "고성능 노트북",
    "category": "전자제품",
    "price": 1500000,
    "stockQuantity": 50,
    "tags": ["전자제품", "컴퓨터", "노트북"]
  }'
```

#### 상품 검색
```bash
# 텍스트 검색
curl "http://localhost:8080/api/products/products?search=노트북"

# 카테고리별 조회
curl "http://localhost:8080/api/products/products?category=전자제품"

# 태그별 조회
curl "http://localhost:8080/api/products/products?tag=컴퓨터"
```

#### 리뷰 추가
```bash
curl -X POST http://localhost:8080/api/products/products/{productId}/reviews \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "username": "홍길동",
    "rating": 5,
    "comment": "정말 좋은 제품입니다!"
  }'
```

### Order Service (주문 관리)

#### 주문 생성
```bash
curl -X POST http://localhost:8080/api/orders/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "items": [
      {
        "productId": 1,
        "productName": "노트북",
        "quantity": 2,
        "price": 1500000
      }
    ]
  }'
```

#### 주문 조회 (캐싱 테스트)
```bash
# 첫 번째 호출: DB에서 조회 (로그 확인)
curl http://localhost:8080/api/orders/orders/1

# 두 번째 호출: Redis 캐시에서 조회 (빠름)
curl http://localhost:8080/api/orders/orders/1
```

#### 주문 상태 변경
```bash
curl -X PUT http://localhost:8080/api/orders/orders/1/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "CONFIRMED"
  }'
```

## 학습 가이드

### 1단계: 단일 서비스 이해
각 서비스를 독립적으로 실행하고 학습:
- User Service: JPA, Security 집중 학습
- Product Service: MongoDB 집중 학습
- Order Service: Redis, Kafka 집중 학습

### 2단계: MSA 인프라 이해
- Eureka Server로 서비스 등록/발견 확인
- API Gateway를 통한 라우팅 확인
- 서비스 간 통신 테스트

### 3단계: 고급 기능 학습
- Redis 캐싱 전략 실험
- Kafka 이벤트 발행/구독
- 분산 트랜잭션 이해
- 서비스 장애 대응

### 4단계: 실습 과제
1. User Service에 JWT 인증 완성하기
2. Order Service에서 Product Service 호출 (RestTemplate/WebClient)
3. 주문 생성 시 재고 감소 로직 추가
4. Kafka를 통한 주문 알림 기능 추가
5. MongoDB Aggregation Pipeline 활용한 통계 기능

## 주요 학습 포인트

### JPA (User Service)
- Entity 설계 및 연관관계
- Repository 패턴
- JPQL, Query Methods
- N+1 문제 해결 (Fetch Join)
- Transaction 관리

### Spring Security (User Service)
- SecurityFilterChain 설정
- Password Encoding
- 인증/인가 처리

### Redis (Order Service)
- Cache Abstraction
- @Cacheable, @CacheEvict, @CachePut
- RedisTemplate 사용

### Kafka (Order Service)
- Producer/Consumer 구현
- Event-Driven Architecture
- 비동기 메시징

### MongoDB (Product Service)
- Document 모델링
- Embedded vs Reference
- Query Methods
- Text Search, Index

### Spring Cloud (전체)
- Service Discovery (Eureka)
- API Gateway
- Load Balancing

## 문제 해결

### Redis 연결 실패
Redis가 설치되지 않은 경우 애플리케이션은 정상 실행되지만 캐싱 기능은 동작하지 않습니다.
필요시 Docker로 실행:
```bash
docker run -d -p 6379:6379 redis
```

### Kafka 연결 실패
Kafka가 설치되지 않은 경우 이벤트 발행 기능은 동작하지 않습니다.
필요시 Docker로 실행:
```bash
docker-compose up -d kafka
```

### 포트 충돌
각 서비스의 `application.yml`에서 포트 변경 가능

## 라이센스
학습용 프로젝트
