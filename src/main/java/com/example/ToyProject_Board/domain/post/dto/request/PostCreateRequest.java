package com.example.ToyProject_Board.domain.post.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "게시글 작성 요청")
public class PostCreateRequest {

    @Schema(description = "게시글 제목", example = "동아리 정기 공연 안내")
    @NotBlank(message = "제목은 입력해주세요.")
    private String title;

    @Schema(description = "게시글 내용", example = "이번 학기 정기 공연은 5월 20일에 진행됩니다.")
    @NotBlank(message = "내용은 입력해주세요.")
    private String content;
}
