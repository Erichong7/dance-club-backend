package com.example.ToyProject_Board.domain.schedule.dto.request;

import com.example.ToyProject_Board.domain.schedule.RoomType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "연습실 배정 요청")
public class AssignRoomRequest {

    @Schema(description = "배정할 연습실 종류", example = "CLUB_ROOM")
    @NotNull
    private RoomType room;
}
