package com.example.ToyProject_Board.domain.user.controller;

import com.example.ToyProject_Board.domain.support.ControllerTestSupport;
import com.example.ToyProject_Board.domain.user.dto.response.TokenResponse;
import com.example.ToyProject_Board.domain.user.service.AuthService;
import com.example.ToyProject_Board.global.exception.BusinessException;
import com.example.ToyProject_Board.global.exception.ErrorCode;
import com.example.ToyProject_Board.global.security.JsonAccessDeniedHandler;
import com.example.ToyProject_Board.global.security.JsonAuthenticationEntryPoint;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JsonAuthenticationEntryPoint.class, JsonAccessDeniedHandler.class})
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
                                    "passwordConfirm": "password123",
                                    "nickname": "테스터",
                                    "phoneNumber": "010-1234-5678"
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
                                    "passwordConfirm": "password123",
                                    "nickname": "테스터",
                                    "phoneNumber": "010-1234-5678"
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
                                    "passwordConfirm": "1234567",
                                    "nickname": "테스터",
                                    "phoneNumber": "010-1234-5678"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 실패 - 전화번호 형식 오류")
    void 전화번호_형식_오류로_회원가입_실패() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "test@test.com",
                                    "password": "password123",
                                    "passwordConfirm": "password123",
                                    "nickname": "테스터",
                                    "phoneNumber": "01012345678"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 실패 - 비밀번호 확인 불일치")
    void 비밀번호_확인_불일치로_회원가입_실패() throws Exception {
        doThrow(new BusinessException(ErrorCode.PASSWORD_CONFIRM_MISMATCH)).when(authService).signup(any());

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "test@test.com",
                                    "password": "password123",
                                    "passwordConfirm": "password456",
                                    "nickname": "테스터",
                                    "phoneNumber": "010-1234-5678"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("U009"))
                .andExpect(jsonPath("$.message").value("비밀번호가 일치하지 않습니다"))
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
    @DisplayName("로그인 실패 - 자격 불일치")
    void 로그인_실패_자격_불일치() throws Exception {
        given(authService.login(any())).willThrow(new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "test@test.com",
                                    "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("U004"))
                .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 틀렸습니다"))
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
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 승인 성공")
    void 회원가입_승인_성공() throws Exception {
        mockMvc.perform(patch("/api/auth/2/approve"))
                .andExpect(status().isOk())
                .andDo(print());

        verify(authService).approve(1L, 2L);
    }

    @Test
    @DisplayName("회원가입 거절 성공")
    void 회원가입_거절_성공() throws Exception {
        mockMvc.perform(patch("/api/auth/2/reject"))
                .andExpect(status().isOk())
                .andDo(print());

        verify(authService).reject(1L, 2L);
    }
}