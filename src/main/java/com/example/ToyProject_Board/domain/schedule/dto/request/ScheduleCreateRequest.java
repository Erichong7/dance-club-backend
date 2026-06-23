package com.example.ToyProject_Board.domain.schedule.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
public class ScheduleCreateRequest {

    @NotNull
    private Long performanceId;

    @NotNull
    private Long teamId;

    @NotNull
    private LocalDate practiceDate;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;
}
