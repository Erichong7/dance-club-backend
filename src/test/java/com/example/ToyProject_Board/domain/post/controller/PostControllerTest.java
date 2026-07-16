package com.example.ToyProject_Board.domain.post.controller;

import com.example.ToyProject_Board.domain.post.dto.request.PostCreateRequest;
import com.example.ToyProject_Board.domain.post.dto.request.PostUpdateRequest;
import com.example.ToyProject_Board.domain.post.dto.response.PostListResponse;
import com.example.ToyProject_Board.domain.post.dto.response.PostResponse;
import com.example.ToyProject_Board.domain.post.service.PostService;
import com.example.ToyProject_Board.domain.support.ControllerTestSupport;
import com.example.ToyProject_Board.global.security.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
@Import(SecurityConfig.class)
public class PostControllerTest extends ControllerTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("게시글 작성 성공")
    void 게시글_작성_성공() throws Exception {
        PostCreateRequest request = new PostCreateRequest("테스트 제목", "테스트 내용");
        PostResponse response = new PostResponse(1L, "테스트 제목", "테스트 내용", "테스터", null, null);
        given(postService.create(any(), any())).willReturn(response);

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("테스트 제목"))
                .andExpect(jsonPath("$.content").value("테스트 내용"))
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 작성 실패 - 빈 제목")
    void 빈_제목으로_게시글_작성_실패() throws Exception {
        PostCreateRequest request = new PostCreateRequest("", "테스트 내용");

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("userId", 1L))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 작성 실패 - 빈 내용")
    void 빈_내용으로_게시글_작성_실패() throws Exception {
        PostCreateRequest request = new PostCreateRequest("테스트 제목", "");

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("userId", 1L))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 목록 조회 성공")
    void 게시글_목록_조회_성공() throws Exception {
        Page<PostListResponse> page = new PageImpl<>(List.of(
                new PostListResponse(1L, "테스트 제목", "테스터", null)
        ));
        given(postService.getList(any())).willReturn(page);

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("테스트 제목"))
                .andExpect(jsonPath("$.content[0].nickname").value("테스터"))
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 단건 조회 성공")
    void 게시글_단건_조회_성공() throws Exception {
        PostResponse response = new PostResponse(1L, "테스트 제목", "테스트 내용", "테스터", null, null);
        given(postService.getOne(1L)).willReturn(response);

        mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("테스트 제목"))
                .andExpect(jsonPath("$.content").value("테스트 내용"))
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 수정 성공")
    void 게시글_수정_성공() throws Exception {
        PostUpdateRequest request = new PostUpdateRequest("수정된 제목", "수정된 내용");
        PostResponse response = new PostResponse(1L, "수정된 제목", "수정된 내용", "테스터", null, null);
        given(postService.update(eq(1L), any(), eq(1L))).willReturn(response);

        mockMvc.perform(put("/api/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("수정된 제목"))
                .andExpect(jsonPath("$.content").value("수정된 내용"))
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 수정 실패 - 빈 제목")
    void 빈_제목으로_게시글_수정_실패() throws Exception {
        PostUpdateRequest request = new PostUpdateRequest("", "수정된 내용");

        mockMvc.perform(put("/api/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("userId", 1L))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    void 게시글_삭제_성공() throws Exception {
        mockMvc.perform(delete("/api/posts/1")
                        .requestAttr("userId", 1L))
                .andExpect(status().isNoContent())
                .andDo(print());
    }
}