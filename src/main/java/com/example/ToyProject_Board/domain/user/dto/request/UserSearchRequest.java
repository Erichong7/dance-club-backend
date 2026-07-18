package com.example.ToyProject_Board.domain.user.dto.request;

import com.example.ToyProject_Board.domain.user.SignupStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원 검색 조건")
public class UserSearchRequest {

    @Schema(description = "닉네임 검색 키워드 (부분 일치)", example = "홍길동")
    private String nickname;

    @Schema(description = "이메일 검색 키워드 (부분 일치)", example = "test@")
    private String email;

    @Schema(description = "가입 승인 상태", example = "APPROVED")
    private SignupStatus signupStatus;
}
