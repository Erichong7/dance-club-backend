package com.example.ToyProject_Board.domain.schedule;

import com.example.ToyProject_Board.domain.performance.Performance;
import com.example.ToyProject_Board.domain.team.Team;
import com.example.ToyProject_Board.domain.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "schedule_requests")
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ScheduleRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performance_id", nullable = false)
    private Performance performance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by", nullable = false)
    private User submittedBy;

    @Column(nullable = false)
    private LocalDate practiceDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    private RoomType assignedRoom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduleStatus status = ScheduleStatus.PENDING;

    private String adminNote;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public ScheduleRequest(Performance performance, Team team, User submittedBy,
                           LocalDate practiceDate, LocalTime startTime, LocalTime endTime) {
        this.performance = performance;
        this.team = team;
        this.submittedBy = submittedBy;
        this.practiceDate = practiceDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = ScheduleStatus.PENDING;
    }

    public void approve(RoomType assignedRoom) {
        this.status = ScheduleStatus.APPROVED;
        this.assignedRoom = assignedRoom;
    }

    public void reject(String adminNote) {
        this.status = ScheduleStatus.REJECTED;
        this.adminNote = adminNote;
    }

    public void cancel() {
        this.status = ScheduleStatus.CANCELLED;
    }

    public void reassignRoom(RoomType room) {
        this.assignedRoom = room;
    }
}
