package com.example.ToyProject_Board.domain.user;

import com.example.ToyProject_Board.domain.user.dto.response.TokenResponse;
import com.example.ToyProject_Board.domain.user.repository.UserRepository;
import com.example.ToyProject_Board.domain.user.service.AuthService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AuthServiceTest는 Mockito 기반이라 엔티티 변경이 실제 DB에 반영되는지 확인할 수 없어,
 * 리프레시 토큰 저장(RTR/로그아웃)을 실제 DB(H2)로 검증하는 통합 테스트를 별도 작성.
 * 트랜잭션 경계를 서비스에 맡기기 위해 테스트 클래스에 @Transactional을 붙이지 않는다.
 */
@SpringBootTest
class AuthServiceIntegrationTest {

    @Autowired
    private AuthService authService;
    @Autowired
    private UserRepository userRepository;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Test
    @DisplayName("토큰 재발급 시 새 리프레시 토큰이 DB에 저장된다")
    void 토큰_재발급시_새_리프레시_토큰이_DB에_저장된다() {
        // given
        User user = userRepository.save(UserFixture.create("reissue@test.com", "재발급", SignupStatus.APPROVED));
        String oldRefreshToken = issuedOneMinuteAgo(user.getId());
        user.updateRefreshToken(oldRefreshToken);
        userRepository.save(user);

        // when
        TokenResponse response = authService.reissue(oldRefreshToken);

        // then
        String stored = userRepository.findById(user.getId()).orElseThrow().getRefreshToken();
        assertThat(response.getRefreshToken()).isNotEqualTo(oldRefreshToken);
        assertThat(stored).isEqualTo(response.getRefreshToken());
    }

    @Test
    @DisplayName("로그아웃 시 리프레시 토큰이 DB에서 삭제된다")
    void 로그아웃시_리프레시_토큰이_DB에서_삭제된다() {
        // given
        User user = userRepository.save(UserFixture.create("logout@test.com", "로그아웃", SignupStatus.APPROVED));
        user.updateRefreshToken(issuedOneMinuteAgo(user.getId()));
        userRepository.save(user);

        // when
        authService.logout(user.getId());

        // then
        assertThat(userRepository.findById(user.getId()).orElseThrow().getRefreshToken()).isNull();
    }

    // JWT의 iat는 초 단위라 같은 초에 발급하면 재발급 토큰과 문자열이 같아지므로, 1분 전에 발급된 토큰을 만든다
    private String issuedOneMinuteAgo(Long userId) {
        long issuedAt = System.currentTimeMillis() - 60_000;
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(new Date(issuedAt))
                .expiration(new Date(issuedAt + refreshExpiration))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
