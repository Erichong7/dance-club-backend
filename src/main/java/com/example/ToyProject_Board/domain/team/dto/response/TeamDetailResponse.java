package com.example.ToyProject_Board.domain.team.dto.response;

import com.example.ToyProject_Board.domain.team.Team;
import com.example.ToyProject_Board.domain.team.TeamMember;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class TeamDetailResponse {

    private final Long id;
    private final String name;
    private final LocalDateTime createdAt;
    private final List<TeamMemberResponse> members;

    public TeamDetailResponse(Team team, List<TeamMember> members) {
        this.id = team.getId();
        this.name = team.getName();
        this.createdAt = team.getCreatedAt();
        this.members = members.stream().map(TeamMemberResponse::new).toList();
    }
}
