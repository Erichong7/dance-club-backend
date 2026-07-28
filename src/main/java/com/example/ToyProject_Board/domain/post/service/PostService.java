package com.example.ToyProject_Board.domain.post.service;

import com.example.ToyProject_Board.domain.post.Post;
import com.example.ToyProject_Board.domain.post.dto.request.PostCreateRequest;
import com.example.ToyProject_Board.domain.post.dto.request.PostUpdateRequest;
import com.example.ToyProject_Board.domain.post.dto.response.PostListResponse;
import com.example.ToyProject_Board.domain.post.dto.response.PostResponse;
import com.example.ToyProject_Board.domain.post.repository.PostRepository;
import com.example.ToyProject_Board.domain.user.User;
import com.example.ToyProject_Board.domain.user.UserRole;
import com.example.ToyProject_Board.domain.user.repository.UserRepository;
import com.example.ToyProject_Board.global.exception.BusinessException;
import com.example.ToyProject_Board.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 게시글 작성
    @Transactional
    public PostResponse create(PostCreateRequest request, Long userId) {
        User user = findUserById(userId);
        verifyAdmin(user);

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .user(user)
                .build();

        return new PostResponse(postRepository.save(post));
    }

    // 게시글 목록 조회
    public Page<PostListResponse> getList(Pageable pageable) {
        return postRepository.findAll(pageable)
                .map(PostListResponse::new);
    }

    // 게시글 단건 조회
    public PostResponse getOne(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        return new PostResponse(post);
    }

    // 게시글 수정
    @Transactional
    public PostResponse update(Long postId, PostUpdateRequest request, Long userId) {
        verifyAdmin(findUserById(userId));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        post.update(request.getTitle(), request.getContent());
        return new PostResponse(post);
    }

    @Transactional
    public void delete(Long postId, Long userId) {
        verifyAdmin(findUserById(userId));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        postRepository.delete(post);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private void verifyAdmin(User user) {
        if (user.getRole() != UserRole.ADMIN) {
            throw new BusinessException(ErrorCode.ADMIN_ONLY);
        }
    }
}
