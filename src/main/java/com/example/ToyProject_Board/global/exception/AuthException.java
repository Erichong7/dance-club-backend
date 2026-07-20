package com.example.ToyProject_Board.global.exception;

import lombok.Getter;

// 로그인 실패 사유(자격 불일치 / 승인 대기 / 거절)를 프론트에서 구분할 수 있도록 code를 함께 싣는 예외.
@Getter
public class AuthException extends RuntimeException {

    private final String code;

    public AuthException(String code, String message) {
        super(message);
        this.code = code;
    }
}
