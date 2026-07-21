package com.example.ToyProject_Board.domain.schedule.service;

import com.example.ToyProject_Board.domain.performance.Performance;
import com.example.ToyProject_Board.domain.performance.repository.PerformanceRepository;
import com.example.ToyProject_Board.domain.schedule.RoomType;
import com.example.ToyProject_Board.domain.schedule.ScheduleRequest;
import com.example.ToyProject_Board.domain.schedule.ScheduleStatus;
import com.example.ToyProject_Board.domain.schedule.dto.request.AssignRoomRequest;
import com.example.ToyProject_Board.domain.schedule.dto.request.ScheduleAssignRequest;
import com.example.ToyProject_Board.domain.schedule.dto.request.ScheduleCreateRequest;
import com.example.ToyProject_Board.domain.schedule.dto.request.ScheduleRejectRequest;
import com.example.ToyProject_Board.domain.schedule.dto.response.ScheduleResponse;
import com.example.ToyProject_Board.domain.schedule.repository.ScheduleRequestRepository;
import com.example.ToyProject_Board.domain.team.Team;
import com.example.ToyProject_Board.domain.team.TeamMember;
import com.example.ToyProject_Board.domain.team.TeamMemberRole;
import com.example.ToyProject_Board.domain.team.repository.TeamMemberRepository;
import com.example.ToyProject_Board.domain.team.repository.TeamRepository;
import com.example.ToyProject_Board.domain.user.User;
import com.example.ToyProject_Board.domain.user.UserRole;
import com.example.ToyProject_Board.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleService {

    private static final LocalTime CHEER_ROOM_START = LocalTime.of(18, 30);
    private static final LocalTime CHEER_ROOM_END = LocalTime.of(20, 30);
    private static final int CHEER_ROOM_MAX_TEAMS = 3;
    private static final long MAX_DAILY_MINUTES = 120;

    private final ScheduleRequestRepository scheduleRequestRepository;
    private final PerformanceRepository performanceRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public ScheduleResponse create(ScheduleCreateRequest request, Long userId) {
        User user = findUserById(userId);
        Performance performance = findPerformanceById(request.getPerformanceId());
        Team team = findTeamById(request.getTeamId());

        TeamMember member = teamMemberRepository.findByTeamAndUser(team, user)
                .orElseThrow(() -> new RuntimeException("해당 팀의 멤버가 아닙니다"));
        if (member.getRole() == TeamMemberRole.MEMBER) {
            throw new RuntimeException("팀장 또는 부팀장만 신청할 수 있습니다");
        }

        TimeRange range = toOvernightAwareRange(request.getPracticeDate(), request.getStartTime(), request.getEndTime());

        validateDeadline(range.start().toLocalDate());
//        validateDailyLimit(team, practiceDate, startTime, endTime);

        ScheduleRequest scheduleRequest = ScheduleRequest.builder()
                .performance(performance)
                .team(team)
                .submittedBy(user)
                .startAt(range.start())
                .endAt(range.end())
                .alternativeRoom(request.getAlternativeRoom())
                .build();
        return new ScheduleResponse(scheduleRequestRepository.save(scheduleRequest));
    }

    public Page<ScheduleResponse> getByWeek(Long performanceId, LocalDate weekStart, Pageable pageable) {
        Performance performance = findPerformanceById(performanceId);
        LocalDate weekEnd = weekStart.plusDays(6);
        return scheduleRequestRepository
                .findByPerformanceAndStartAtBetween(
                        performance, weekStart.atStartOfDay(), LocalDateTime.of(weekEnd, LocalTime.MAX), pageable)
                .map(ScheduleResponse::new);
    }

    public ScheduleResponse getOne(Long scheduleId) {
        return new ScheduleResponse(findScheduleById(scheduleId));
    }

    public Page<ScheduleResponse> getByTeam(Long teamId, Long userId, Pageable pageable) {
        Team team = findTeamById(teamId);
        User user = findUserById(userId);
        teamMemberRepository.findByTeamAndUser(team, user)
                .orElseThrow(() -> new RuntimeException("해당 팀의 멤버가 아닙니다"));
        return scheduleRequestRepository.findByTeam(team, pageable).map(ScheduleResponse::new);
    }

    @Transactional
    public void cancel(Long scheduleId, Long userId) {
        ScheduleRequest request = findScheduleById(scheduleId);
        User user = findUserById(userId);

        boolean isSubmitter = request.getSubmittedBy().getId().equals(userId);
        boolean isAdmin = user.getRole() == UserRole.ADMIN;
        if (!isSubmitter && !isAdmin) {
            throw new RuntimeException("취소 권한이 없습니다");
        }
        if (request.getStatus() == ScheduleStatus.REJECTED
                || request.getStatus() == ScheduleStatus.CANCELLED) {
            throw new RuntimeException("이미 종료된 신청입니다");
        }
        request.cancel();
    }

    @Transactional
    public ScheduleResponse reject(Long scheduleId, ScheduleRejectRequest rejectRequest, Long adminUserId) {
        verifyAdmin(adminUserId);
        ScheduleRequest request = findScheduleById(scheduleId);
        if (request.getStatus() != ScheduleStatus.PENDING) {
            throw new RuntimeException("대기 중인 신청만 거절할 수 있습니다");
        }
        request.reject(rejectRequest.getAdminNote());
        return new ScheduleResponse(request);
    }

    @Transactional
    public ScheduleResponse assignRoom(ScheduleAssignRequest request, Long adminUserId) {
        verifyAdmin(adminUserId);
        Performance performance = findPerformanceById(request.getPerformanceId());
        Team team = findTeamById(request.getTeamId());
        List<TeamMember> teamMembers = teamMemberRepository.findByTeam(team);
        User leader = teamMembers.stream()
                .filter(teamMember -> teamMember.getRole() == TeamMemberRole.LEADER)
                .findFirst()
                .map(TeamMember::getUser)
                .orElseThrow(() -> new RuntimeException("팀에 리더가 존재하지 않습니다."));

        TimeRange range = toOvernightAwareRange(request.getPracticeDate(), request.getStartTime(), request.getEndTime());

        ScheduleRequest scheduleRequest = ScheduleRequest.builder()
                .performance(performance)
                .team(team)
                .submittedBy(leader)
                .startAt(range.start())
                .endAt(range.end())
                .alternativeRoom(request.getRoom())
                .build();
        scheduleRequest.approve(request.getRoom());

        return new ScheduleResponse(scheduleRequestRepository.save(scheduleRequest));
    }

    @Transactional
    public ScheduleResponse reassignRoom(Long scheduleId, AssignRoomRequest assignRequest, Long adminUserId) {
        verifyAdmin(adminUserId);
        ScheduleRequest request = findScheduleById(scheduleId);
        if (request.getStatus() != ScheduleStatus.APPROVED) {
            throw new RuntimeException("승인된 신청만 방을 재배정할 수 있습니다");
        }
        request.reassignRoom(assignRequest.getRoom());
        return new ScheduleResponse(request);
    }

    @Transactional
    public List<ScheduleResponse> assignWeek(Long performanceId, LocalDate weekStart, Long adminUserId) {
        verifyAdmin(adminUserId);
        Performance performance = findPerformanceById(performanceId);
        LocalDate weekEnd = weekStart.plusDays(6);

        List<ScheduleRequest> requests = scheduleRequestRepository
                .findByPerformanceAndStartAtBetweenAndStatusOrderByCreatedAtAsc(
                        performance, weekStart.atStartOfDay(), LocalDateTime.of(weekEnd, LocalTime.MAX), ScheduleStatus.PENDING);

        // 슬롯 추적: 자정을 넘기는 연습도 정확히 비교할 수 있도록 날짜로 나누지 않고 구간 목록으로 관리
        List<TimeRange> clubRoomSlots = new ArrayList<>();
        List<TimeRange> studentUnionSlots = new ArrayList<>();
        // 치어룸 카운트: date → 배정된 팀 수 (치어룸 창은 항상 같은 날 안에서만 성립하므로 날짜 키 유효)
        Map<LocalDate, Integer> cheerRoomCount = new HashMap<>();
        // 이번 주 동방 배정받은 팀 ID
        Set<Long> teamsWithClubRoom = new HashSet<>();

        for (ScheduleRequest request : requests) {
            LocalDateTime start = request.getStartAt();
            LocalDateTime end = request.getEndAt();
            Long teamId = request.getTeam().getId();
            boolean assigned = false;

            // 1. 동방: 이번 주 동방 미배정 팀에게 우선
            if (!teamsWithClubRoom.contains(teamId)) {
                if (noConflict(clubRoomSlots, start, end)) {
                    request.approve(RoomType.CLUB_ROOM);
                    clubRoomSlots.add(new TimeRange(start, end));
                    teamsWithClubRoom.add(teamId);
                    assigned = true;
                }
            }

            // 2. 학생회관 지하
            if (!assigned) {
                if (noConflict(studentUnionSlots, start, end)) {
                    request.approve(RoomType.STUDENT_UNION_BASEMENT);
                    studentUnionSlots.add(new TimeRange(start, end));
                    assigned = true;
                }
            }

            // 3. 치어룸 (수요일 18:30~20:30 이내만)
            if (!assigned && isWithinCheerRoomWindow(start, end)) {
                LocalDate cheerDate = start.toLocalDate();
                int count = cheerRoomCount.getOrDefault(cheerDate, 0);
                if (count < CHEER_ROOM_MAX_TEAMS) {
                    request.approve(RoomType.CHEER_ROOM);
                    cheerRoomCount.put(cheerDate, count + 1);
                    assigned = true;
                }
            }

            // 4. 예비 연습실 배정
            if (!assigned) {
                request.approve(request.getAlternativeRoom());
            }
        }

        return requests.stream().map(ScheduleResponse::new).toList();
    }

    @Transactional
    public void delete(Long scheduleId, Long userId) {
        User user = findUserById(userId);
        ScheduleRequest scheduleRequest = findScheduleById(scheduleId);

        boolean isAdmin = user.getRole() == UserRole.ADMIN;
        boolean isTeamLeader = teamMemberRepository.findByTeamAndUser(scheduleRequest.getTeam(), user)
                .map(teamMember -> teamMember.getRole() == TeamMemberRole.LEADER)
                .orElse(false);

        if (!isAdmin && !isTeamLeader) {
            throw new RuntimeException("삭제 권한이 없습니다");
        }

        scheduleRequestRepository.delete(scheduleRequest);
    }

    private void validateDeadline(LocalDate practiceDate) {
        LocalDate weekStart = practiceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate deadline = weekStart.minusDays(1); // 전주 일요일
        if (LocalDate.now().isAfter(deadline)) {
            throw new RuntimeException("제출 기한이 지났습니다 (연습일 기준 이전 주 일요일까지 신청 가능합니다)");
        }
    }

//    private void validateDailyLimit(Team team, LocalDate date, LocalTime startTime, LocalTime endTime) {
//        List<ScheduleRequest> existing = scheduleRequestRepository.findByTeamAndPracticeDate(team, date);
//        long bookedMinutes = existing.stream()
//                .filter(r -> r.getStatus() != ScheduleStatus.CANCELLED
//                        && r.getStatus() != ScheduleStatus.REJECTED)
//                .mapToLong(r -> ChronoUnit.MINUTES.between(r.getStartTime(), r.getEndTime()))
//                .sum();
//        long newMinutes = ChronoUnit.MINUTES.between(startTime, endTime);
//        if (bookedMinutes + newMinutes > MAX_DAILY_MINUTES) {
//            throw new RuntimeException("팀별 하루 최대 2시간을 초과합니다");
//        }
//    }

    private record TimeRange(LocalDateTime start, LocalDateTime end) {}

    // 시작 시각과 종료 시각으로 구간을 만들되, 종료가 시작보다 앞서면(예: 23:00 → 02:00) 익일 종료로 간주
    private TimeRange toOvernightAwareRange(LocalDate date, LocalTime startTime, LocalTime endTime) {
        if (startTime.equals(endTime)) {
            throw new RuntimeException("시작 시간과 종료 시간이 같을 수 없습니다");
        }
        LocalDateTime start = LocalDateTime.of(date, startTime);
        LocalDateTime end = LocalDateTime.of(date, endTime);
        if (end.isBefore(start)) { // 익일 종료
            end = end.plusDays(1);
        }
        return new TimeRange(start, end);
    }

    private boolean noConflict(List<TimeRange> slots, LocalDateTime start, LocalDateTime end) {
        return slots.stream().noneMatch(slot -> slot.start().isBefore(end) && slot.end().isAfter(start));
    }

    private boolean isWithinCheerRoomWindow(LocalDateTime start, LocalDateTime end) {
        LocalDate date = start.toLocalDate();
        if (date.getDayOfWeek() != DayOfWeek.WEDNESDAY) {
            return false;
        }
        LocalDateTime windowStart = LocalDateTime.of(date, CHEER_ROOM_START);
        LocalDateTime windowEnd = LocalDateTime.of(date, CHEER_ROOM_END);
        return !start.isBefore(windowStart) && !end.isAfter(windowEnd);
    }

    private ScheduleRequest findScheduleById(Long id) {
        return scheduleRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("신청을 찾을 수 없습니다"));
    }

    private Performance findPerformanceById(Long id) {
        return performanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공연을 찾을 수 없습니다"));
    }

    private Team findTeamById(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("팀을 찾을 수 없습니다"));
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다"));
    }

    private void verifyAdmin(Long userId) {
        User user = findUserById(userId);
        if (user.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("관리자 권한이 필요합니다");
        }
    }
}
