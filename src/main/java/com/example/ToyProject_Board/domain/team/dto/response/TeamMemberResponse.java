package com.example.ToyProject_Board.domain.team.dto.response;

import com.example.ToyProject_Board.domain.team.TeamMember;
import com.example.ToyProject_Board.domain.team.TeamMemberRole;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TeamMemberResponse {

    private final Long id;
    private final Long userId;
    private final String nickname;
    private final TeamMemberRole role;
    private final LocalDateTime createdAt;

    public TeamMemberResponse(TeamMember member) {
        this.id = member.getId();
        this.userId = member.getUser().getId();
        this.nickname = member.getUser().getNickname();
        this.role = member.getRole();
        this.createdAt = member.getCreatedAt();
    }
}
