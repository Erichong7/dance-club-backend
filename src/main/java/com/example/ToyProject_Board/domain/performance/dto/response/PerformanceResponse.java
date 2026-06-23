package com.example.ToyProject_Board.domain.performance.dto.response;

import com.example.ToyProject_Board.domain.performance.Performance;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class PerformanceResponse {

    private final Long id;
    private final String name;
    private final LocalDate performanceDate;
    private final String description;
    private final LocalDateTime createdAt;

    public PerformanceResponse(Performance performance) {
        this.id = performance.getId();
        this.name = performance.getName();
        this.performanceDate = performance.getPerformanceDate();
        this.description = performance.getDescription();
        this.createdAt = performance.getCreatedAt();
    }
}
