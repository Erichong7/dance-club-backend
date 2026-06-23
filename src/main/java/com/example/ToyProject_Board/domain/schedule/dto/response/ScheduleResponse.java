package com.example.ToyProject_Board.domain.schedule.dto.response;

import com.example.ToyProject_Board.domain.schedule.RoomType;
import com.example.ToyProject_Board.domain.schedule.ScheduleRequest;
import com.example.ToyProject_Board.domain.schedule.ScheduleStatus;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
public class ScheduleResponse {

    private final Long id;
    private final Long performanceId;
    private final String performanceName;
    private final Long teamId;
    private final String teamName;
    private final String submittedByNickname;
    private final LocalDate practiceDate;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final RoomType assignedRoom;
    private final ScheduleStatus status;
    private final String adminNote;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public ScheduleResponse(ScheduleRequest request) {
        this.id = request.getId();
        this.performanceId = request.getPerformance().getId();
        this.performanceName = request.getPerformance().getName();
        this.teamId = request.getTeam().getId();
        this.teamName = request.getTeam().getName();
        this.submittedByNickname = request.getSubmittedBy().getNickname();
        this.practiceDate = request.getPracticeDate();
        this.startTime = request.getStartTime();
        this.endTime = request.getEndTime();
        this.assignedRoom = request.getAssignedRoom();
        this.status = request.getStatus();
        this.adminNote = request.getAdminNote();
        this.createdAt = request.getCreatedAt();
        this.updatedAt = request.getUpdatedAt();
    }
}
