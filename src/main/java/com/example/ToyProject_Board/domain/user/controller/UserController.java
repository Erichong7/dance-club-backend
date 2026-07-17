package com.example.ToyProject_Board.domain.user.controller;

import com.example.ToyProject_Board.domain.user.dto.response.UserDetailResponse;
import com.example.ToyProject_Board.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "사용자 정보 조회 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회", description = "인증된 사용자 본인의 이메일, 닉네임, 역할, 소속 팀 정보를 조회합니다. Authorization 헤더에 액세스 토큰이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공")
    })
    @GetMapping("/me")
    public ResponseEntity<UserDetailResponse> getMyInfo(@RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(userService.getMyInfo(userId));
    }
}