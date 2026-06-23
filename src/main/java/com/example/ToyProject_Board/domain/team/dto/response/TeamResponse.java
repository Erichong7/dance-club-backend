package com.example.ToyProject_Board.domain.team.dto.response;

import com.example.ToyProject_Board.domain.team.Team;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TeamResponse {

    private final Long id;
    private final String name;
    private final LocalDateTime createdAt;

    public TeamResponse(Team team) {
        this.id = team.getId();
        this.name = team.getName();
        this.createdAt = team.getCreatedAt();
    }
}
