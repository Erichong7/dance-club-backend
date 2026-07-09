package com.example.ToyProject_Board.domain.post.dto.response;

import com.example.ToyProject_Board.domain.post.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Schema(description = "게시글 상세 응답")
public class PostResponse {

    @Schema(description = "게시글 ID", example = "1")
    private Long id;

    @Schema(description = "게시글 제목", example = "동아리 정기 공연 안내")
    private String title;

    @Schema(description = "게시글 내용", example = "이번 학기 정기 공연은 5월 20일에 진행됩니다.")
    private String content;

    @Schema(description = "작성자 닉네임", example = "홍길동")
    private String nickname;

    @Schema(description = "게시글 작성일시", example = "2026-07-10T14:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "게시글 최종 수정일시", example = "2026-07-11T09:15:00")
    private LocalDateTime updatedAt;

    public PostResponse(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.nickname = post.getUser().getNickname();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
    }
}
