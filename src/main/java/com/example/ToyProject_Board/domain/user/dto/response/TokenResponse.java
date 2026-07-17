package com.example.ToyProject_Board.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "로그인/토큰 재발급 응답")
public class TokenResponse {

    @Schema(description = "API 인증에 사용하는 액세스 토큰", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.abc123")
    private String accessToken;

    @Schema(description = "액세스 토큰 재발급에 사용하는 리프레시 토큰", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.def456")
    private String refreshToken;
}
