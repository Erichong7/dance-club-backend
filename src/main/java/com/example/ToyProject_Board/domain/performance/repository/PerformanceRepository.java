package com.example.ToyProject_Board.domain.performance.repository;

import com.example.ToyProject_Board.domain.performance.Performance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerformanceRepository extends JpaRepository<Performance, Long> {
}
