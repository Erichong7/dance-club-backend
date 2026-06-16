package com.example.ToyProject_Board.domain.post.dto.response;

import com.example.ToyProject_Board.domain.post.Post;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostListResponse {

    private Long id;
    private String title;
    private String nickname;
    private LocalDateTime createdAt;

    public PostListResponse(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.nickname = post.getUser().getNickname();
        this.createdAt = post.getCreatedAt();
    }

}
