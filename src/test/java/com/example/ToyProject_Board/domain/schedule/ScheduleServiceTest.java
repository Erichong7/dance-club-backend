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
import java.time.LocalDateTime;
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
                .startAt(LocalDateTime.of(futurePracticeDate(), LocalTime.of(18, 0)))
                .endAt(LocalDateTime.of(futurePracticeDate(), LocalTime.of(20, 0)))
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

    @Test
    void 종료시간이_시작시간보다_이른_새벽_연습은_익일_종료로_저장된다() {
        User user = UserFixture.createWithId(1L);
        Team team = TeamFixture.createWithId(10L);
        Performance performance = PerformanceFixture.createWithId(5L);

        TeamMember leader = TeamMember.builder()
                .team(team).user(user).role(TeamMemberRole.LEADER).build();

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(performanceRepository.findById(5L)).willReturn(Optional.of(performance));
        given(teamRepository.findById(10L)).willReturn(Optional.of(team));
        given(teamMemberRepository.findByTeamAndUser(team, user)).willReturn(Optional.of(leader));
        given(scheduleRequestRepository.save(any()))
                .willAnswer(invocation -> invocation.getArgument(0));

        ScheduleCreateRequest request = new ScheduleCreateRequest();
        ReflectionTestUtils.setField(request, "performanceId", 5L);
        ReflectionTestUtils.setField(request, "teamId", 10L);
        ReflectionTestUtils.setField(request, "practiceDate", futurePracticeDate());
        ReflectionTestUtils.setField(request, "startTime", LocalTime.of(23, 0));
        ReflectionTestUtils.setField(request, "endTime", LocalTime.of(2, 0));

        ScheduleResponse response = scheduleService.create(request, 1L);

        assertThat(response.getStartAt()).isEqualTo(LocalDateTime.of(futurePracticeDate(), LocalTime.of(23, 0)));
        assertThat(response.getEndAt()).isEqualTo(LocalDateTime.of(futurePracticeDate().plusDays(1), LocalTime.of(2, 0)));
    }

    @Test
    void 시작시간과_종료시간이_같으면_일정_생성_실패() {
        User user = UserFixture.createWithId(1L);
        Team team = TeamFixture.createWithId(10L);
        Performance performance = PerformanceFixture.createWithId(5L);

        TeamMember leader = TeamMember.builder()
                .team(team).user(user).role(TeamMemberRole.LEADER).build();

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(performanceRepository.findById(5L)).willReturn(Optional.of(performance));
        given(teamRepository.findById(10L)).willReturn(Optional.of(team));
        given(teamMemberRepository.findByTeamAndUser(team, user)).willReturn(Optional.of(leader));

        ScheduleCreateRequest request = new ScheduleCreateRequest();
        ReflectionTestUtils.setField(request, "performanceId", 5L);
        ReflectionTestUtils.setField(request, "teamId", 10L);
        ReflectionTestUtils.setField(request, "practiceDate", futurePracticeDate());
        ReflectionTestUtils.setField(request, "startTime", LocalTime.of(18, 0));
        ReflectionTestUtils.setField(request, "endTime", LocalTime.of(18, 0));

        assertThatThrownBy(() -> scheduleService.create(request, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("시작 시간과 종료 시간이 같을 수 없습니다");
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
                .startAt(LocalDateTime.of(monday, LocalTime.of(18, 0)))
                .endAt(LocalDateTime.of(monday, LocalTime.of(20, 0)))
                .build();
        ReflectionTestUtils.setField(reqA, "id", 1L);

        ScheduleRequest reqB = ScheduleRequest.builder()
                .performance(performance).team(teamB).submittedBy(userB)
                .startAt(LocalDateTime.of(monday, LocalTime.of(18, 0)))
                .endAt(LocalDateTime.of(monday, LocalTime.of(20, 0)))
                .build();
        ReflectionTestUtils.setField(reqB, "id", 2L);

        given(userRepository.findById(1L)).willReturn(Optional.of(admin));
        given(performanceRepository.findById(5L)).willReturn(Optional.of(performance));
        given(scheduleRequestRepository
                .findByPerformanceAndStartAtBetweenAndStatusOrderByCreatedAtAsc(
                        performance, monday.atStartOfDay(), LocalDateTime.of(monday.plusDays(6), LocalTime.MAX), ScheduleStatus.PENDING))
                .willReturn(List.of(reqA, reqB));

        List<ScheduleResponse> responses = scheduleService.assignWeek(5L, monday, 1L);

        assertThat(responses).hasSize(2);
        // A팀: 동방(첫 번째), B팀: 학생회관 지하(두 번째, 같은 시간대이므로 동방 충돌)
        assertThat(responses.get(0).getAssignedRoom()).isEqualTo(RoomType.CLUB_ROOM);
        assertThat(responses.get(1).getAssignedRoom()).isEqualTo(RoomType.STUDENT_UNION_BASEMENT);
    }

    @Test
    void 주간_연습실_배정_자정을_넘기는_연습도_충돌이_감지된다() {
        User admin = UserFixture.createAdminWithId(1L);
        Team teamA = TeamFixture.createWithNameAndId("A팀", 10L);
        Team teamB = TeamFixture.createWithNameAndId("B팀", 20L);
        User userA = UserFixture.createWithId(2L);
        User userB = UserFixture.createWithId(3L);
        Performance performance = PerformanceFixture.createWithId(5L);

        LocalDate monday = LocalDate.now().plusWeeks(1).with(java.time.DayOfWeek.MONDAY);

        // A팀: 월요일 23:00 ~ 화요일 02:00 (자정을 넘기는 새벽 연습)
        ScheduleRequest reqA = ScheduleRequest.builder()
                .performance(performance).team(teamA).submittedBy(userA)
                .startAt(LocalDateTime.of(monday, LocalTime.of(23, 0)))
                .endAt(LocalDateTime.of(monday.plusDays(1), LocalTime.of(2, 0)))
                .build();
        ReflectionTestUtils.setField(reqA, "id", 1L);

        // B팀: 화요일 01:00 ~ 03:00 → A팀의 새벽 연습(~화 02:00)과 겹침
        ScheduleRequest reqB = ScheduleRequest.builder()
                .performance(performance).team(teamB).submittedBy(userB)
                .startAt(LocalDateTime.of(monday.plusDays(1), LocalTime.of(1, 0)))
                .endAt(LocalDateTime.of(monday.plusDays(1), LocalTime.of(3, 0)))
                .build();
        ReflectionTestUtils.setField(reqB, "id", 2L);

        given(userRepository.findById(1L)).willReturn(Optional.of(admin));
        given(performanceRepository.findById(5L)).willReturn(Optional.of(performance));
        given(scheduleRequestRepository
                .findByPerformanceAndStartAtBetweenAndStatusOrderByCreatedAtAsc(
                        performance, monday.atStartOfDay(), LocalDateTime.of(monday.plusDays(6), LocalTime.MAX), ScheduleStatus.PENDING))
                .willReturn(List.of(reqA, reqB));

        List<ScheduleResponse> responses = scheduleService.assignWeek(5L, monday, 1L);

        assertThat(responses).hasSize(2);
        // A팀: 동방(첫 번째), B팀: 실제로 겹치므로 동방이 아닌 학생회관 지하로 배정되어야 함
        assertThat(responses.get(0).getAssignedRoom()).isEqualTo(RoomType.CLUB_ROOM);
        assertThat(responses.get(1).getAssignedRoom()).isEqualTo(RoomType.STUDENT_UNION_BASEMENT);
    }

    @Test
    void 주간_연습실_배정_자정을_넘기는_연습은_치어룸에_배정되지_않는다() {
        User admin = UserFixture.createAdminWithId(1L);
        Team teamA = TeamFixture.createWithNameAndId("A팀", 10L);
        Team teamB = TeamFixture.createWithNameAndId("B팀", 20L);
        Team teamC = TeamFixture.createWithNameAndId("C팀", 30L);
        User userA = UserFixture.createWithId(2L);
        User userB = UserFixture.createWithId(3L);
        User userC = UserFixture.createWithId(4L);
        Performance performance = PerformanceFixture.createWithId(5L);

        LocalDate wednesday = LocalDate.now().plusWeeks(1).with(java.time.DayOfWeek.WEDNESDAY);
        LocalDate monday = wednesday.with(java.time.DayOfWeek.MONDAY);
        LocalDateTime start = LocalDateTime.of(wednesday, LocalTime.of(23, 0));
        LocalDateTime end = LocalDateTime.of(wednesday.plusDays(1), LocalTime.of(1, 0));

        // 세 팀 모두 같은 자정 넘기는 시간대(수 23:00 ~ 목 01:00)에 신청 → 동방/학생회관 지하가 먼저 소진됨
        ScheduleRequest reqA = ScheduleRequest.builder()
                .performance(performance).team(teamA).submittedBy(userA)
                .startAt(start).endAt(end).build();
        ReflectionTestUtils.setField(reqA, "id", 1L);

        ScheduleRequest reqB = ScheduleRequest.builder()
                .performance(performance).team(teamB).submittedBy(userB)
                .startAt(start).endAt(end).build();
        ReflectionTestUtils.setField(reqB, "id", 2L);

        ScheduleRequest reqC = ScheduleRequest.builder()
                .performance(performance).team(teamC).submittedBy(userC)
                .startAt(start).endAt(end)
                .alternativeRoom(RoomType.UNDERGROUND_PARKING)
                .build();
        ReflectionTestUtils.setField(reqC, "id", 3L);

        given(userRepository.findById(1L)).willReturn(Optional.of(admin));
        given(performanceRepository.findById(5L)).willReturn(Optional.of(performance));
        given(scheduleRequestRepository
                .findByPerformanceAndStartAtBetweenAndStatusOrderByCreatedAtAsc(
                        performance, monday.atStartOfDay(), LocalDateTime.of(monday.plusDays(6), LocalTime.MAX), ScheduleStatus.PENDING))
                .willReturn(List.of(reqA, reqB, reqC));

        List<ScheduleResponse> responses = scheduleService.assignWeek(5L, monday, 1L);

        assertThat(responses).hasSize(3);
        assertThat(responses.get(0).getAssignedRoom()).isEqualTo(RoomType.CLUB_ROOM);
        assertThat(responses.get(1).getAssignedRoom()).isEqualTo(RoomType.STUDENT_UNION_BASEMENT);
        // 자정을 넘기므로 치어룸(수 18:30~20:30) 창에 들어가지 않아 예비 연습실로 폴백되어야 함
        assertThat(responses.get(2).getAssignedRoom()).isEqualTo(RoomType.UNDERGROUND_PARKING);
    }

    @Test
    void 신청자_본인의_일정_취소_성공() {
        User user = UserFixture.createWithId(1L);
        Team team = TeamFixture.createWithId(10L);
        Performance performance = PerformanceFixture.createWithId(5L);

        ScheduleRequest request = ScheduleRequest.builder()
                .performance(performance).team(team).submittedBy(user)
                .startAt(LocalDateTime.of(LocalDate.now().plusDays(7), LocalTime.of(18, 0)))
                .endAt(LocalDateTime.of(LocalDate.now().plusDays(7), LocalTime.of(20, 0)))
                .build();
        ReflectionTestUtils.setField(request, "id", 100L);

        given(scheduleRequestRepository.findById(100L)).willReturn(Optional.of(request));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        scheduleService.cancel(100L, 1L);

        assertThat(request.getStatus()).isEqualTo(ScheduleStatus.CANCELLED);
    }
}
