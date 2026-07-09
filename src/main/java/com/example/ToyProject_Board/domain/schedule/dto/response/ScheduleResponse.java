package com.example.ToyProject_Board.domain.schedule.dto.response;

import com.example.ToyProject_Board.domain.schedule.RoomType;
import com.example.ToyProject_Board.domain.schedule.ScheduleRequest;
import com.example.ToyProject_Board.domain.schedule.ScheduleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Schema(description = "연습 일정 응답")
public class ScheduleResponse {

    @Schema(description = "연습 일정 ID", example = "1")
    private final Long id;

    @Schema(description = "대상 공연 ID", example = "1")
    private final Long performanceId;

    @Schema(description = "대상 공연명", example = "2026 봄 정기공연")
    private final String performanceName;

    @Schema(description = "신청 팀 ID", example = "1")
    private final Long teamId;

    @Schema(description = "신청 팀명", example = "스트릿댄스팀")
    private final String teamName;

    @Schema(description = "신청자 닉네임", example = "홍길동")
    private final String submittedByNickname;

    @Schema(description = "연습 희망 날짜", example = "2026-07-15")
    private final LocalDate practiceDate;

    @Schema(description = "연습 시작 시간", example = "18:00:00")
    private final LocalTime startTime;

    @Schema(description = "연습 종료 시간", example = "20:00:00")
    private final LocalTime endTime;

    @Schema(description = "배정된 연습실 (미배정 시 null)", example = "CLUB_ROOM")
    private final RoomType assignedRoom;

    @Schema(description = "일정 상태", example = "PENDING")
    private final ScheduleStatus status;

    @Schema(description = "관리자 메모 (반려 사유 등)", example = "해당 시간대는 다른 팀과 중복됩니다.")
    private final String adminNote;

    @Schema(description = "신청 등록일시", example = "2026-07-10T09:00:00")
    private final LocalDateTime createdAt;

    @Schema(description = "최종 수정일시", example = "2026-07-11T10:30:00")
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
