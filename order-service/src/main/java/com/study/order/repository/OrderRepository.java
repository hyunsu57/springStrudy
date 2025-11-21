package com.study.order.repository;

import com.study.order.domain.Order;
import com.study.order.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Order Repository
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * 사용자별 주문 조회
     */
    List<Order> findByUserId(Long userId);

    /**
     * 상태별 주문 조회
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * 사용자별 + 상태별 주문 조회
     */
    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    /**
     * N+1 문제 해결: OrderItem을 fetch join으로 함께 조회
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = :orderId")
    Order findByIdWithItems(@Param("orderId") Long orderId);

    /**
     * 사용자의 주문을 OrderItem과 함께 조회
     */
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.userId = :userId")
    List<Order> findByUserIdWithItems(@Param("userId") Long userId);
}
