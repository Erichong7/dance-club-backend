package com.example.ToyProject_Board.domain.team.repository;

import com.example.ToyProject_Board.domain.performance.Performance;
import com.example.ToyProject_Board.domain.team.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {

    boolean existsByName(String name);

    List<Team> findByPerformance(Performance performance);
}
