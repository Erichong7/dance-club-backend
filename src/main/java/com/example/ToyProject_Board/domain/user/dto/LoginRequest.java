package com.example.ToyProject_Board.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "로그인 요청")
public class LoginRequest {

    @Schema(description = "로그인에 사용할 이메일", example = "hong@example.com")
    @NotBlank
    private String email;

    @Schema(description = "비밀번호", example = "password1234")
    @NotBlank
    private String password;
}
