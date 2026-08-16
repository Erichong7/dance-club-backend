package com.example.ToyProject_Board.domain.performance.service;

import com.example.ToyProject_Board.domain.performance.Performance;
import com.example.ToyProject_Board.domain.performance.dto.request.PerformanceCreateRequest;
import com.example.ToyProject_Board.domain.performance.dto.response.PerformanceResponse;
import com.example.ToyProject_Board.domain.performance.repository.PerformanceRepository;
import com.example.ToyProject_Board.domain.schedule.repository.ScheduleRequestRepository;
import com.example.ToyProject_Board.domain.team.Team;
import com.example.ToyProject_Board.domain.team.repository.TeamMemberRepository;
import com.example.ToyProject_Board.domain.team.repository.TeamRepository;
import com.example.ToyProject_Board.domain.user.User;
import com.example.ToyProject_Board.domain.user.UserRole;
import com.example.ToyProject_Board.domain.user.repository.UserRepository;
import com.example.ToyProject_Board.global.exception.BusinessException;
import com.example.ToyProject_Board.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformanceService {

    private final PerformanceRepository performanceRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final ScheduleRequestRepository scheduleRequestRepository;
    private final UserRepository userRepository;

    @Transactional
    public PerformanceResponse create(PerformanceCreateRequest request, Long adminUserId) {
        verifyAdmin(adminUserId);
        Performance performance = Performance.builder()
                .name(request.getName())
                .performanceDate(request.getPerformanceDate())
                .description(request.getDescription())
                .build();
        return new PerformanceResponse(performanceRepository.save(performance));
    }

    public List<PerformanceResponse> getAll() {
        return performanceRepository.findAll().stream()
                .map(PerformanceResponse::new)
                .toList();
    }

    public PerformanceResponse getOne(Long id) {
        return new PerformanceResponse(findById(id));
    }

    @Transactional
    public void delete(Long id, Long adminUserId) {
        verifyAdmin(adminUserId);
        Performance performance = findById(id);

        scheduleRequestRepository.deleteByPerformance(performance);
        List<Team> teams = teamRepository.findByPerformance(performance);
        for (Team team : teams) {
            teamMemberRepository.deleteByTeam(team);
        }
        teamRepository.deleteAll(teams);
        performanceRepository.delete(performance);
    }

    private Performance findById(Long id) {
        return performanceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PERFORMANCE_NOT_FOUND));
    }

    private void verifyAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.getRole() != UserRole.ADMIN) {
            throw new BusinessException(ErrorCode.ADMIN_ONLY);
        }
    }
}
