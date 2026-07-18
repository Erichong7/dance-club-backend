package com.example.ToyProject_Board.domain.user.dto.response;

import com.example.ToyProject_Board.domain.user.SignupStatus;
import com.example.ToyProject_Board.domain.user.User;
import com.example.ToyProject_Board.domain.user.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Schema(description = "회원 검색 응답")
public class UserSearchResponse {

    @Schema(description = "사용자의 id")
    private Long id;

    @Schema(description = "사용자의 이메일")
    private String email;

    @Schema(description = "사용자의 닉네임")
    private String nickName;

    @Schema(description = "사용자의 역할")
    private UserRole role;

    @Schema(description = "사용자의 승인 여부")
    private SignupStatus signupStatus;

    @Schema(description = "사용자의 가입일")
    private LocalDateTime createdAt;

    public UserSearchResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.nickName = user.getNickname();
        this.role = user.getRole();
        this.signupStatus = user.getSignupStatus();
        this.createdAt = user.getCreatedAt();
    }
}
