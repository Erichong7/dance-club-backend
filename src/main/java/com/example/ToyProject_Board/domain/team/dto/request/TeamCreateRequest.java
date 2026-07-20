package com.example.ToyProject_Board.domain.team.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "팀 생성 요청")
public class TeamCreateRequest {

    @Schema(description = "속한 공연", example = "2026-2학기 정기 공연")
    private Long performanceId;

    @Schema(description = "팀 이름", example = "스트릿댄스팀", minLength = 1)
    @NotBlank
    private String name;
}
