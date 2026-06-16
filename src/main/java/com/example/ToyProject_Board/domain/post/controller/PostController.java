package com.example.ToyProject_Board.domain.post.controller;

import com.example.ToyProject_Board.domain.post.dto.request.PostCreateRequest;
import com.example.ToyProject_Board.domain.post.dto.request.PostUpdateRequest;
import com.example.ToyProject_Board.domain.post.dto.response.PostListResponse;
import com.example.ToyProject_Board.domain.post.dto.response.PostResponse;
import com.example.ToyProject_Board.domain.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

     // 게시글 작성
    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @Valid @RequestBody PostCreateRequest request,
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(postService.create(request, userId));
    }

    @GetMapping
    public ResponseEntity<Page<PostListResponse>> getList(
            @PageableDefault(size = 10, sort = "createdAt",
            direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(postService.getList(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getOne(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody PostUpdateRequest request,
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(postService.update(id, request, userId));
    }
}
