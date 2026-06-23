package com.example.ToyProject_Board.domain.team.repository;

import com.example.ToyProject_Board.domain.team.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {

    boolean existsByName(String name);
}
