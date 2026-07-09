package com.example.ToyProject_Board.domain.team.dto.request;

import com.example.ToyProject_Board.domain.team.TeamMemberRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "팀원 추가 요청")
public class AddMemberRequest {

    @Schema(description = "추가할 사용자 ID", example = "2")
    @NotNull
    private Long userId;

    @Schema(description = "부여할 팀 내 역할", example = "MEMBER")
    @NotNull
    private TeamMemberRole role;
}
