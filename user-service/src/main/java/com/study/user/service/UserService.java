package com.study.user.service;

import com.study.common.exception.BusinessException;
import com.study.user.domain.User;
import com.study.user.domain.UserRole;
import com.study.user.dto.UserDto;
import com.study.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * User Service
 *
 * 학습 포인트:
 * - @Transactional을 활용한 트랜잭션 관리
 * - 비즈니스 로직 처리
 * - 예외 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입
     */
    @Transactional
    public UserDto.Response signUp(UserDto.SignUpRequest request) {
        // 중복 검증
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("이미 존재하는 사용자명입니다", "USER_DUPLICATE_USERNAME");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("이미 존재하는 이메일입니다", "USER_DUPLICATE_EMAIL");
        }

        // 엔티티 생성 및 저장
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .role(UserRole.USER)
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("새로운 사용자 가입: {}", savedUser.getUsername());

        return UserDto.Response.from(savedUser);
    }

    /**
     * 사용자 조회
     */
    public UserDto.Response getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("사용자를 찾을 수 없습니다", "USER_NOT_FOUND"));
        return UserDto.Response.from(user);
    }

    /**
     * 사용자명으로 조회
     */
    public UserDto.Response getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("사용자를 찾을 수 없습니다", "USER_NOT_FOUND"));
        return UserDto.Response.from(user);
    }

    /**
     * 전체 사용자 조회
     */
    public List<UserDto.Response> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserDto.Response::from)
                .collect(Collectors.toList());
    }

    /**
     * 프로필 수정
     */
    @Transactional
    public UserDto.Response updateProfile(Long id, UserDto.UpdateProfileRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("사용자를 찾을 수 없습니다", "USER_NOT_FOUND"));

        // 이메일 중복 검증 (자신 제외)
        userRepository.findByEmail(request.getEmail())
                .ifPresent(existingUser -> {
                    if (!existingUser.getId().equals(id)) {
                        throw new BusinessException("이미 사용 중인 이메일입니다", "USER_DUPLICATE_EMAIL");
                    }
                });

        user.updateProfile(request.getName(), request.getEmail());
        log.info("사용자 프로필 수정: {}", user.getUsername());

        return UserDto.Response.from(user);
    }

    /**
     * 사용자 비활성화
     */
    @Transactional
    public void disableUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("사용자를 찾을 수 없습니다", "USER_NOT_FOUND"));

        user.disable();
        log.info("사용자 비활성화: {}", user.getUsername());
    }
}
