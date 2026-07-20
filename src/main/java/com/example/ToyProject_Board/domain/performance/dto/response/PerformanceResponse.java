package com.example.ToyProject_Board.domain.performance.dto.response;

import com.example.ToyProject_Board.domain.performance.Performance;
import com.example.ToyProject_Board.domain.team.dto.response.TeamResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Getter
@Schema(description = "공연 응답")
public class PerformanceResponse {

    @Schema(description = "공연 ID", example = "1")
    private final Long id;

    @Schema(description = "공연명", example = "2026 봄 정기공연")
    private final String name;

    @Schema(description = "공연 날짜", example = "2026-05-20")
    private final LocalDate performanceDate;

    @Schema(description = "공연 설명", example = "학내 대강당에서 진행되는 정기 공연입니다.")
    private final String description;

    @Schema(description = "공연에 등록된 팀들", example = "방송댄스팀, 코레오팀")
    private final List<TeamResponse> teams;

    @Schema(description = "공연 등록일시", example = "2026-03-01T10:00:00")
    private final LocalDateTime createdAt;

    public PerformanceResponse(Performance performance) {
        this.id = performance.getId();
        this.name = performance.getName();
        this.performanceDate = performance.getPerformanceDate();
        this.description = performance.getDescription();
        this.createdAt = performance.getCreatedAt();
        this.teams = performance.getTeams() == null
                ? Collections.emptyList()
                : performance.getTeams().stream().map(TeamResponse::new).toList();
    }
}
