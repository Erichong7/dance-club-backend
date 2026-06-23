package com.example.ToyProject_Board.domain.team.dto.request;

import com.example.ToyProject_Board.domain.team.TeamMemberRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateMemberRoleRequest {

    @NotNull
    private TeamMemberRole role;
}
