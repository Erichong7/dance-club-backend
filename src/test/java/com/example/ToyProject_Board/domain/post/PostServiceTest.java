package com.example.ToyProject_Board.domain.post;

import com.example.ToyProject_Board.domain.post.dto.request.PostCreateRequest;
import com.example.ToyProject_Board.domain.post.dto.response.PostResponse;
import com.example.ToyProject_Board.domain.post.repository.PostRepository;
import com.example.ToyProject_Board.domain.post.service.PostService;
import com.example.ToyProject_Board.domain.user.User;
import com.example.ToyProject_Board.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {
    // todo 테스트 코드 작성

    @InjectMocks
    private PostService postService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    // 테스트용 유저 생성 헬퍼
    private User createUser() {
        User user = User.builder()
                .email("test@test.com")
                .password("test1234")
                .nickname("테스터")
                .build();

        return user;
    }

    // 테스트용 게시글 생성 헬퍼
    private Post createPost(User user) {
        return Post.builder()
                .title("테스트 제목")
                .content("테스트 내용")
                .user(user)
                .build();
    }

    /*
    게시글 작성 테스트
     */

    @Test
    @DisplayName("게시글 작성 성공")
    void createSuccess() {
        // given
        User user = createUser();
        PostCreateRequest request = new PostCreateRequest("테스트 제목", "테스트 내용");
        Post post = createPost(user);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(postRepository.save(any(Post.class))).willReturn(post);

        // when
        PostResponse response = postService.create(request, 1L);

        // then
        assertThat(response.getTitle()).isEqualTo("테스트 제목");
        assertThat(response.getContent()).isEqualTo("테스트 내용");
        assertThat(response.getNickname()).isEqualTo("테스터");
    }

    @Test
    @DisplayName("게시글 작성 실패 - 유저 없음")
    void createFailUserNotFound() {
        // given
        PostCreateRequest request = new PostCreateRequest("테스트 제목", "테스트 내용");
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.create(request, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("유저를 찾을 수 없습니다");
    }

}
