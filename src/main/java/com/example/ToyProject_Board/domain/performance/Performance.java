package com.example.ToyProject_Board.domain.performance;

import com.example.ToyProject_Board.domain.team.Team;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "performances")
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Performance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate performanceDate;

    private String description;

    @OneToMany(mappedBy = "performance")
    private List<Team> teams;

    @CreatedDate
    private LocalDateTime createdAt;

    @Builder
    public Performance(String name, LocalDate performanceDate, String description) {
        this.name = name;
        this.performanceDate = performanceDate;
        this.description = description;
    }
}
