package com.example.ToyProject_Board.domain.performance;

import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

public class PerformanceFixture {

    public static Performance create() {
        return Performance.builder()
                .name("2024 정기공연")
                .performanceDate(LocalDate.of(2024, 12, 31))
                .description("연말 정기공연")
                .build();
    }

    public static Performance createWithId(Long id) {
        Performance performance = create();
        ReflectionTestUtils.setField(performance, "id", id);
        return performance;
    }
}
