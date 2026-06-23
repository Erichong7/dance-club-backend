package com.example.ToyProject_Board.domain.schedule.repository;

import com.example.ToyProject_Board.domain.performance.Performance;
import com.example.ToyProject_Board.domain.schedule.ScheduleRequest;
import com.example.ToyProject_Board.domain.schedule.ScheduleStatus;
import com.example.ToyProject_Board.domain.team.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleRequestRepository extends JpaRepository<ScheduleRequest, Long> {

    List<ScheduleRequest> findByTeamAndPracticeDate(Team team, LocalDate practiceDate);

    List<ScheduleRequest> findByPerformanceAndPracticeDateBetweenAndStatusOrderByCreatedAtAsc(
            Performance performance, LocalDate start, LocalDate end, ScheduleStatus status);

    Page<ScheduleRequest> findByPerformanceAndPracticeDateBetween(
            Performance performance, LocalDate start, LocalDate end, Pageable pageable);

    Page<ScheduleRequest> findByTeam(Team team, Pageable pageable);
}
