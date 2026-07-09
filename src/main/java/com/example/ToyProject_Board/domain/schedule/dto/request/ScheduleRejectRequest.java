package com.example.ToyProject_Board.domain.schedule.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "연습 일정 반려 요청")
public class ScheduleRejectRequest {

    @Schema(description = "반려 사유 등 관리자 메모", example = "해당 시간대는 다른 팀과 중복됩니다.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String adminNote;
}
