package com.example.ToyProject_Board.domain.schedule.dto.request;

import com.example.ToyProject_Board.domain.schedule.RoomType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
@Schema(description = "연습 일정 신청 요청")
public class ScheduleCreateRequest {

    @Schema(description = "대상 공연 ID", example = "1")
    @NotNull
    private Long performanceId;

    @Schema(description = "신청하는 팀 ID", example = "1")
    @NotNull
    private Long teamId;

    @Schema(description = "연습 희망 날짜", example = "2026-07-15")
    @NotNull
    private LocalDate practiceDate;

    @Schema(description = "연습 시작 시간", example = "18:00:00")
    @NotNull
    private LocalTime startTime;

    @Schema(description = "연습 종료 시간", example = "20:00:00")
    @NotNull
    private LocalTime endTime;

    @Schema(description = "후보 연습실", example = "UNDERGROUND_PARKING")
    @NotNull
    private RoomType alternativeRoom;
}
