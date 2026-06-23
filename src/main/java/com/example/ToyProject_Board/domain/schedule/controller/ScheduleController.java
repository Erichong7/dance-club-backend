package com.example.ToyProject_Board.domain.schedule.controller;

import com.example.ToyProject_Board.domain.schedule.dto.request.AssignRoomRequest;
import com.example.ToyProject_Board.domain.schedule.dto.request.ScheduleCreateRequest;
import com.example.ToyProject_Board.domain.schedule.dto.request.ScheduleRejectRequest;
import com.example.ToyProject_Board.domain.schedule.dto.response.ScheduleResponse;
import com.example.ToyProject_Board.domain.schedule.service.ScheduleService;
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

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping
    public ResponseEntity<ScheduleResponse> create(
            @Valid @RequestBody ScheduleCreateRequest request,
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(scheduleService.create(request, userId));
    }

    @GetMapping
    public ResponseEntity<Page<ScheduleResponse>> getByWeek(
            @RequestParam Long performanceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @PageableDefault(size = 20, sort = "practiceDate", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(scheduleService.getByWeek(performanceId, weekStart, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduleResponse> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(scheduleService.getOne(id));
    }

    @GetMapping("/team/{teamId}")
    public ResponseEntity<Page<ScheduleResponse>> getByTeam(
            @PathVariable Long teamId,
            @RequestAttribute("userId") Long userId,
            @PageableDefault(size = 20, sort = "practiceDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(scheduleService.getByTeam(teamId, userId, pageable));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        scheduleService.cancel(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ScheduleResponse> reject(
            @PathVariable Long id,
            @RequestBody ScheduleRejectRequest request,
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(scheduleService.reject(id, request, userId));
    }

    @PutMapping("/{id}/room")
    public ResponseEntity<ScheduleResponse> reassignRoom(
            @PathVariable Long id,
            @Valid @RequestBody AssignRoomRequest request,
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(scheduleService.reassignRoom(id, request, userId));
    }

    @PostMapping("/assign")
    public ResponseEntity<List<ScheduleResponse>> assignWeek(
            @RequestParam Long performanceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(scheduleService.assignWeek(performanceId, weekStart, userId));
    }
}
