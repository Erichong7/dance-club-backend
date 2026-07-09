package com.example.ToyProject_Board.domain.post.controller;

import com.example.ToyProject_Board.domain.post.dto.request.PostCreateRequest;
import com.example.ToyProject_Board.domain.post.dto.request.PostUpdateRequest;
import com.example.ToyProject_Board.domain.post.dto.response.PostListResponse;
import com.example.ToyProject_Board.domain.post.dto.response.PostResponse;
import com.example.ToyProject_Board.domain.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Post", description = "게시글 CRUD API")
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @Operation(summary = "게시글 작성", description = "로그인한 사용자가 새 게시글을 작성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시글 작성 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패 (제목/내용 누락)")
    })
    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @Valid @RequestBody PostCreateRequest request,
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(postService.create(request, userId));
    }

    @Operation(summary = "게시글 목록 조회", description = "게시글을 페이지 단위로 조회합니다. 로그인하지 않아도 조회할 수 있습니다.")
    @ApiResponse(responseCode = "200", description = "게시글 목록 조회 성공")
    @SecurityRequirements
    @GetMapping
    public ResponseEntity<Page<PostListResponse>> getList(
            @PageableDefault(size = 10, sort = "createdAt",
            direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(postService.getList(pageable));
    }

    @Operation(summary = "게시글 단건 조회", description = "게시글 ID로 상세 내용을 조회합니다. 로그인하지 않아도 조회할 수 있습니다.")
    @ApiResponse(responseCode = "200", description = "게시글 조회 성공")
    @SecurityRequirements
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getOne(
            @Parameter(description = "게시글 ID", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(postService.getOne(id));
    }

    @Operation(summary = "게시글 수정", description = "작성자 본인만 게시글의 제목과 내용을 수정할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시글 수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패 (제목/내용 누락)")
    })
    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> update(
            @Parameter(description = "게시글 ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody PostUpdateRequest request,
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(postService.update(id, request, userId));
    }

    @Operation(summary = "게시글 삭제", description = "작성자 본인만 게시글을 삭제할 수 있습니다.")
    @ApiResponse(responseCode = "204", description = "게시글 삭제 성공")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "게시글 ID", example = "1") @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        postService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}
