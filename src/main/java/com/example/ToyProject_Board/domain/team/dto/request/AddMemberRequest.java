package com.example.ToyProject_Board.domain.team.dto.request;

import com.example.ToyProject_Board.domain.team.TeamMemberRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AddMemberRequest {

    @NotNull
    private Long userId;

    @NotNull
    private TeamMemberRole role;
}
