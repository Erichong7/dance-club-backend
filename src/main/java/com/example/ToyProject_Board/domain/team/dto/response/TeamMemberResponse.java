package com.example.ToyProject_Board.domain.team.dto.response;

import com.example.ToyProject_Board.domain.team.TeamMember;
import com.example.ToyProject_Board.domain.team.TeamMemberRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Schema(description = "팀원 응답")
public class TeamMemberResponse {

    @Schema(description = "팀원 레코드 ID", example = "1")
    private final Long id;

    @Schema(description = "사용자 ID", example = "2")
    private final Long userId;

    @Schema(description = "사용자 닉네임", example = "홍길동")
    private final String nickname;

    @Schema(description = "팀 내 역할", example = "MEMBER")
    private final TeamMemberRole role;

    @Schema(description = "팀 가입일시", example = "2026-03-05T12:00:00")
    private final LocalDateTime createdAt;

    public TeamMemberResponse(TeamMember member) {
        this.id = member.getId();
        this.userId = member.getUser().getId();
        this.nickname = member.getUser().getNickname();
        this.role = member.getRole();
        this.createdAt = member.getCreatedAt();
    }
}
