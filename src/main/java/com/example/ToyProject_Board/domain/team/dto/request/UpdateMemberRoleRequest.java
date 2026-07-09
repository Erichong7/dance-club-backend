package com.example.ToyProject_Board.domain.team.dto.request;

import com.example.ToyProject_Board.domain.team.TeamMemberRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "팀원 역할 변경 요청")
public class UpdateMemberRoleRequest {

    @Schema(description = "변경할 팀 내 역할", example = "DEPUTY")
    @NotNull
    private TeamMemberRole role;
}
