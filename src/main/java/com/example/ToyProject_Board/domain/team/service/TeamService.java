package com.example.ToyProject_Board.domain.team.service;

import com.example.ToyProject_Board.domain.performance.Performance;
import com.example.ToyProject_Board.domain.performance.repository.PerformanceRepository;
import com.example.ToyProject_Board.domain.team.Team;
import com.example.ToyProject_Board.domain.team.TeamMember;
import com.example.ToyProject_Board.domain.team.TeamMemberRole;
import com.example.ToyProject_Board.domain.team.dto.request.AddMemberRequest;
import com.example.ToyProject_Board.domain.team.dto.request.TeamCreateRequest;
import com.example.ToyProject_Board.domain.team.dto.request.UpdateMemberRoleRequest;
import com.example.ToyProject_Board.domain.team.dto.response.TeamDetailResponse;
import com.example.ToyProject_Board.domain.team.dto.response.TeamMemberResponse;
import com.example.ToyProject_Board.domain.team.dto.response.TeamResponse;
import com.example.ToyProject_Board.domain.team.repository.TeamMemberRepository;
import com.example.ToyProject_Board.domain.team.repository.TeamRepository;
import com.example.ToyProject_Board.domain.user.User;
import com.example.ToyProject_Board.domain.user.UserRole;
import com.example.ToyProject_Board.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final PerformanceRepository performanceRepository;
    private final UserRepository userRepository;

    @Transactional
    public TeamResponse createTeam(TeamCreateRequest request, Long adminUserId) {
        verifyAdmin(adminUserId);
        if (teamRepository.existsByName(request.getName())) {
            throw new RuntimeException("이미 존재하는 팀 이름입니다");
        }
        Performance performance = performanceRepository.findById(request.getPerformanceId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 공연입니다."));
        Team team = Team.builder()
                .name(request.getName())
                .performance(performance)
                .build();
        return new TeamResponse(teamRepository.save(team));
    }

    public List<TeamResponse> getAllTeams() {
        return teamRepository.findAll().stream()
                .map(TeamResponse::new)
                .toList();
    }

    public TeamDetailResponse getTeam(Long teamId) {
        Team team = findTeamById(teamId);
        List<TeamMember> members = teamMemberRepository.findByTeam(team);
        return new TeamDetailResponse(team, members);
    }

    @Transactional
    public TeamMemberResponse addMember(Long teamId, AddMemberRequest request, Long adminUserId) {
        verifyAdmin(adminUserId);
        Team team = findTeamById(teamId);
        User user = findUserById(request.getUserId());

        if (teamMemberRepository.existsByTeamAndUser(team, user)) {
            throw new RuntimeException("이미 팀에 속해있는 멤버입니다");
        }
        if (request.getRole() == TeamMemberRole.LEADER
                && teamMemberRepository.existsByTeamAndRole(team, TeamMemberRole.LEADER)) {
            throw new RuntimeException("이미 팀장이 존재합니다");
        }
        if (request.getRole() == TeamMemberRole.DEPUTY
                && teamMemberRepository.existsByTeamAndRole(team, TeamMemberRole.DEPUTY)) {
            throw new RuntimeException("이미 부팀장이 존재합니다");
        }

        TeamMember member = TeamMember.builder()
                .team(team)
                .user(user)
                .role(request.getRole())
                .build();
        return new TeamMemberResponse(teamMemberRepository.save(member));
    }

    @Transactional
    public TeamMemberResponse updateMemberRole(Long teamId, Long userId, UpdateMemberRoleRequest request, Long adminUserId) {
        verifyAdmin(adminUserId);
        Team team = findTeamById(teamId);
        User user = findUserById(userId);

        TeamMember member = teamMemberRepository.findByTeamAndUser(team, user)
                .orElseThrow(() -> new RuntimeException("팀 멤버를 찾을 수 없습니다"));

        TeamMemberRole newRole = request.getRole();
        if (newRole == TeamMemberRole.LEADER
                && teamMemberRepository.existsByTeamAndRole(team, TeamMemberRole.LEADER)) {
            throw new RuntimeException("이미 팀장이 존재합니다");
        }
        if (newRole == TeamMemberRole.DEPUTY
                && teamMemberRepository.existsByTeamAndRole(team, TeamMemberRole.DEPUTY)) {
            throw new RuntimeException("이미 부팀장이 존재합니다");
        }

        member.updateRole(newRole);
        return new TeamMemberResponse(member);
    }

    @Transactional
    public void delete(Long teamId, Long userId) {
        verifyAdmin(userId);
        if(!teamRepository.existsById(teamId)) {
            throw new RuntimeException("팀을 찾을 수 없습니다");
        }
        teamRepository.deleteById(teamId);
    }

    @Transactional
    public void removeMember(Long teamId, Long userId, Long adminUserId) {
        verifyAdmin(adminUserId);
        Team team = findTeamById(teamId);
        User user = findUserById(userId);
        if (!teamMemberRepository.existsByTeamAndUser(team, user)) {
            throw new RuntimeException("팀 멤버를 찾을 수 없습니다");
        }
        teamMemberRepository.deleteByTeamAndUser(team, user);
    }

    private Team findTeamById(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("팀을 찾을 수 없습니다"));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다"));
    }

    private void verifyAdmin(Long userId) {
        User user = findUserById(userId);
        if (user.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("관리자 권한이 필요합니다");
        }
    }
}
