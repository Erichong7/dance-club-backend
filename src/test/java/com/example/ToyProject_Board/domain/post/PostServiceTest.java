package com.example.ToyProject_Board.domain.post;

import com.example.ToyProject_Board.domain.post.dto.request.PostCreateRequest;
import com.example.ToyProject_Board.domain.post.dto.request.PostUpdateRequest;
import com.example.ToyProject_Board.domain.post.dto.response.PostListResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    private User createUser() {
        return User.builder()
                .email("test@test.com")
                .password("test1234")
                .nickname("테스터")
                .build();
    }

    private Post createPost(User user) {
        return Post.builder()
                .title("테스트 제목")
                .content("테스트 내용")
                .user(user)
                .build();
    }

    @Test
    @DisplayName("게시글 작성 성공")
    void 게시글_작성_성공() {
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
    void 존재하지_않는_유저의_게시글_작성_실패() {
        // given
        PostCreateRequest request = new PostCreateRequest("테스트 제목", "테스트 내용");
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.create(request, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("유저를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("게시글 목록 조회 성공")
    void 게시글_목록_조회_성공() {
        // given
        User user = createUser();
        Post post1 = createPost(user);
        Post post2 = createPost(user);
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Post> postPage = new PageImpl<>(List.of(post1, post2));

        given(postRepository.findAll(pageable)).willReturn(postPage);

        // when
        Page<PostListResponse> response = postService.getList(pageable);

        // then
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getContent().get(0).getTitle()).isEqualTo("테스트 제목");
    }

    @Test
    @DisplayName("게시글 단건 조회 성공")
    void 게시글_단건_조회_성공() {
        // given
        User user = createUser();
        Post post = createPost(user);

        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        // when
        PostResponse response = postService.getOne(1L);

        // then
        assertThat(response.getTitle()).isEqualTo("테스트 제목");
        assertThat(response.getContent()).isEqualTo("테스트 내용");
        assertThat(response.getNickname()).isEqualTo("테스터");
    }

    @Test
    @DisplayName("게시글 단건 조회 실패 - 게시글 없음")
    void 존재하지_않는_게시글_조회_실패() {
        // given
        given(postRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.getOne(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("게시글을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("게시글 수정 성공")
    void 게시글_수정_성공() {
        // given
        User user = createUser();
        ReflectionTestUtils.setField(user, "id", 1L);
        Post post = createPost(user);

        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        // when
        PostResponse response = postService.update(1L, new PostUpdateRequest("수정된 제목", "수정된 내용"), 1L);

        // then
        assertThat(response.getTitle()).isEqualTo("수정된 제목");
        assertThat(response.getContent()).isEqualTo("수정된 내용");
    }

    @Test
    @DisplayName("게시글 수정 실패 - 권한 없음")
    void 권한없는_사용자의_게시글_수정_실패() {
        // given
        User user = createUser();
        ReflectionTestUtils.setField(user, "id", 1L);
        Post post = createPost(user);

        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> postService.update(1L, new PostUpdateRequest("수정된 제목", "수정된 내용"), 2L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("게시글 수정 권한이 없습니다");
    }

    @Test
    @DisplayName("게시글 수정 실패 - 게시글 없음")
    void 존재하지_않는_게시글_수정_실패() {
        // given
        given(postRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.update(1L, new PostUpdateRequest("수정된 제목", "수정된 내용"), 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("게시글을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    void 게시글_삭제_성공() {
        // given
        User user = createUser();
        ReflectionTestUtils.setField(user, "id", 1L);
        Post post = createPost(user);
        ReflectionTestUtils.setField(post, "id", 1L);

        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        // when & then
        assertThatCode(() -> postService.delete(1L, 1L))
                .doesNotThrowAnyException();
        verify(postRepository, times(1)).delete(post);
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 권한 없음")
    void 권한없는_사용자의_게시글_삭제_실패() {
        // given
        User user = createUser();
        ReflectionTestUtils.setField(user, "id", 1L);
        Post post = createPost(user);
        ReflectionTestUtils.setField(post, "id", 1L);

        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> postService.delete(1L, 2L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("게시글 삭제 권한이 없습니다");
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 게시글 없음")
    void 존재하지_않는_게시글_삭제_실패() {
        // given
        given(postRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.delete(1L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("게시글을 찾을 수 없습니다");
    }
}