package com.example.ToyProject_Board.domain.user.dto;

import com.example.ToyProject_Board.domain.user.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "사용자 정보 응답")
public class UserDetailResponse {

    @Schema(description = "사용자의 id")
    private Long id;

    @Schema(description = "사용자의 이메일")
    private String email;

    @Schema(description = "사용자의 닉네임")
    private String nickName;

    @Schema(description = "사용자의 역할")
    private UserRole role;

    @Schema(description = "사용자의 팀 id들")
    private List<Long> teamIds;

    @Schema(description = "사용자의 팀 이름들")
    private List<String> teamNames;
}
