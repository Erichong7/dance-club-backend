package com.example.ToyProject_Board.domain.performance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class PerformanceCreateRequest {

    @NotBlank
    private String name;

    @NotNull
    private LocalDate performanceDate;

    private String description;
}
