package com.example.ToyProject_Board.domain.performance.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@Schema(description = "공연 생성 요청")
public class PerformanceCreateRequest {

    @Schema(description = "공연명", example = "2026 봄 정기공연")
    @NotBlank
    private String name;

    @Schema(description = "공연 날짜", example = "2026-05-20")
    @NotNull
    private LocalDate performanceDate;

    @Schema(description = "공연 설명", example = "학내 대강당에서 진행되는 정기 공연입니다.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;
}
