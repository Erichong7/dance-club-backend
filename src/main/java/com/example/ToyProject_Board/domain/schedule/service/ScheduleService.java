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

        LocalDate practiceDate = request.getPracticeDate();
        LocalTime startTime = request.getStartTime();
        LocalTime endTime = request.getEndTime();

        validateDeadline(practiceDate);
        validateDailyLimit(team, practiceDate, startTime, endTime);

        ScheduleRequest scheduleRequest = ScheduleRequest.builder()
                .performance(performance)
                .team(team)
                .submittedBy(user)
                .practiceDate(practiceDate)
                .startTime(startTime)
                .endTime(endTime)
                .alternativeRoom(request.getAlternativeRoom())
                .build();
        return new ScheduleResponse(scheduleRequestRepository.save(scheduleRequest));
    }

    public Page<ScheduleResponse> getByWeek(Long performanceId, LocalDate weekStart, Pageable pageable) {
        Performance performance = findPerformanceById(performanceId);
        LocalDate weekEnd = weekStart.plusDays(6);
        return scheduleRequestRepository
                .findByPerformanceAndPracticeDateBetween(performance, weekStart, weekEnd, pageable)
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

        ScheduleRequest scheduleRequest = ScheduleRequest.builder()
                .performance(performance)
                .team(team)
                .submittedBy(leader)
                .practiceDate(request.getPracticeDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
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
                .findByPerformanceAndPracticeDateBetweenAndStatusOrderByCreatedAtAsc(
                        performance, weekStart, weekEnd, ScheduleStatus.PENDING);

        // 슬롯 추적: date → list of (start, end) pairs
        Map<LocalDate, List<long[]>> clubRoomSlots = new HashMap<>();
        Map<LocalDate, List<long[]>> studentUnionSlots = new HashMap<>();
        // 치어룸 카운트: date → 배정된 팀 수
        Map<LocalDate, Integer> cheerRoomCount = new HashMap<>();
        // 이번 주 동방 배정받은 팀 ID
        Set<Long> teamsWithClubRoom = new HashSet<>();

        for (ScheduleRequest request : requests) {
            LocalDate date = request.getPracticeDate();
            LocalTime start = request.getStartTime();
            LocalTime end = request.getEndTime();
            Long teamId = request.getTeam().getId();
            boolean assigned = false;

            // 1. 동방: 이번 주 동방 미배정 팀에게 우선
            if (!teamsWithClubRoom.contains(teamId)) {
                if (noConflict(clubRoomSlots.getOrDefault(date, List.of()), start, end)) {
                    request.approve(RoomType.CLUB_ROOM);
                    clubRoomSlots.computeIfAbsent(date, k -> new ArrayList<>())
                            .add(toMinutes(start, end));
                    teamsWithClubRoom.add(teamId);
                    assigned = true;
                }
            }

            // 2. 학생회관 지하
            if (!assigned) {
                if (noConflict(studentUnionSlots.getOrDefault(date, List.of()), start, end)) {
                    request.approve(RoomType.STUDENT_UNION_BASEMENT);
                    studentUnionSlots.computeIfAbsent(date, k -> new ArrayList<>())
                            .add(toMinutes(start, end));
                    assigned = true;
                }
            }

            // 3. 치어룸 (수요일 18:30~20:30 이내만)
            if (!assigned && isWithinCheerRoomWindow(date, start, end)) {
                int count = cheerRoomCount.getOrDefault(date, 0);
                if (count < CHEER_ROOM_MAX_TEAMS) {
                    request.approve(RoomType.CHEER_ROOM);
                    cheerRoomCount.put(date, count + 1);
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

    private void validateDailyLimit(Team team, LocalDate date, LocalTime startTime, LocalTime endTime) {
        List<ScheduleRequest> existing = scheduleRequestRepository.findByTeamAndPracticeDate(team, date);
        long bookedMinutes = existing.stream()
                .filter(r -> r.getStatus() != ScheduleStatus.CANCELLED
                        && r.getStatus() != ScheduleStatus.REJECTED)
                .mapToLong(r -> ChronoUnit.MINUTES.between(r.getStartTime(), r.getEndTime()))
                .sum();
        long newMinutes = ChronoUnit.MINUTES.between(startTime, endTime);
        if (bookedMinutes + newMinutes > MAX_DAILY_MINUTES) {
            throw new RuntimeException("팀별 하루 최대 2시간을 초과합니다");
        }
    }

    // toMinutes: [startMinuteOfDay, endMinuteOfDay]
    private long[] toMinutes(LocalTime start, LocalTime end) {
        return new long[]{start.toSecondOfDay() / 60L, end.toSecondOfDay() / 60L};
    }

    private boolean noConflict(List<long[]> slots, LocalTime start, LocalTime end) {
        long s = start.toSecondOfDay() / 60L;
        long e = end.toSecondOfDay() / 60L;
        return slots.stream().noneMatch(slot -> slot[0] < e && slot[1] > s);
    }

    private boolean isWithinCheerRoomWindow(LocalDate date, LocalTime start, LocalTime end) {
        return date.getDayOfWeek() == DayOfWeek.WEDNESDAY
                && !start.isBefore(CHEER_ROOM_START)
                && !end.isAfter(CHEER_ROOM_END);
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
