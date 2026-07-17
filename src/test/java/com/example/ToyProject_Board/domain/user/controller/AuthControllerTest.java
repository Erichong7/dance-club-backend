package com.example.ToyProject_Board.domain.user.controller;

import com.example.ToyProject_Board.domain.support.ControllerTestSupport;
import com.example.ToyProject_Board.domain.user.dto.TokenResponse;
import com.example.ToyProject_Board.domain.user.service.AuthService;
import com.example.ToyProject_Board.global.security.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
public class AuthControllerTest extends ControllerTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("회원가입 성공")
    void 회원가입_성공() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "test@test.com",
                                    "password": "password123",
                                    "nickname": "테스터"
                                }
                                """))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 형식 오류")
    void 이메일_형식_오류로_회원가입_실패() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "not-an-email",
                                    "password": "password123",
                                    "nickname": "테스터"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 실패 - 비밀번호 최소 길이 미달")
    void 짧은_비밀번호로_회원가입_실패() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "test@test.com",
                                    "password": "1234567",
                                    "nickname": "테스터"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("로그인 성공")
    void 로그인_성공() throws Exception {
        given(authService.login(any())).willReturn(new TokenResponse("access_token", "refresh_token"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "test@test.com",
                                    "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access_token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh_token"))
                .andDo(print());
    }

    @Test
    @DisplayName("토큰 재발급 성공")
    void 토큰_재발급_성공() throws Exception {
        given(authService.reissue(any())).willReturn(new TokenResponse("new_access", "new_refresh"));

        mockMvc.perform(post("/api/auth/reissue")
                        .header("Refresh-Token", "valid_refresh_token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new_access"))
                .andExpect(jsonPath("$.refreshToken").value("new_refresh"))
                .andDo(print());
    }

    @Test
    @DisplayName("로그아웃 성공")
    void 로그아웃_성공() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 승인 성공")
    void 회원가입_승인_성공() throws Exception {
        mockMvc.perform(patch("/api/auth/2/approve")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andDo(print());

        verify(authService).approve(1L, 2L);
    }

    @Test
    @DisplayName("회원가입 거절 성공")
    void 회원가입_거절_성공() throws Exception {
        mockMvc.perform(patch("/api/auth/2/reject")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andDo(print());

        verify(authService).reject(1L, 2L);
    }
}