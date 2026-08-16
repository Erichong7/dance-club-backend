package com.example.ToyProject_Board.domain.performance;

import com.example.ToyProject_Board.domain.performance.repository.PerformanceRepository;
import com.example.ToyProject_Board.domain.performance.service.PerformanceService;
import com.example.ToyProject_Board.domain.schedule.repository.ScheduleRequestRepository;
import com.example.ToyProject_Board.domain.team.Team;
import com.example.ToyProject_Board.domain.team.TeamFixture;
import com.example.ToyProject_Board.domain.team.repository.TeamMemberRepository;
import com.example.ToyProject_Board.domain.team.repository.TeamRepository;
import com.example.ToyProject_Board.domain.user.User;
import com.example.ToyProject_Board.domain.user.UserFixture;
import com.example.ToyProject_Board.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class PerformanceServiceTest {

    @InjectMocks
    private PerformanceService performanceService;

    @Mock
    private PerformanceRepository performanceRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private ScheduleRequestRepository scheduleRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    void 공연_삭제시_연관된_팀과_일정도_함께_삭제된다() {
        User admin = UserFixture.createAdminWithId(1L);
        Performance performance = PerformanceFixture.createWithId(100L);
        Team team1 = TeamFixture.createWithId(10L);
        Team team2 = TeamFixture.createWithId(11L);
        List<Team> teams = List.of(team1, team2);

        given(userRepository.findById(1L)).willReturn(Optional.of(admin));
        given(performanceRepository.findById(100L)).willReturn(Optional.of(performance));
        given(teamRepository.findByPerformance(performance)).willReturn(teams);

        performanceService.delete(100L, 1L);

        verify(scheduleRequestRepository).deleteByPerformance(performance);
        verify(teamMemberRepository).deleteByTeam(team1);
        verify(teamMemberRepository).deleteByTeam(team2);
        verify(teamRepository).deleteAll(teams);
        verify(performanceRepository).delete(performance);
    }

    @Test
    void 공연에_팀이_없어도_삭제된다() {
        User admin = UserFixture.createAdminWithId(1L);
        Performance performance = PerformanceFixture.createWithId(100L);

        given(userRepository.findById(1L)).willReturn(Optional.of(admin));
        given(performanceRepository.findById(100L)).willReturn(Optional.of(performance));
        given(teamRepository.findByPerformance(performance)).willReturn(List.of());

        performanceService.delete(100L, 1L);

        verify(scheduleRequestRepository).deleteByPerformance(performance);
        verifyNoInteractions(teamMemberRepository);
        verify(teamRepository).deleteAll(List.of());
        verify(performanceRepository).delete(performance);
    }

    @Test
    void 관리자가_아닌_사용자의_공연_삭제_실패() {
        User user = UserFixture.createWithId(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        assertThatThrownBy(() -> performanceService.delete(100L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("관리자 권한");

        verifyNoInteractions(performanceRepository, teamRepository, teamMemberRepository, scheduleRequestRepository);
    }

    @Test
    void 존재하지_않는_공연_삭제_실패() {
        User admin = UserFixture.createAdminWithId(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(admin));
        given(performanceRepository.findById(100L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> performanceService.delete(100L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("공연을 찾을 수 없습니다");

        verify(teamRepository, never()).findByPerformance(any());
        verifyNoInteractions(scheduleRequestRepository, teamMemberRepository);
    }
}
