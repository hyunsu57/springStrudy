package com.study.user.repository;

import com.study.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * User Repository
 *
 * JPA 학습 포인트:
 * - JpaRepository 기본 메서드 (save, findById, findAll, delete 등)
 * - 메서드 이름 기반 쿼리 (findByUsername, existsByEmail 등)
 * - @Query를 통한 JPQL 작성
 * - Optional을 활용한 null 안전성
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 메서드 이름 기반 쿼리
     */
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    /**
     * JPQL 쿼리
     */
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.enabled = true")
    Optional<User> findActiveUserByUsername(@Param("username") String username);
}
