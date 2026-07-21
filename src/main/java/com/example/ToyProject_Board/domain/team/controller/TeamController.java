package com.example.ToyProject_Board.domain.team.controller;

import com.example.ToyProject_Board.domain.team.dto.request.AddMemberRequest;
import com.example.ToyProject_Board.domain.team.dto.request.TeamCreateRequest;
import com.example.ToyProject_Board.domain.team.dto.request.UpdateMemberRoleRequest;
import com.example.ToyProject_Board.domain.team.dto.response.TeamDetailResponse;
import com.example.ToyProject_Board.domain.team.dto.response.TeamMemberResponse;
import com.example.ToyProject_Board.domain.team.dto.response.TeamResponse;
import com.example.ToyProject_Board.domain.team.service.TeamService;
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

@Tag(name = "Team", description = "팀 및 팀원 관리 API")
@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @Operation(summary = "팀 생성", description = "새로운 팀을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "팀 생성 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패 (팀 이름 누락)")
    })
    @PostMapping
    public ResponseEntity<TeamResponse> create(
            @Valid @RequestBody TeamCreateRequest request,
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(teamService.createTeam(request, userId));
    }

    @Operation(summary = "팀 목록 조회", description = "등록된 모든 팀을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "팀 목록 조회 성공")
    @GetMapping
    public ResponseEntity<List<TeamResponse>> getAll() {
        return ResponseEntity.ok(teamService.getAllTeams());
    }

    @Operation(summary = "팀 상세 조회", description = "팀 ID로 팀 정보와 소속 팀원 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "팀 상세 조회 성공")
    @GetMapping("/{id}")
    public ResponseEntity<TeamDetailResponse> getOne(
            @Parameter(description = "팀 ID", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(teamService.getTeam(id));
    }

    @Operation(summary = "팀원 추가", description = "팀에 새로운 팀원을 추가합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "팀원 추가 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패 (사용자 ID/역할 누락)")
    })
    @PostMapping("/{id}/members")
    public ResponseEntity<TeamMemberResponse> addMember(
            @Parameter(description = "팀 ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody AddMemberRequest request,
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(teamService.addMember(id, request, userId));
    }

    @Operation(summary = "팀원 역할 변경", description = "팀에 소속된 특정 팀원의 역할을 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "팀원 역할 변경 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패 (역할 누락)")
    })
    @PutMapping("/{id}/members/{targetUserId}/role")
    public ResponseEntity<TeamMemberResponse> updateMemberRole(
            @Parameter(description = "팀 ID", example = "1") @PathVariable Long id,
            @Parameter(description = "역할을 변경할 대상 사용자 ID", example = "2") @PathVariable Long targetUserId,
            @Valid @RequestBody UpdateMemberRoleRequest request,
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(teamService.updateMemberRole(id, targetUserId, request, userId));
    }

    @Operation(summary = "팀 제거", description = "팀을 제거합니다.")
    @ApiResponse(responseCode = "204", description = "팀 제거 성공")
    @DeleteMapping("/{teamId}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "팀 ID", example = "1") @PathVariable Long teamId,
            @RequestAttribute("userId") Long userId) {
        teamService.delete(teamId, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "팀원 제거", description = "팀에서 특정 팀원을 제거합니다.")
    @ApiResponse(responseCode = "204", description = "팀원 제거 성공")
    @DeleteMapping("/{id}/members/{targetUserId}")
    public ResponseEntity<Void> removeMember(
            @Parameter(description = "팀 ID", example = "1") @PathVariable Long id,
            @Parameter(description = "제거할 대상 사용자 ID", example = "2") @PathVariable Long targetUserId,
            @RequestAttribute("userId") Long userId) {
        teamService.removeMember(id, targetUserId, userId);
        return ResponseEntity.noContent().build();
    }
}
