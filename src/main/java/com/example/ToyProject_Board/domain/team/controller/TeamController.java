package com.example.ToyProject_Board.domain.team.controller;

import com.example.ToyProject_Board.domain.team.dto.request.AddMemberRequest;
import com.example.ToyProject_Board.domain.team.dto.request.TeamCreateRequest;
import com.example.ToyProject_Board.domain.team.dto.request.UpdateMemberRoleRequest;
import com.example.ToyProject_Board.domain.team.dto.response.TeamDetailResponse;
import com.example.ToyProject_Board.domain.team.dto.response.TeamMemberResponse;
import com.example.ToyProject_Board.domain.team.dto.response.TeamResponse;
import com.example.ToyProject_Board.domain.team.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    public ResponseEntity<TeamResponse> create(
            @Valid @RequestBody TeamCreateRequest request,
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(teamService.createTeam(request, userId));
    }

    @GetMapping
    public ResponseEntity<List<TeamResponse>> getAll() {
        return ResponseEntity.ok(teamService.getAllTeams());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamDetailResponse> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(teamService.getTeam(id));
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<TeamMemberResponse> addMember(
            @PathVariable Long id,
            @Valid @RequestBody AddMemberRequest request,
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(teamService.addMember(id, request, userId));
    }

    @PutMapping("/{id}/members/{targetUserId}/role")
    public ResponseEntity<TeamMemberResponse> updateMemberRole(
            @PathVariable Long id,
            @PathVariable Long targetUserId,
            @Valid @RequestBody UpdateMemberRoleRequest request,
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(teamService.updateMemberRole(id, targetUserId, request, userId));
    }

    @DeleteMapping("/{id}/members/{targetUserId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long id,
            @PathVariable Long targetUserId,
            @RequestAttribute("userId") Long userId) {
        teamService.removeMember(id, targetUserId, userId);
        return ResponseEntity.noContent().build();
    }
}
