package com.example.ToyProject_Board.domain.user.service;

import com.example.ToyProject_Board.domain.post.repository.PostRepository;
import com.example.ToyProject_Board.domain.schedule.repository.ScheduleRequestRepository;
import com.example.ToyProject_Board.domain.team.Team;
import com.example.ToyProject_Board.domain.team.TeamMember;
import com.example.ToyProject_Board.domain.team.repository.TeamMemberRepository;
import com.example.ToyProject_Board.domain.user.User;
import com.example.ToyProject_Board.domain.user.UserRole;
import com.example.ToyProject_Board.domain.user.dto.request.UserSearchRequest;
import com.example.ToyProject_Board.domain.user.dto.response.UserDetailResponse;
import com.example.ToyProject_Board.domain.user.dto.response.UserSearchResponse;
import com.example.ToyProject_Board.domain.user.repository.UserRepository;
import com.example.ToyProject_Board.global.exception.BusinessException;
import com.example.ToyProject_Board.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final PostRepository postRepository;
    private final ScheduleRequestRepository scheduleRequestRepository;

    public UserDetailResponse getMyInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        List<Team> teams = teamMemberRepository.findByUser(user).stream()
                .map(TeamMember::getTeam)
                .toList();
        List<Long> teamIds = teams.stream().map(Team::getId).toList();
        List<String> teamNames = teams.stream().map(Team::getName).toList();

        return new UserDetailResponse(user.getId(), user.getEmail(), user.getNickname(), user.getRole(),
                teamIds, teamNames);
    }

    // 회원 검색
    public Page<UserSearchResponse> searchUsers(Long userId, UserSearchRequest request, Pageable pageable) {
        verifyAdmin(userId);
        return userRepository.searchUsers(request, pageable)
                .map(UserSearchResponse::new);
    }

    // 회원 삭제
    @Transactional
    public void deleteUser(Long adminId, Long targetUserId) {
        verifyAdmin(adminId);

        if (adminId.equals(targetUserId)) {
            throw new BusinessException(ErrorCode.SELF_DELETE_FORBIDDEN);
        }

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        teamMemberRepository.deleteByUser(target);
        postRepository.deleteByUser(target);
        scheduleRequestRepository.deleteBySubmittedBy(target);
        userRepository.delete(target);
    }

    private void verifyAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.getRole() != UserRole.ADMIN) {
            throw new BusinessException(ErrorCode.ADMIN_ONLY);
        }
    }
}