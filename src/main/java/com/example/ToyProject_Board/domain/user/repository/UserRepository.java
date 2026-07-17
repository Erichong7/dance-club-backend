package com.example.ToyProject_Board.domain.user.repository;

import com.example.ToyProject_Board.domain.user.ApprovalStatus;
import com.example.ToyProject_Board.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Page<User> findByApprovalStatusNot(ApprovalStatus approvalStatus, Pageable pageable);
}
