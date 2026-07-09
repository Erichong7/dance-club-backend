package com.example.ToyProject_Board.domain.post.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "게시글 수정 요청")
public class PostUpdateRequest {

    @Schema(description = "수정할 게시글 제목", example = "동아리 정기 공연 일정 변경 안내")
    @NotBlank(message = "제목을 입력해주세요.")
    private String title;

    @Schema(description = "수정할 게시글 내용", example = "공연 일정이 5월 27일로 변경되었습니다.")
    @NotBlank(message = "내용을 입력해주세요.")
    private String content;
}
