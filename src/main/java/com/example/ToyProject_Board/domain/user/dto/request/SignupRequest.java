package com.example.ToyProject_Board.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "회원가입 요청")
public class SignupRequest {

    @Schema(description = "가입에 사용할 이메일", example = "hong@example.com")
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;

    @Schema(description = "비밀번호 (최소 8자)", example = "password1234", minLength = 8)
    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    private String password;

    @Schema(description = "비밀번호 확인", example = "password1234")
    @NotBlank(message = "비밀번호 확인을 입력해주세요.")
    private String passwordConfirm;

    @Schema(description = "사용자의 닉네임", example = "홍길동")
    @NotBlank(message = "닉네임을 입력해주세요.")
    private String nickname;

    @Schema(description = "전화번호", example = "010-1234-5678")
    @NotBlank(message = "전화번호를 입력해주세요.")
    @Pattern(regexp = "^01[016789]-\\d{3,4}-\\d{4}$", message = "유효한 전화번호 형식이 아닙니다.")
    private String phoneNumber;
}