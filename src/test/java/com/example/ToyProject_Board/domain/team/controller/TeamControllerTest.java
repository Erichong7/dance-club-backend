package com.example.ToyProject_Board.domain.team.controller;

import com.example.ToyProject_Board.domain.support.ControllerTestSupport;
import com.example.ToyProject_Board.domain.team.TeamMemberRole;
import com.example.ToyProject_Board.domain.team.dto.response.TeamDetailResponse;
import com.example.ToyProject_Board.domain.team.dto.response.TeamMemberResponse;
import com.example.ToyProject_Board.domain.team.dto.response.TeamResponse;
import com.example.ToyProject_Board.domain.team.service.TeamService;
import com.example.ToyProject_Board.global.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import tools.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TeamController.class)
@Import(SecurityConfig.class)
class TeamControllerTest extends ControllerTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TeamService teamService;

    @Test
    void 팀_생성_성공() throws Exception {
        TeamResponse response = new TeamResponse(
                buildTeamStub(1L, "A팀", LocalDateTime.now()));
        given(teamService.createTeam(any(), eq(1L))).willReturn(response);

        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "A팀")))
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("A팀"));
    }

    @Test
    void 빈_이름으로_팀_생성_실패() throws Exception {
        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", ""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 전체_팀_목록_조회_성공() throws Exception {
        given(teamService.getAllTeams()).willReturn(List.of());

        mockMvc.perform(get("/api/teams"))
                .andExpect(status().isOk());
    }

    @Test
    void 팀_상세_조회_성공() throws Exception {
        TeamDetailResponse response = buildTeamDetailStub(1L, "A팀");
        given(teamService.getTeam(1L)).willReturn(response);

        mockMvc.perform(get("/api/teams/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void 팀원_추가_성공() throws Exception {
        TeamMemberResponse response = buildMemberStub(100L, 2L, "테스터", TeamMemberRole.MEMBER);
        given(teamService.addMember(eq(1L), any(), eq(1L))).willReturn(response);

        mockMvc.perform(post("/api/teams/1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("userId", 2, "role", "MEMBER")))
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("MEMBER"));
    }

    // --- 스텁 헬퍼 (리플렉션 없이 익명 서브클래스로 구성) ---

    private com.example.ToyProject_Board.domain.team.Team buildTeamStub(Long id, String name, LocalDateTime createdAt) {
        com.example.ToyProject_Board.domain.team.Team team =
                com.example.ToyProject_Board.domain.team.Team.builder().name(name).build();
        org.springframework.test.util.ReflectionTestUtils.setField(team, "id", id);
        org.springframework.test.util.ReflectionTestUtils.setField(team, "createdAt", createdAt);
        return team;
    }

    private TeamDetailResponse buildTeamDetailStub(Long id, String name) {
        com.example.ToyProject_Board.domain.team.Team team = buildTeamStub(id, name, LocalDateTime.now());
        return new TeamDetailResponse(team, List.of());
    }

    private TeamMemberResponse buildMemberStub(Long memberId, Long userId, String nickname, TeamMemberRole role) {
        com.example.ToyProject_Board.domain.user.User user =
                com.example.ToyProject_Board.domain.user.UserFixture.createWithId(userId);
        com.example.ToyProject_Board.domain.team.Team team =
                com.example.ToyProject_Board.domain.team.TeamFixture.createWithId(1L);
        com.example.ToyProject_Board.domain.team.TeamMember member =
                com.example.ToyProject_Board.domain.team.TeamMember.builder()
                        .team(team).user(user).role(role).build();
        org.springframework.test.util.ReflectionTestUtils.setField(member, "id", memberId);
        org.springframework.test.util.ReflectionTestUtils.setField(member, "createdAt", LocalDateTime.now());
        return new TeamMemberResponse(member);
    }
}
