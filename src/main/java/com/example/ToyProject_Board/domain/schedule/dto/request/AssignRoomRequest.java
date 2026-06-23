package com.example.ToyProject_Board.domain.schedule.dto.request;

import com.example.ToyProject_Board.domain.schedule.RoomType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AssignRoomRequest {

    @NotNull
    private RoomType room;
}
