package com.study.order.service;

import com.study.common.exception.BusinessException;
import com.study.order.domain.Order;
import com.study.order.domain.OrderItem;
import com.study.order.domain.OrderStatus;
import com.study.order.dto.OrderDto;
import com.study.order.event.OrderEvent;
import com.study.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Order Service
 *
 * 학습 포인트:
 * 1. Redis 캐싱: @Cacheable, @CacheEvict, @CachePut
 * 2. Kafka 이벤트 발행: Event-Driven Architecture
 * 3. 트랜잭션 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String ORDER_TOPIC = "order-events";
    private static final String ORDER_CACHE = "orders";

    /**
     * 주문 생성
     */
    @Transactional
    public OrderDto.Response createOrder(OrderDto.CreateRequest request) {
        // Order 엔티티 생성
        Order order = Order.builder()
                .userId(request.getUserId())
                .status(OrderStatus.PENDING)
                .build();

        // OrderItem 추가
        request.getItems().forEach(itemRequest -> {
            OrderItem orderItem = OrderItem.builder()
                    .productId(itemRequest.getProductId())
                    .productName(itemRequest.getProductName())
                    .quantity(itemRequest.getQuantity())
                    .price(itemRequest.getPrice())
                    .build();
            order.addOrderItem(orderItem);
        });

        // 총액 계산
        order.calculateTotalAmount();

        // 저장
        Order savedOrder = orderRepository.save(order);
        log.info("주문 생성: id={}, userId={}, totalAmount={}",
                savedOrder.getId(), savedOrder.getUserId(), savedOrder.getTotalAmount());

        // Kafka 이벤트 발행
        publishOrderEvent("ORDER_CREATED", savedOrder);

        return OrderDto.Response.from(savedOrder);
    }

    /**
     * 주문 조회 - 캐시 적용
     */
    @Cacheable(value = ORDER_CACHE, key = "#orderId")
    public OrderDto.Response getOrder(Long orderId) {
        log.info("Cache Miss - DB에서 주문 조회: orderId={}", orderId);
        Order order = orderRepository.findByIdWithItems(orderId);
        if (order == null) {
            throw new BusinessException("주문을 찾을 수 없습니다", "ORDER_NOT_FOUND");
        }
        return OrderDto.Response.from(order);
    }

    /**
     * 사용자별 주문 조회
     */
    public List<OrderDto.Response> getUserOrders(Long userId) {
        List<Order> orders = orderRepository.findByUserIdWithItems(userId);
        return orders.stream()
                .map(OrderDto.Response::from)
                .collect(Collectors.toList());
    }

    /**
     * 전체 주문 조회
     */
    public List<OrderDto.Response> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderDto.Response::from)
                .collect(Collectors.toList());
    }

    /**
     * 주문 상태 변경 - 캐시 업데이트
     */
    @Transactional
    @CachePut(value = ORDER_CACHE, key = "#orderId")
    public OrderDto.Response updateOrderStatus(Long orderId, OrderDto.StatusUpdateRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("주문을 찾을 수 없습니다", "ORDER_NOT_FOUND"));

        OrderStatus oldStatus = order.getStatus();
        order.updateStatus(request.getStatus());

        log.info("주문 상태 변경: orderId={}, {} -> {}",
                orderId, oldStatus, request.getStatus());

        // Kafka 이벤트 발행
        publishOrderEvent("ORDER_STATUS_UPDATED", order);

        return OrderDto.Response.from(order);
    }

    /**
     * 주문 취소 - 캐시 삭제
     */
    @Transactional
    @CacheEvict(value = ORDER_CACHE, key = "#orderId")
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("주문을 찾을 수 없습니다", "ORDER_NOT_FOUND"));

        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new BusinessException("배송 중이거나 완료된 주문은 취소할 수 없습니다", "ORDER_CANNOT_CANCEL");
        }

        order.updateStatus(OrderStatus.CANCELLED);
        log.info("주문 취소: orderId={}", orderId);

        // Kafka 이벤트 발행
        publishOrderEvent("ORDER_CANCELLED", order);
    }

    /**
     * Kafka 이벤트 발행
     */
    private void publishOrderEvent(String eventType, Order order) {
        OrderEvent event = OrderEvent.builder()
                .eventType(eventType)
                .orderId(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .build();

        kafkaTemplate.send(ORDER_TOPIC, event);
        log.info("Kafka 이벤트 발행: topic={}, eventType={}, orderId={}",
                ORDER_TOPIC, eventType, order.getId());
    }
}
