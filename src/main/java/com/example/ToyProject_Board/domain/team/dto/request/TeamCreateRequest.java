package com.example.ToyProject_Board.domain.team.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TeamCreateRequest {

    @NotBlank
    private String name;
}
