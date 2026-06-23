package com.example.ToyProject_Board.domain.team.repository;

import com.example.ToyProject_Board.domain.team.Team;
import com.example.ToyProject_Board.domain.team.TeamMember;
import com.example.ToyProject_Board.domain.team.TeamMemberRole;
import com.example.ToyProject_Board.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    Optional<TeamMember> findByTeamAndUser(Team team, User user);

    List<TeamMember> findByTeam(Team team);

    List<TeamMember> findByUser(User user);

    boolean existsByTeamAndUser(Team team, User user);

    boolean existsByTeamAndRole(Team team, TeamMemberRole role);

    void deleteByTeamAndUser(Team team, User user);
}
