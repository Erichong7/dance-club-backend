package com.example.ToyProject_Board.domain.user.dto.response;

import com.example.ToyProject_Board.domain.user.ApprovalStatus;
import com.example.ToyProject_Board.domain.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "회원가입 요청 목록 응답")
public class SignupRequestListResponse {

    @Schema(description = "사용자의 id")
    private Long id;

    @Schema(description = "사용자의 이메일")
    private String email;

    @Schema(description = "사용자의 닉네임")
    private String nickName;

    @Schema(description = "사용자의 승인 여부")
    private ApprovalStatus approvalStatus;

    public SignupRequestListResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.nickName = user.getNickname();
        this.approvalStatus = user.getApprovalStatus();
    }
}
