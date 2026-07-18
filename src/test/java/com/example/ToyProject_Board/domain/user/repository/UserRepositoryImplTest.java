package com.example.ToyProject_Board.domain.user.repository;

import com.example.ToyProject_Board.domain.user.SignupStatus;
import com.example.ToyProject_Board.domain.user.User;
import com.example.ToyProject_Board.domain.user.UserFixture;
import com.example.ToyProject_Board.domain.user.dto.request.UserSearchRequest;
import com.example.ToyProject_Board.global.config.JpaConfig;
import com.example.ToyProject_Board.global.config.QuerydslConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QuerydslConfig.class, JpaConfig.class})
class UserRepositoryImplTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.save(UserFixture.create("hong@test.com", "홍길동", SignupStatus.APPROVED));
        userRepository.save(UserFixture.create("kim@test.com", "김철수", SignupStatus.APPROVED));
        userRepository.save(UserFixture.create("lee@naver.com", "홍당무", SignupStatus.REQUESTED));
    }

    @Test
    @DisplayName("닉네임 키워드로 부분 일치 검색")
    void 닉네임_키워드로_검색() {
        // given
        UserSearchRequest request = new UserSearchRequest("홍", null, null);

        // when
        Page<User> result = userRepository.searchUsers(request, PageRequest.of(0, 10));

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).extracting(User::getNickname)
                .containsExactlyInAnyOrder("홍길동", "홍당무");
    }

    @Test
    @DisplayName("이메일 키워드로 부분 일치 검색")
    void 이메일_키워드로_검색() {
        // given
        UserSearchRequest request = new UserSearchRequest(null, "naver", null);

        // when
        Page<User> result = userRepository.searchUsers(request, PageRequest.of(0, 10));

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("lee@naver.com");
    }

    @Test
    @DisplayName("가입 상태로 검색")
    void 가입_상태로_검색() {
        // given
        UserSearchRequest request = new UserSearchRequest(null, null, SignupStatus.REQUESTED);

        // when
        Page<User> result = userRepository.searchUsers(request, PageRequest.of(0, 10));

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getNickname()).isEqualTo("홍당무");
    }

    @Test
    @DisplayName("조건이 없으면 전체 조회")
    void 조건이_없으면_전체_조회() {
        // given
        UserSearchRequest request = new UserSearchRequest();

        // when
        Page<User> result = userRepository.searchUsers(request, PageRequest.of(0, 10));

        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("빈 문자열 키워드는 조건 없음으로 처리")
    void 빈_문자열_키워드는_조건_없음으로_처리() {
        // given
        UserSearchRequest request = new UserSearchRequest("", " ", null);

        // when
        Page<User> result = userRepository.searchUsers(request, PageRequest.of(0, 10));

        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("복합 조건 검색 - 닉네임 + 가입 상태")
    void 복합_조건_검색() {
        // given
        UserSearchRequest request = new UserSearchRequest("홍", null, SignupStatus.APPROVED);

        // when
        Page<User> result = userRepository.searchUsers(request, PageRequest.of(0, 10));

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getNickname()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("페이징 동작 확인")
    void 페이징_동작_확인() {
        // given
        UserSearchRequest request = new UserSearchRequest();

        // when
        Page<User> result = userRepository.searchUsers(request, PageRequest.of(0, 2));

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getContent()).allSatisfy(user -> assertThat(user.getCreatedAt()).isNotNull());
    }
}
