package com.example.ToyProject_Board.domain.performance.service;

import com.example.ToyProject_Board.domain.performance.Performance;
import com.example.ToyProject_Board.domain.performance.dto.request.PerformanceCreateRequest;
import com.example.ToyProject_Board.domain.performance.dto.response.PerformanceResponse;
import com.example.ToyProject_Board.domain.performance.repository.PerformanceRepository;
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
public class PerformanceService {

    private final PerformanceRepository performanceRepository;
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
        performanceRepository.delete(findById(id));
    }

    private Performance findById(Long id) {
        return performanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공연을 찾을 수 없습니다"));
    }

    private void verifyAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다"));
        if (user.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("관리자 권한이 필요합니다");
        }
    }
}
