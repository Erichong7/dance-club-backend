package com.example.ToyProject_Board.domain.team;

import com.example.ToyProject_Board.domain.performance.Performance;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "teams")
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToOne
    @JoinColumn(name = "performance_id")
    private Performance performance;

    @CreatedDate
    private LocalDateTime createdAt;

    @Builder
    public Team(String name, Performance performance) {
        this.name = name;
        this.performance = performance;
    }
}
