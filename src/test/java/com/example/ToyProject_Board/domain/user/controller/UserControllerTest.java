package com.example.ToyProject_Board.domain.user.controller;

import com.example.ToyProject_Board.domain.support.ControllerTestSupport;
import com.example.ToyProject_Board.domain.user.SignupStatus;
import com.example.ToyProject_Board.domain.user.UserRole;
import com.example.ToyProject_Board.domain.user.dto.request.UserSearchRequest;
import com.example.ToyProject_Board.domain.user.dto.response.UserDetailResponse;
import com.example.ToyProject_Board.domain.user.dto.response.UserSearchResponse;
import com.example.ToyProject_Board.domain.user.service.UserService;
import com.example.ToyProject_Board.global.exception.BusinessException;
import com.example.ToyProject_Board.global.exception.ErrorCode;
import com.example.ToyProject_Board.global.security.JsonAccessDeniedHandler;
import com.example.ToyProject_Board.global.security.JsonAuthenticationEntryPoint;
import com.example.ToyProject_Board.global.security.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, JsonAuthenticationEntryPoint.class, JsonAccessDeniedHandler.class})
public class UserControllerTest extends ControllerTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("내 정보 조회 성공")
    void 내_정보_조회_성공() throws Exception {
        UserDetailResponse response = new UserDetailResponse(
                1L, "test@test.com", "테스터", UserRole.USER, List.of(10L), List.of("댄스팀"));
        given(userService.getMyInfo(1L)).willReturn(response);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.nickName").value("테스터"))
                .andExpect(jsonPath("$.teamIds[0]").value(10L))
                .andExpect(jsonPath("$.teamNames[0]").value("댄스팀"))
                .andDo(print());
    }

    @Test
    @DisplayName("회원 검색 성공 - 쿼리 파라미터 바인딩 확인")
    void 회원_검색_성공() throws Exception {
        UserSearchResponse response = new UserSearchResponse(
                2L, "target@test.com", "테스터", "010-1234-5678", UserRole.USER, SignupStatus.APPROVED,
                LocalDateTime.of(2026, 7, 1, 12, 0));
        given(userService.searchUsers(eq(1L), any(UserSearchRequest.class), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/users/search")
                        .param("nickname", "테스")
                        .param("signupStatus", "APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(2L))
                .andExpect(jsonPath("$.content[0].email").value("target@test.com"))
                .andExpect(jsonPath("$.content[0].nickName").value("테스터"))
                .andExpect(jsonPath("$.content[0].signupStatus").value("APPROVED"))
                .andDo(print());

        ArgumentCaptor<UserSearchRequest> captor = ArgumentCaptor.forClass(UserSearchRequest.class);
        then(userService).should().searchUsers(eq(1L), captor.capture(), any(Pageable.class));
        assertThat(captor.getValue().getNickname()).isEqualTo("테스");
        assertThat(captor.getValue().getEmail()).isNull();
        assertThat(captor.getValue().getSignupStatus()).isEqualTo(SignupStatus.APPROVED);
    }

    @Test
    @DisplayName("회원 삭제 성공")
    void 회원_삭제_성공() throws Exception {
        mockMvc.perform(delete("/api/users/2"))
                .andExpect(status().isNoContent())
                .andDo(print());

        then(userService).should().deleteUser(1L, 2L);
    }

    @Test
    @DisplayName("회원 삭제 실패 - 관리자 아님")
    void 회원_삭제_실패_관리자_아님() throws Exception {
        doThrow(new BusinessException(ErrorCode.ADMIN_ONLY)).when(userService).deleteUser(1L, 2L);

        mockMvc.perform(delete("/api/users/2"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("U002"))
                .andExpect(jsonPath("$.message").value("관리자 권한이 필요합니다"))
                .andDo(print());
    }

    @Test
    @DisplayName("회원 삭제 실패 - 본인 계정 삭제 시도")
    void 회원_삭제_실패_본인_삭제() throws Exception {
        doThrow(new BusinessException(ErrorCode.SELF_DELETE_FORBIDDEN)).when(userService).deleteUser(1L, 1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("U010"))
                .andExpect(jsonPath("$.message").value("본인 계정은 삭제할 수 없습니다"))
                .andDo(print());
    }
}