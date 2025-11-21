package com.study.user.controller;

import com.study.common.dto.ApiResponse;
import com.study.user.dto.UserDto;
import com.study.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User Controller
 *
 * 학습 포인트:
 * - RESTful API 설계 원칙
 * - HTTP 메서드 (GET, POST, PUT, DELETE)
 * - @Valid를 통한 입력 검증
 * - ResponseEntity를 통한 HTTP 응답 제어
 */
@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserDto.Response> signUp(@Valid @RequestBody UserDto.SignUpRequest request) {
        log.info("회원가입 요청: username={}", request.getUsername());
        UserDto.Response response = userService.signUp(request);
        return ApiResponse.success("회원가입이 완료되었습니다", response);
    }

    /**
     * 전체 사용자 조회
     */
    @GetMapping("/users")
    public ApiResponse<List<UserDto.Response>> getAllUsers() {
        log.info("전체 사용자 조회 요청");
        List<UserDto.Response> users = userService.getAllUsers();
        return ApiResponse.success(users);
    }

    /**
     * 사용자 ID로 조회
     */
    @GetMapping("/users/{id}")
    public ApiResponse<UserDto.Response> getUserById(@PathVariable Long id) {
        log.info("사용자 조회 요청: id={}", id);
        UserDto.Response user = userService.getUserById(id);
        return ApiResponse.success(user);
    }

    /**
     * 사용자명으로 조회
     */
    @GetMapping("/users/username/{username}")
    public ApiResponse<UserDto.Response> getUserByUsername(@PathVariable String username) {
        log.info("사용자 조회 요청: username={}", username);
        UserDto.Response user = userService.getUserByUsername(username);
        return ApiResponse.success(user);
    }

    /**
     * 프로필 수정
     */
    @PutMapping("/users/{id}")
    public ApiResponse<UserDto.Response> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody UserDto.UpdateProfileRequest request) {
        log.info("프로필 수정 요청: id={}", id);
        UserDto.Response user = userService.updateProfile(id, request);
        return ApiResponse.success("프로필이 수정되었습니다", user);
    }

    /**
     * 사용자 비활성화
     */
    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disableUser(@PathVariable Long id) {
        log.info("사용자 비활성화 요청: id={}", id);
        userService.disableUser(id);
    }
}
