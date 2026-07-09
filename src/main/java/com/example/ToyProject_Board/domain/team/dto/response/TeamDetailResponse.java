package com.example.ToyProject_Board.domain.team.dto.response;

import com.example.ToyProject_Board.domain.team.Team;
import com.example.ToyProject_Board.domain.team.TeamMember;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Schema(description = "팀 상세 응답")
public class TeamDetailResponse {

    @Schema(description = "팀 ID", example = "1")
    private final Long id;

    @Schema(description = "팀 이름", example = "스트릿댄스팀")
    private final String name;

    @Schema(description = "팀 생성일시", example = "2026-03-01T10:00:00")
    private final LocalDateTime createdAt;

    @Schema(description = "팀에 소속된 팀원 목록")
    private final List<TeamMemberResponse> members;

    public TeamDetailResponse(Team team, List<TeamMember> members) {
        this.id = team.getId();
        this.name = team.getName();
        this.createdAt = team.getCreatedAt();
        this.members = members.stream().map(TeamMemberResponse::new).toList();
    }
}
