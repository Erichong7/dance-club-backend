package com.example.ToyProject_Board.domain.team;

import com.example.ToyProject_Board.domain.team.dto.request.AddMemberRequest;
import com.example.ToyProject_Board.domain.team.dto.request.TeamCreateRequest;
import com.example.ToyProject_Board.domain.team.dto.response.TeamMemberResponse;
import com.example.ToyProject_Board.domain.team.dto.response.TeamResponse;
import com.example.ToyProject_Board.domain.team.repository.TeamMemberRepository;
import com.example.ToyProject_Board.domain.team.repository.TeamRepository;
import com.example.ToyProject_Board.domain.team.service.TeamService;
import com.example.ToyProject_Board.domain.user.User;
import com.example.ToyProject_Board.domain.user.UserFixture;
import com.example.ToyProject_Board.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @InjectMocks
    private TeamService teamService;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    void createTeamSuccess() {
        User admin = UserFixture.createAdminWithId(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(admin));
        given(teamRepository.existsByName("신규팀")).willReturn(false);

        Team savedTeam = TeamFixture.createWithNameAndId("신규팀", 10L);
        given(teamRepository.save(any())).willReturn(savedTeam);

        TeamCreateRequest request = new TeamCreateRequest();
        ReflectionTestUtils.setField(request, "name", "신규팀");

        TeamResponse response = teamService.createTeam(request, 1L);

        assertThat(response.getName()).isEqualTo("신규팀");
    }

    @Test
    void createTeamFail_notAdmin() {
        User user = UserFixture.createWithId(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        TeamCreateRequest request = new TeamCreateRequest();
        ReflectionTestUtils.setField(request, "name", "신규팀");

        assertThatThrownBy(() -> teamService.createTeam(request, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("관리자 권한");
    }

    @Test
    void createTeamFail_duplicateName() {
        User admin = UserFixture.createAdminWithId(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(admin));
        given(teamRepository.existsByName("중복팀")).willReturn(true);

        TeamCreateRequest request = new TeamCreateRequest();
        ReflectionTestUtils.setField(request, "name", "중복팀");

        assertThatThrownBy(() -> teamService.createTeam(request, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("이미 존재하는 팀 이름");
    }

    @Test
    void addMemberSuccess() {
        User admin = UserFixture.createAdminWithId(1L);
        User targetUser = UserFixture.createWithId(2L);
        Team team = TeamFixture.createWithId(10L);

        given(userRepository.findById(1L)).willReturn(Optional.of(admin));
        given(teamRepository.findById(10L)).willReturn(Optional.of(team));
        given(userRepository.findById(2L)).willReturn(Optional.of(targetUser));
        given(teamMemberRepository.existsByTeamAndUser(team, targetUser)).willReturn(false);

        TeamMember savedMember = TeamMember.builder()
                .team(team).user(targetUser).role(TeamMemberRole.MEMBER).build();
        ReflectionTestUtils.setField(savedMember, "id", 100L);
        given(teamMemberRepository.save(any())).willReturn(savedMember);

        AddMemberRequest request = new AddMemberRequest();
        ReflectionTestUtils.setField(request, "userId", 2L);
        ReflectionTestUtils.setField(request, "role", TeamMemberRole.MEMBER);

        TeamMemberResponse response = teamService.addMember(10L, request, 1L);

        assertThat(response.getRole()).isEqualTo(TeamMemberRole.MEMBER);
        assertThat(response.getNickname()).isEqualTo("테스터");
    }

    @Test
    void addMemberFail_alreadyLeaderExists() {
        User admin = UserFixture.createAdminWithId(1L);
        User targetUser = UserFixture.createWithId(2L);
        Team team = TeamFixture.createWithId(10L);

        given(userRepository.findById(1L)).willReturn(Optional.of(admin));
        given(teamRepository.findById(10L)).willReturn(Optional.of(team));
        given(userRepository.findById(2L)).willReturn(Optional.of(targetUser));
        given(teamMemberRepository.existsByTeamAndUser(team, targetUser)).willReturn(false);
        given(teamMemberRepository.existsByTeamAndRole(team, TeamMemberRole.LEADER)).willReturn(true);

        AddMemberRequest request = new AddMemberRequest();
        ReflectionTestUtils.setField(request, "userId", 2L);
        ReflectionTestUtils.setField(request, "role", TeamMemberRole.LEADER);

        assertThatThrownBy(() -> teamService.addMember(10L, request, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("이미 팀장이 존재");
    }

    @Test
    void addMemberFail_alreadyMember() {
        User admin = UserFixture.createAdminWithId(1L);
        User targetUser = UserFixture.createWithId(2L);
        Team team = TeamFixture.createWithId(10L);

        given(userRepository.findById(1L)).willReturn(Optional.of(admin));
        given(teamRepository.findById(10L)).willReturn(Optional.of(team));
        given(userRepository.findById(2L)).willReturn(Optional.of(targetUser));
        given(teamMemberRepository.existsByTeamAndUser(team, targetUser)).willReturn(true);

        AddMemberRequest request = new AddMemberRequest();
        ReflectionTestUtils.setField(request, "userId", 2L);
        ReflectionTestUtils.setField(request, "role", TeamMemberRole.MEMBER);

        assertThatThrownBy(() -> teamService.addMember(10L, request, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("이미 팀에 속해있는");
    }
}
