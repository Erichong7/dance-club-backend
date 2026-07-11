package com.example.ToyProject_Board.domain.schedule.controller;

import com.example.ToyProject_Board.domain.schedule.dto.request.AssignRoomRequest;
import com.example.ToyProject_Board.domain.schedule.dto.request.ScheduleAssignRequest;
import com.example.ToyProject_Board.domain.schedule.dto.request.ScheduleCreateRequest;
import com.example.ToyProject_Board.domain.schedule.dto.request.ScheduleRejectRequest;
import com.example.ToyProject_Board.domain.schedule.dto.response.ScheduleResponse;
import com.example.ToyProject_Board.domain.schedule.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Schedule", description = "연습실 예약(연습 일정) 관리 API")
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @Operation(summary = "연습 일정 신청", description = "팀이 특정 공연을 위한 연습 일정을 신청합니다. 신청 직후 상태는 PENDING입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "연습 일정 신청 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패 (공연/팀/날짜/시간 누락)")
    })
    @PostMapping
    public ResponseEntity<ScheduleResponse> create(
            @Valid @RequestBody ScheduleCreateRequest request,
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(scheduleService.create(request, userId));
    }

    @Operation(summary = "주간 연습 일정 조회", description = "특정 공연에 대해 해당 주(weekStart 기준)의 연습 일정을 페이지 단위로 조회합니다.")
    @ApiResponse(responseCode = "200", description = "주간 연습 일정 조회 성공")
    @GetMapping
    public ResponseEntity<Page<ScheduleResponse>> getByWeek(
            @Parameter(description = "대상 공연 ID", example = "1") @RequestParam Long performanceId,
            @Parameter(description = "조회할 주의 시작일 (월요일 등 기준일)", example = "2026-07-13")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @PageableDefault(size = 20, sort = "practiceDate", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(scheduleService.getByWeek(performanceId, weekStart, pageable));
    }

    @Operation(summary = "연습 일정 단건 조회", description = "연습 일정 ID로 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "연습 일정 조회 성공")
    @GetMapping("/{id}")
    public ResponseEntity<ScheduleResponse> getOne(
            @Parameter(description = "연습 일정 ID", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(scheduleService.getOne(id));
    }

    @Operation(summary = "팀별 연습 일정 조회", description = "특정 팀이 신청한 연습 일정을 페이지 단위로 조회합니다.")
    @ApiResponse(responseCode = "200", description = "팀별 연습 일정 조회 성공")
    @GetMapping("/team/{teamId}")
    public ResponseEntity<Page<ScheduleResponse>> getByTeam(
            @Parameter(description = "팀 ID", example = "1") @PathVariable Long teamId,
            @RequestAttribute("userId") Long userId,
            @PageableDefault(size = 20, sort = "practiceDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(scheduleService.getByTeam(teamId, userId, pageable));
    }

    @Operation(summary = "연습 일정 취소", description = "신청한 연습 일정을 취소합니다. 상태가 CANCELLED로 변경됩니다.")
    @ApiResponse(responseCode = "204", description = "연습 일정 취소 성공")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(
            @Parameter(description = "연습 일정 ID", example = "1") @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        scheduleService.cancel(id, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "연습 일정 반려", description = "관리자가 신청된 연습 일정을 반려합니다. 상태가 REJECTED로 변경됩니다.")
    @ApiResponse(responseCode = "200", description = "연습 일정 반려 성공")
    @PostMapping("/{id}/reject")
    public ResponseEntity<ScheduleResponse> reject(
            @Parameter(description = "연습 일정 ID", example = "1") @PathVariable Long id,
            @RequestBody ScheduleRejectRequest request,
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(scheduleService.reject(id, request, userId));
    }

    @Operation(summary = "연습실 재배정", description = "이미 배정된 연습 일정의 연습실을 다른 연습실로 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "연습실 재배정 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패 (연습실 종류 누락)")
    })
    @PutMapping("/{id}/room")
    public ResponseEntity<ScheduleResponse> reassignRoom(
            @Parameter(description = "연습 일정 ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody AssignRoomRequest request,
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(scheduleService.reassignRoom(id, request, userId));
    }

    @Operation(summary = "연습실 수동 배정", description = "관리자가 팀의 연습 일정을 직접 등록하면서 연습실을 지정해 바로 승인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "연습실 수동 배정 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패 (공연/팀/날짜/시간/연습실 누락)")
    })
    @PostMapping("/assign/manual")
    public ResponseEntity<ScheduleResponse> assignRoom(
            @Valid @RequestBody ScheduleAssignRequest request,
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(scheduleService.assignRoom(request, userId));
    }

    @Operation(summary = "주간 연습실 일괄 배정", description = "특정 공연의 해당 주(weekStart 기준)에 PENDING 상태인 연습 일정들에 연습실을 일괄 자동 배정합니다.")
    @ApiResponse(responseCode = "200", description = "주간 연습실 일괄 배정 성공")
    @PostMapping("/assign")
    public ResponseEntity<List<ScheduleResponse>> assignWeek(
            @Parameter(description = "대상 공연 ID", example = "1") @RequestParam Long performanceId,
            @Parameter(description = "배정할 주의 시작일", example = "2026-07-13")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(scheduleService.assignWeek(performanceId, weekStart, userId));
    }
}
