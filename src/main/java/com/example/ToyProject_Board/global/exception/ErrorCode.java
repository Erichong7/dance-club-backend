package com.example.ToyProject_Board.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // COMMON
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "입력 값이 올바르지 않습니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 내부 오류가 발생했습니다"),
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "C003", "인증이 필요합니다"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "C004", "접근 권한이 없습니다"),

    // USER / AUTH
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "유저를 찾을 수 없습니다"),
    ADMIN_ONLY(HttpStatus.FORBIDDEN, "U002", "관리자 권한이 필요합니다"),
    EMAIL_DUPLICATE(HttpStatus.CONFLICT, "U003", "이미 사용중인 이메일입니다"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "U004", "이메일 또는 비밀번호가 틀렸습니다"),
    SIGNUP_REJECTED(HttpStatus.FORBIDDEN, "U005", "회원가입 요청이 거절되었습니다."),
    SIGNUP_PENDING(HttpStatus.FORBIDDEN, "U006", "회원가입 승인 전입니다"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "U007", "유효하지 않은 토큰입니다"),
    TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "U008", "토큰이 일치하지 않습니다"),
    PASSWORD_CONFIRM_MISMATCH(HttpStatus.BAD_REQUEST, "U009", "비밀번호가 일치하지 않습니다"),

    // POST
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "게시글을 찾을 수 없습니다"),

    // TEAM
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "T001", "팀을 찾을 수 없습니다"),
    TEAM_NAME_DUPLICATE(HttpStatus.CONFLICT, "T002", "이미 존재하는 팀 이름입니다"),
    TEAM_PERFORMANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "T003", "존재하지 않는 공연입니다."),
    TEAM_MEMBER_DUPLICATE(HttpStatus.CONFLICT, "T004", "이미 팀에 속해있는 멤버입니다"),
    TEAM_LEADER_EXISTS(HttpStatus.CONFLICT, "T005", "이미 팀장이 존재합니다"),
    TEAM_DEPUTY_EXISTS(HttpStatus.CONFLICT, "T006", "이미 부팀장이 존재합니다"),
    TEAM_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "T007", "팀 멤버를 찾을 수 없습니다"),
    TEAM_LEADER_NOT_FOUND(HttpStatus.NOT_FOUND, "T008", "팀에 리더가 존재하지 않습니다."),
    NOT_TEAM_MEMBER(HttpStatus.FORBIDDEN, "T009", "해당 팀의 멤버가 아닙니다"),

    // SCHEDULE
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "신청을 찾을 수 없습니다"),
    SCHEDULE_CREATE_FORBIDDEN(HttpStatus.FORBIDDEN, "S002", "팀장 또는 부팀장만 신청할 수 있습니다"),
    SCHEDULE_CANCEL_FORBIDDEN(HttpStatus.FORBIDDEN, "S003", "취소 권한이 없습니다"),
    SCHEDULE_ALREADY_CLOSED(HttpStatus.CONFLICT, "S004", "이미 종료된 신청입니다"),
    SCHEDULE_NOT_PENDING(HttpStatus.CONFLICT, "S005", "대기 중인 신청만 거절할 수 있습니다"),
    SCHEDULE_NOT_APPROVED(HttpStatus.CONFLICT, "S006", "승인된 신청만 방을 재배정할 수 있습니다"),
    SCHEDULE_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "S007", "삭제 권한이 없습니다"),
    SCHEDULE_DEADLINE_PASSED(HttpStatus.BAD_REQUEST, "S008", "제출 기한이 지났습니다 (연습일 기준 이전 주 일요일까지 신청 가능합니다)"),
    SCHEDULE_INVALID_TIME_RANGE(HttpStatus.BAD_REQUEST, "S009", "시작 시간과 종료 시간이 같을 수 없습니다"),

    // PERFORMANCE
    PERFORMANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "PF001", "공연을 찾을 수 없습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
