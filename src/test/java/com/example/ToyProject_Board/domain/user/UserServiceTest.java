package com.example.ToyProject_Board.domain.user;

import com.example.ToyProject_Board.domain.team.Team;
import com.example.ToyProject_Board.domain.team.TeamMember;
import com.example.ToyProject_Board.domain.team.TeamMemberRole;
import com.example.ToyProject_Board.domain.team.repository.TeamMemberRepository;
import com.example.ToyProject_Board.domain.user.dto.UserDetailResponse;
import com.example.ToyProject_Board.domain.user.repository.UserRepository;
import com.example.ToyProject_Board.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    private Team teamWithId(Long id, String name) {
        Team team = Team.builder().name(name).build();
        ReflectionTestUtils.setField(team, "id", id);
        return team;
    }

    @Test
    @DisplayName("내 정보 조회 성공 - 소속 팀 포함")
    void 내_정보_조회_성공() {
        // given
        User user = UserFixture.createWithId(1L);
        Team team = teamWithId(10L, "댄스팀");
        TeamMember teamMember = TeamMember.builder().team(team).user(user).role(TeamMemberRole.MEMBER).build();

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(teamMemberRepository.findByUser(user)).willReturn(List.of(teamMember));

        // when
        UserDetailResponse response = userService.getMyInfo(1L);

        // then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo(user.getEmail());
        assertThat(response.getNickName()).isEqualTo(user.getNickname());
        assertThat(response.getRole()).isEqualTo(user.getRole());
        assertThat(response.getTeamIds()).containsExactly(10L);
        assertThat(response.getTeamNames()).containsExactly("댄스팀");
    }

    @Test
    @DisplayName("내 정보 조회 실패 - 유저 없음")
    void 존재하지_않는_유저의_정보_조회_실패() {
        // given
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getMyInfo(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("유저를 찾을 수 없습니다");
    }
}