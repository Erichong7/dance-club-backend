package com.example.ToyProject_Board.domain.user;

import com.example.ToyProject_Board.domain.user.dto.LoginRequest;
import com.example.ToyProject_Board.domain.user.dto.SignupRequest;
import com.example.ToyProject_Board.domain.user.dto.TokenResponse;
import com.example.ToyProject_Board.domain.user.repository.UserRepository;
import com.example.ToyProject_Board.domain.user.service.UserService;
import com.example.ToyProject_Board.global.jwt.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    private SignupRequest signupRequest(String email, String password, String nickname) {
        SignupRequest request = new SignupRequest();
        ReflectionTestUtils.setField(request, "email", email);
        ReflectionTestUtils.setField(request, "password", password);
        ReflectionTestUtils.setField(request, "nickname", nickname);
        return request;
    }

    private LoginRequest loginRequest(String email, String password) {
        LoginRequest request = new LoginRequest();
        ReflectionTestUtils.setField(request, "email", email);
        ReflectionTestUtils.setField(request, "password", password);
        return request;
    }

    @Test
    @DisplayName("회원가입 성공")
    void 회원가입_성공() {
        // given
        SignupRequest request = signupRequest("test@test.com", "password123", "테스터");
        given(userRepository.existsByEmail("test@test.com")).willReturn(false);
        given(passwordEncoder.encode("password123")).willReturn("encoded_pw");

        // when & then
        assertThatCode(() -> userService.signup(request)).doesNotThrowAnyException();
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void 이메일_중복으로_회원가입_실패() {
        // given
        SignupRequest request = signupRequest("test@test.com", "password123", "테스터");
        given(userRepository.existsByEmail("test@test.com")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.signup(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("이미 사용중인 이메일입니다");
    }

    @Test
    @DisplayName("로그인 성공")
    void 로그인_성공() {
        // given
        LoginRequest request = loginRequest("test@test.com", "password123");
        User user = UserFixture.createWithId(1L);

        given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("password123", user.getPassword())).willReturn(true);
        given(jwtUtil.generateAccessToken(1L)).willReturn("access_token");
        given(jwtUtil.generateRefreshToken(1L)).willReturn("refresh_token");

        // when
        TokenResponse response = userService.login(request);

        // then
        assertThat(response.getAccessToken()).isEqualTo("access_token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh_token");
    }

    @Test
    @DisplayName("로그인 실패 - 이메일 없음")
    void 존재하지_않는_이메일로_로그인_실패() {
        // given
        LoginRequest request = loginRequest("notfound@test.com", "password123");
        given(userRepository.findByEmail("notfound@test.com")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("이메일 또는 비밀번호가 틀렸습니다");
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 틀림")
    void 비밀번호_불일치로_로그인_실패() {
        // given
        LoginRequest request = loginRequest("test@test.com", "wrong_password");
        User user = UserFixture.createWithId(1L);

        given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrong_password", user.getPassword())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("이메일 또는 비밀번호가 틀렸습니다");
    }

    @Test
    @DisplayName("토큰 재발급 성공")
    void 토큰_재발급_성공() {
        // given
        User user = UserFixture.createWithId(1L);
        user.updateRefreshToken("valid_refresh_token");

        given(jwtUtil.validateToken("valid_refresh_token")).willReturn(true);
        given(jwtUtil.getUserId("valid_refresh_token")).willReturn(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(jwtUtil.generateAccessToken(1L)).willReturn("new_access_token");
        given(jwtUtil.generateRefreshToken(1L)).willReturn("new_refresh_token");

        // when
        TokenResponse response = userService.reissue("valid_refresh_token");

        // then
        assertThat(response.getAccessToken()).isEqualTo("new_access_token");
        assertThat(response.getRefreshToken()).isEqualTo("new_refresh_token");
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 유효하지 않은 토큰")
    void 유효하지_않은_토큰으로_재발급_실패() {
        // given
        given(jwtUtil.validateToken("invalid_token")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.reissue("invalid_token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("유효하지 않은 토큰입니다");
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 저장된 토큰과 불일치")
    void 저장된_토큰과_불일치로_재발급_실패() {
        // given
        User user = UserFixture.createWithId(1L);
        user.updateRefreshToken("stored_token");

        given(jwtUtil.validateToken("other_token")).willReturn(true);
        given(jwtUtil.getUserId("other_token")).willReturn(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> userService.reissue("other_token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("토큰이 일치하지 않습니다");
    }

    @Test
    @DisplayName("로그아웃 성공")
    void 로그아웃_성공() {
        // given
        User user = UserFixture.createWithId(1L);
        user.updateRefreshToken("some_token");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        // when & then
        assertThatCode(() -> userService.logout(1L)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("로그아웃 실패 - 유저 없음")
    void 존재하지_않는_유저의_로그아웃_실패() {
        // given
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.logout(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("유저를 찾을 수 없습니다");
    }
}