package com.example.ToyProject_Board.domain.performance.controller;

import com.example.ToyProject_Board.domain.performance.dto.request.PerformanceCreateRequest;
import com.example.ToyProject_Board.domain.performance.dto.response.PerformanceResponse;
import com.example.ToyProject_Board.domain.performance.service.PerformanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Performance", description = "공연 관리 API")
@RestController
@RequestMapping("/api/performances")
@RequiredArgsConstructor
public class PerformanceController {

    private final PerformanceService performanceService;

    @Operation(summary = "공연 생성", description = "새로운 공연을 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "공연 생성 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패 (공연명/날짜 누락)")
    })
    @PostMapping
    public ResponseEntity<PerformanceResponse> create(
            @Valid @RequestBody PerformanceCreateRequest request,
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(performanceService.create(request, userId));
    }

    @Operation(summary = "공연 목록 조회", description = "등록된 모든 공연을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "공연 목록 조회 성공")
    @GetMapping
    public ResponseEntity<List<PerformanceResponse>> getAll() {
        return ResponseEntity.ok(performanceService.getAll());
    }

    @Operation(summary = "공연 단건 조회", description = "공연 ID로 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "공연 조회 성공")
    @GetMapping("/{id}")
    public ResponseEntity<PerformanceResponse> getOne(
            @Parameter(description = "공연 ID", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(performanceService.getOne(id));
    }

    @Operation(summary = "공연 삭제", description = "공연을 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "공연 삭제 성공")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "공연 ID", example = "1") @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        performanceService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}
