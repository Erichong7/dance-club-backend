package com.example.ToyProject_Board.domain.user.controller;

import com.example.ToyProject_Board.domain.user.dto.LoginRequest;
import com.example.ToyProject_Board.domain.user.dto.SignupRequest;
import com.example.ToyProject_Board.domain.user.dto.TokenResponse;
import com.example.ToyProject_Board.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "회원가입, 로그인 등 인증 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 닉네임으로 신규 회원을 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패 (이메일 형식, 비밀번호 길이 등)")
    })
    @SecurityRequirements
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequest request) {
        userService.signup(request);
        return ResponseEntity.ok("회원가입 성공");
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 액세스 토큰과 리프레시 토큰을 발급받습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공, 토큰 발급"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패")
    })
    @SecurityRequirements
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @Operation(summary = "토큰 재발급", description = "리프레시 토큰으로 액세스/리프레시 토큰을 재발급합니다. Refresh Token Rotation(RTR) 정책에 따라 기존 리프레시 토큰은 즉시 무효화됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 재발급 성공")
    })
    @SecurityRequirements
    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissue(
            @Parameter(in = ParameterIn.HEADER, name = "Refresh-Token", description = "로그인 시 발급받은 리프레시 토큰", required = true)
            @RequestHeader("Refresh-Token") String refreshToken) {
        return ResponseEntity.ok(userService.reissue(refreshToken));
    }

    @Operation(summary = "로그아웃", description = "현재 로그인된 사용자의 리프레시 토큰을 무효화합니다. Authorization 헤더에 액세스 토큰이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    })
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestAttribute("userId") Long userId) {
        userService.logout(userId);
        return ResponseEntity.ok("로그아웃 성공");
    }
}
