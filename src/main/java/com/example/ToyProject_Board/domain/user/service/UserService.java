package com.example.ToyProject_Board.domain.user.service;

import com.example.ToyProject_Board.domain.team.Team;
import com.example.ToyProject_Board.domain.team.TeamMember;
import com.example.ToyProject_Board.domain.team.repository.TeamMemberRepository;
import com.example.ToyProject_Board.domain.user.User;
import com.example.ToyProject_Board.domain.user.dto.response.UserDetailResponse;
import com.example.ToyProject_Board.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;

    public UserDetailResponse getMyInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다"));
        List<Team> teams = teamMemberRepository.findByUser(user).stream()
                .map(TeamMember::getTeam)
                .toList();
        List<Long> teamIds = teams.stream().map(Team::getId).toList();
        List<String> teamNames = teams.stream().map(Team::getName).toList();

        return new UserDetailResponse(user.getId(), user.getEmail(), user.getNickname(), user.getRole(),
                teamIds, teamNames);
    }
}