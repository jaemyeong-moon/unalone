package com.project.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.api.dto.community.CommunityPostRequest;
import com.project.api.dto.community.CommunityPostResponse;
import com.project.api.exception.BusinessException;
import com.project.api.exception.GlobalExceptionHandler;
import com.project.api.security.JwtAuthenticationFilter;
import com.project.api.security.JwtTokenProvider;
import com.project.api.service.CommunityService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommunityController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("CommunityController 테스트")
class CommunityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommunityService communityService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UsernamePasswordAuthenticationToken createAuth(Long userId) {
        return new UsernamePasswordAuthenticationToken(
                userId, null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    private CommunityPostResponse samplePostResponse(Long id, Long userId) {
        return new CommunityPostResponse(
                id, userId, "홍길동", "테스트 게시글", "게시글 내용입니다", "DAILY",
                0, LocalDateTime.now(), null, null, "ko", "PENDING", null, null);
    }

    @Nested
    @DisplayName("POST /api/community/posts")
    class CreatePost {

        @Test
        @DisplayName("인증된 사용자의 게시글 작성 성공")
        void createPost_success() throws Exception {
            // Arrange
            CommunityPostRequest request = new CommunityPostRequest("테스트 게시글", "게시글 내용입니다", "DAILY");
            CommunityPostResponse response = samplePostResponse(1L, 1L);
            given(communityService.createPost(eq(1L), any(CommunityPostRequest.class))).willReturn(response);

            // Act & Assert
            mockMvc.perform(post("/api/community/posts")
                            .with(authentication(createAuth(1L)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("게시글 작성 완료"))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.title").value("테스트 게시글"))
                    .andExpect(jsonPath("$.data.category").value("DAILY"));
        }

        @Test
        @DisplayName("인증 없이 게시글 작성 시 401 응답")
        void createPost_unauthorized() throws Exception {
            // Arrange
            CommunityPostRequest request = new CommunityPostRequest("제목", "내용", "DAILY");

            // Act & Assert
            mockMvc.perform(post("/api/community/posts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("제목 누락 시 400 응답")
        void createPost_missingTitle_badRequest() throws Exception {
            // Arrange
            String requestBody = """
                    {"title": "", "content": "내용입니다", "category": "DAILY"}
                    """;

            // Act & Assert
            mockMvc.perform(post("/api/community/posts")
                            .with(authentication(createAuth(1L)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("GET /api/community/posts")
    class GetPosts {

        @Test
        @DisplayName("게시글 목록 페이지네이션 조회 성공")
        void getPosts_success() throws Exception {
            // Arrange
            List<CommunityPostResponse> items = List.of(
                    samplePostResponse(1L, 1L),
                    samplePostResponse(2L, 2L)
            );
            Page<CommunityPostResponse> page = new PageImpl<>(items, PageRequest.of(0, 20), 2);
            given(communityService.getPosts(any(), any())).willReturn(page);

            // Act & Assert (permitAll endpoint)
            mockMvc.perform(get("/api/community/posts")
                            .param("page", "0")
                            .param("size", "20"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(2))
                    .andExpect(jsonPath("$.data.totalElements").value(2));
        }

        @Test
        @DisplayName("카테고리 필터링 조회 성공")
        void getPosts_withCategory_success() throws Exception {
            // Arrange
            List<CommunityPostResponse> items = List.of(samplePostResponse(1L, 1L));
            Page<CommunityPostResponse> page = new PageImpl<>(items, PageRequest.of(0, 20), 1);
            given(communityService.getPosts(eq("HEALTH"), any())).willReturn(page);

            // Act & Assert
            mockMvc.perform(get("/api/community/posts")
                            .param("category", "HEALTH"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content.length()").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/community/posts/{postId}")
    class GetPost {

        @Test
        @DisplayName("게시글 상세 조회 성공")
        void getPost_success() throws Exception {
            // Arrange
            CommunityPostResponse response = samplePostResponse(1L, 1L);
            given(communityService.getPost(1L)).willReturn(response);

            // Act & Assert
            mockMvc.perform(get("/api/community/posts/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.title").value("테스트 게시글"));
        }

        @Test
        @DisplayName("존재하지 않는 게시글 조회 시 404 응답")
        void getPost_notFound() throws Exception {
            // Arrange
            given(communityService.getPost(999L))
                    .willThrow(BusinessException.notFound("게시글을 찾을 수 없습니다"));

            // Act & Assert
            mockMvc.perform(get("/api/community/posts/999"))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("게시글을 찾을 수 없습니다"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/community/posts/{postId}")
    class DeletePost {

        @Test
        @DisplayName("본인 게시글 삭제 성공")
        void deletePost_success() throws Exception {
            // Arrange
            willDoNothing().given(communityService).deletePost(1L, 1L);

            // Act & Assert
            mockMvc.perform(delete("/api/community/posts/1")
                            .with(authentication(createAuth(1L))))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("게시글 삭제 완료"));
        }

        @Test
        @DisplayName("타인 게시글 삭제 시 401 응답")
        void deletePost_forbidden() throws Exception {
            // Arrange
            willThrow(BusinessException.unauthorized("본인의 게시글만 삭제할 수 있습니다"))
                    .given(communityService).deletePost(2L, 1L);

            // Act & Assert
            mockMvc.perform(delete("/api/community/posts/1")
                            .with(authentication(createAuth(2L))))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("본인의 게시글만 삭제할 수 있습니다"));
        }
    }
}
