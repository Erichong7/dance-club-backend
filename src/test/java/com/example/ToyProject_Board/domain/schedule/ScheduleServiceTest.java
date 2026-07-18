package com.example.ToyProject_Board.domain.schedule;

import com.example.ToyProject_Board.domain.performance.Performance;
import com.example.ToyProject_Board.domain.performance.PerformanceFixture;
import com.example.ToyProject_Board.domain.performance.repository.PerformanceRepository;
import com.example.ToyProject_Board.domain.schedule.dto.request.ScheduleCreateRequest;
import com.example.ToyProject_Board.domain.schedule.dto.response.ScheduleResponse;
import com.example.ToyProject_Board.domain.schedule.repository.ScheduleRequestRepository;
import com.example.ToyProject_Board.domain.schedule.service.ScheduleService;
import com.example.ToyProject_Board.domain.team.Team;
import com.example.ToyProject_Board.domain.team.TeamFixture;
import com.example.ToyProject_Board.domain.team.TeamMember;
import com.example.ToyProject_Board.domain.team.TeamMemberRole;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @InjectMocks
    private ScheduleService scheduleService;

    @Mock
    private ScheduleRequestRepository scheduleRequestRepository;

    @Mock
    private PerformanceRepository performanceRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private UserRepository userRepository;

    // 항상 마감 전인 날짜 (다음 주 목요일)
    private LocalDate futurePracticeDate() {
        return LocalDate.now().plusWeeks(1).with(java.time.DayOfWeek.THURSDAY);
    }

    @Test
    void 일정_생성_성공() {
        User user = UserFixture.createWithId(1L);
        Team team = TeamFixture.createWithId(10L);
        Performance performance = PerformanceFixture.createWithId(5L);

        TeamMember leader = TeamMember.builder()
                .team(team).user(user).role(TeamMemberRole.LEADER).build();

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(performanceRepository.findById(5L)).willReturn(Optional.of(performance));
        given(teamRepository.findById(10L)).willReturn(Optional.of(team));
        given(teamMemberRepository.findByTeamAndUser(team, user)).willReturn(Optional.of(leader));

        ScheduleRequest saved = ScheduleRequest.builder()
                .performance(performance).team(team).submittedBy(user)
                .practiceDate(futurePracticeDate())
                .startTime(LocalTime.of(18, 0))
                .endTime(LocalTime.of(20, 0))
                .build();
        ReflectionTestUtils.setField(saved, "id", 100L);
        given(scheduleRequestRepository.save(any())).willReturn(saved);

        ScheduleCreateRequest request = new ScheduleCreateRequest();
        ReflectionTestUtils.setField(request, "performanceId", 5L);
        ReflectionTestUtils.setField(request, "teamId", 10L);
        ReflectionTestUtils.setField(request, "practiceDate", futurePracticeDate());
        ReflectionTestUtils.setField(request, "startTime", LocalTime.of(18, 0));
        ReflectionTestUtils.setField(request, "endTime", LocalTime.of(20, 0));

        ScheduleResponse response = scheduleService.create(request, 1L);

        assertThat(response.getStatus()).isEqualTo(ScheduleStatus.PENDING);
    }

    @Test
    void 권한없는_팀원의_일정_생성_실패() {
        User user = UserFixture.createWithId(1L);
        Team team = TeamFixture.createWithId(10L);
        Performance performance = PerformanceFixture.createWithId(5L);

        TeamMember member = TeamMember.builder()
                .team(team).user(user).role(TeamMemberRole.MEMBER).build();

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(performanceRepository.findById(5L)).willReturn(Optional.of(performance));
        given(teamRepository.findById(10L)).willReturn(Optional.of(team));
        given(teamMemberRepository.findByTeamAndUser(team, user)).willReturn(Optional.of(member));

        ScheduleCreateRequest request = new ScheduleCreateRequest();
        ReflectionTestUtils.setField(request, "performanceId", 5L);
        ReflectionTestUtils.setField(request, "teamId", 10L);
        ReflectionTestUtils.setField(request, "practiceDate", futurePracticeDate());
        ReflectionTestUtils.setField(request, "startTime", LocalTime.of(18, 0));
        ReflectionTestUtils.setField(request, "endTime", LocalTime.of(20, 0));

        assertThatThrownBy(() -> scheduleService.create(request, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("팀장 또는 부팀장");
    }

    @Test
    void 제출_기한_초과로_일정_생성_실패() {
        User user = UserFixture.createWithId(1L);
        Team team = TeamFixture.createWithId(10L);
        Performance performance = PerformanceFixture.createWithId(5L);

        TeamMember leader = TeamMember.builder()
                .team(team).user(user).role(TeamMemberRole.LEADER).build();

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(performanceRepository.findById(5L)).willReturn(Optional.of(performance));
        given(teamRepository.findById(10L)).willReturn(Optional.of(team));
        given(teamMemberRepository.findByTeamAndUser(team, user)).willReturn(Optional.of(leader));

        // 이번 주 월요일 = 이미 마감 지남
        LocalDate pastDate = LocalDate.now().with(java.time.DayOfWeek.WEDNESDAY);

        ScheduleCreateRequest request = new ScheduleCreateRequest();
        ReflectionTestUtils.setField(request, "performanceId", 5L);
        ReflectionTestUtils.setField(request, "teamId", 10L);
        ReflectionTestUtils.setField(request, "practiceDate", pastDate);
        ReflectionTestUtils.setField(request, "startTime", LocalTime.of(18, 0));
        ReflectionTestUtils.setField(request, "endTime", LocalTime.of(20, 0));

        assertThatThrownBy(() -> scheduleService.create(request, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("제출 기한");
    }

//    @Test
//    void 하루_최대_연습시간_초과로_일정_생성_실패() {
//        User user = UserFixture.createWithId(1L);
//        Team team = TeamFixture.createWithId(10L);
//        Performance performance = PerformanceFixture.createWithId(5L);
//
//        TeamMember leader = TeamMember.builder()
//                .team(team).user(user).role(TeamMemberRole.LEADER).build();
//
//        // 이미 2시간 신청된 상태
//        ScheduleRequest existing = ScheduleRequest.builder()
//                .performance(performance).team(team).submittedBy(user)
//                .practiceDate(futurePracticeDate())
//                .startTime(LocalTime.of(16, 0))
//                .endTime(LocalTime.of(18, 0))
//                .build();
//
//        given(userRepository.findById(1L)).willReturn(Optional.of(user));
//        given(performanceRepository.findById(5L)).willReturn(Optional.of(performance));
//        given(teamRepository.findById(10L)).willReturn(Optional.of(team));
//        given(teamMemberRepository.findByTeamAndUser(team, user)).willReturn(Optional.of(leader));
//        given(scheduleRequestRepository.findByTeamAndPracticeDate(team, futurePracticeDate()))
//                .willReturn(List.of(existing));
//
//        ScheduleCreateRequest request = new ScheduleCreateRequest();
//        ReflectionTestUtils.setField(request, "performanceId", 5L);
//        ReflectionTestUtils.setField(request, "teamId", 10L);
//        ReflectionTestUtils.setField(request, "practiceDate", futurePracticeDate());
//        ReflectionTestUtils.setField(request, "startTime", LocalTime.of(18, 0));
//        ReflectionTestUtils.setField(request, "endTime", LocalTime.of(19, 0));
//
//        assertThatThrownBy(() -> scheduleService.create(request, 1L))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("하루 최대 2시간");
//    }

    @Test
    void 주간_연습실_배정_성공() {
        User admin = UserFixture.createAdminWithId(1L);
        Team teamA = TeamFixture.createWithNameAndId("A팀", 10L);
        Team teamB = TeamFixture.createWithNameAndId("B팀", 20L);
        User userA = UserFixture.createWithId(2L);
        User userB = UserFixture.createWithId(3L);
        Performance performance = PerformanceFixture.createWithId(5L);

        LocalDate monday = LocalDate.now().plusWeeks(1).with(java.time.DayOfWeek.MONDAY);

        ScheduleRequest reqA = ScheduleRequest.builder()
                .performance(performance).team(teamA).submittedBy(userA)
                .practiceDate(monday)
                .startTime(LocalTime.of(18, 0))
                .endTime(LocalTime.of(20, 0))
                .build();
        ReflectionTestUtils.setField(reqA, "id", 1L);

        ScheduleRequest reqB = ScheduleRequest.builder()
                .performance(performance).team(teamB).submittedBy(userB)
                .practiceDate(monday)
                .startTime(LocalTime.of(18, 0))
                .endTime(LocalTime.of(20, 0))
                .build();
        ReflectionTestUtils.setField(reqB, "id", 2L);

        given(userRepository.findById(1L)).willReturn(Optional.of(admin));
        given(performanceRepository.findById(5L)).willReturn(Optional.of(performance));
        given(scheduleRequestRepository
                .findByPerformanceAndPracticeDateBetweenAndStatusOrderByCreatedAtAsc(
                        performance, monday, monday.plusDays(6), ScheduleStatus.PENDING))
                .willReturn(List.of(reqA, reqB));

        List<ScheduleResponse> responses = scheduleService.assignWeek(5L, monday, 1L);

        assertThat(responses).hasSize(2);
        // A팀: 동방(첫 번째), B팀: 학생회관 지하(두 번째, 같은 시간대이므로 동방 충돌)
        assertThat(responses.get(0).getAssignedRoom()).isEqualTo(RoomType.CLUB_ROOM);
        assertThat(responses.get(1).getAssignedRoom()).isEqualTo(RoomType.STUDENT_UNION_BASEMENT);
    }

    @Test
    void 신청자_본인의_일정_취소_성공() {
        User user = UserFixture.createWithId(1L);
        Team team = TeamFixture.createWithId(10L);
        Performance performance = PerformanceFixture.createWithId(5L);

        ScheduleRequest request = ScheduleRequest.builder()
                .performance(performance).team(team).submittedBy(user)
                .practiceDate(LocalDate.now().plusDays(7))
                .startTime(LocalTime.of(18, 0))
                .endTime(LocalTime.of(20, 0))
                .build();
        ReflectionTestUtils.setField(request, "id", 100L);

        given(scheduleRequestRepository.findById(100L)).willReturn(Optional.of(request));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        scheduleService.cancel(100L, 1L);

        assertThat(request.getStatus()).isEqualTo(ScheduleStatus.CANCELLED);
    }
}
