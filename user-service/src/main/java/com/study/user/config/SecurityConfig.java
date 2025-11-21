package com.study.user.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 설정
 *
 * 학습 포인트:
 * - SecurityFilterChain 기반 설정 (Spring Security 6.x)
 * - HTTP 보안 설정
 * - CORS, CSRF 설정
 * - Session 관리 (Stateless)
 * - Password Encoding
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * Security Filter Chain 설정
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (REST API이므로)
                .csrf(AbstractHttpConfigurer::disable)

                // CORS 설정
                .cors(AbstractHttpConfigurer::disable)

                // 세션 관리 - Stateless (JWT 사용 시)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 인증/인가 설정
                .authorizeHttpRequests(auth -> auth
                        // 회원가입, H2 콘솔은 인증 없이 접근 가능
                        .requestMatchers("/signup", "/h2-console/**").permitAll()
                        // 그 외 모든 요청은 인증 필요 (학습용으로 일단 모두 허용)
                        .anyRequest().permitAll()
                )

                // H2 콘솔 사용을 위한 설정
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin()));

        return http.build();
    }

    /**
     * Password Encoder - BCrypt 사용
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
