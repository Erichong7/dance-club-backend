package com.example.ToyProject_Board.domain.support;

import com.example.ToyProject_Board.global.jwt.JwtAuthenticationFilter;
import com.example.ToyProject_Board.global.logging.RequestLoggingFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

public class ControllerTestSupport {

    @MockitoBean
    protected JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    protected RequestLoggingFilter requestLoggingFilter;

    @BeforeEach
    void bypassJwtFilter() throws Exception {
        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            // 기본 인증 설정 — 테스트에서 setAuthentication()으로 덮어쓸 수 있음
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(1L, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());

        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(requestLoggingFilter).doFilter(any(), any(), any());
    }

    // 인증된 사용자로 만들고 싶을 때 호출
    protected void setAuthentication(Long userId) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
