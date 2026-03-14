package com.project.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.api.domain.CommunityPost;
import com.project.api.domain.User;
import com.project.api.dto.community.CommunityPostRequest;
import com.project.api.dto.community.CommunityPostResponse;
import com.project.api.exception.BusinessException;
import com.project.api.repository.CommentRepository;
import com.project.api.repository.CommunityPostRepository;
import com.project.api.repository.PostQualityLogRepository;
import com.project.api.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommunityService 테스트")
class CommunityServiceTest {

    @InjectMocks
    private CommunityService communityService;

    @Mock
    private CommunityPostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostQualityLogRepository qualityLogRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    private User createTestUser(Long id) {
        return User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .name("홍길동")
                .phone("01012345678")
                .role(User.Role.ROLE_USER)
                .build();
    }

    @Nested
    @DisplayName("createPost 메서드")
    class CreatePost {

        @Test
        @DisplayName("DAILY 카테고리 게시글 작성 성공")
        void createPost_daily_success() {
            // Arrange
            Long userId = 1L;
            User user = createTestUser(userId);
            CommunityPostRequest request = new CommunityPostRequest("일상 게시글", "오늘의 일상입니다", "DAILY");

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(postRepository.save(any(CommunityPost.class))).willAnswer(inv -> inv.getArgument(0));

            // Act
            CommunityPostResponse response = communityService.createPost(userId, request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.title()).isEqualTo("일상 게시글");
            assertThat(response.content()).isEqualTo("오늘의 일상입니다");
            assertThat(response.category()).isEqualTo("DAILY");
            verify(postRepository).save(any(CommunityPost.class));
        }

        @Test
        @DisplayName("HEALTH 카테고리 게시글 작성 성공")
        void createPost_health_success() {
            // Arrange
            Long userId = 1L;
            User user = createTestUser(userId);
            CommunityPostRequest request = new CommunityPostRequest("건강 게시글", "건강 관련 내용", "HEALTH");

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(postRepository.save(any(CommunityPost.class))).willAnswer(inv -> inv.getArgument(0));

            // Act
            CommunityPostResponse response = communityService.createPost(userId, request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.category()).isEqualTo("HEALTH");
        }

        @Test
        @DisplayName("잘못된 카테고리 값은 DAILY로 기본 설정")
        void createPost_invalidCategory_defaultsToDaily() {
            // Arrange
            Long userId = 1L;
            User user = createTestUser(userId);
            CommunityPostRequest request = new CommunityPostRequest("게시글", "내용", "INVALID_CATEGORY");

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(postRepository.save(any(CommunityPost.class))).willAnswer(inv -> inv.getArgument(0));

            // Act
            CommunityPostResponse response = communityService.createPost(userId, request);

            // Assert
            assertThat(response.category()).isEqualTo("DAILY");
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 게시글 작성 시 NOT_FOUND 예외")
        void createPost_userNotFound() {
            // Arrange
            Long userId = 999L;
            CommunityPostRequest request = new CommunityPostRequest("제목", "내용", "DAILY");
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> communityService.createPost(userId, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("사용자를 찾을 수 없습니다")
                    .extracting("status")
                    .isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("getPosts 메서드")
    class GetPosts {

        @Test
        @DisplayName("전체 게시글 페이지네이션 조회 성공")
        void getPosts_all_success() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            User user = createTestUser(1L);
            CommunityPost post = CommunityPost.builder()
                    .user(user).title("테스트").content("내용").category(CommunityPost.PostCategory.DAILY)
                    .build();

            Page<CommunityPost> postPage = new PageImpl<>(List.of(post), pageable, 1);
            given(postRepository.findAllWithUser(pageable)).willReturn(postPage);
            given(commentRepository.countByPostId(any())).willReturn(3L);

            // Act
            Page<CommunityPostResponse> result = communityService.getPosts(null, pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).commentCount()).isEqualTo(3);
            verify(postRepository).findAllWithUser(pageable);
        }

        @Test
        @DisplayName("카테고리 필터링 조회 성공")
        void getPosts_withCategory_success() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            User user = createTestUser(1L);
            CommunityPost post = CommunityPost.builder()
                    .user(user).title("건강").content("건강 내용").category(CommunityPost.PostCategory.HEALTH)
                    .build();

            Page<CommunityPost> postPage = new PageImpl<>(List.of(post), pageable, 1);
            given(postRepository.findByCategoryWithUser(CommunityPost.PostCategory.HEALTH, pageable)).willReturn(postPage);
            given(commentRepository.countByPostId(any())).willReturn(0L);

            // Act
            Page<CommunityPostResponse> result = communityService.getPosts("HEALTH", pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            verify(postRepository).findByCategoryWithUser(CommunityPost.PostCategory.HEALTH, pageable);
        }
    }

    @Nested
    @DisplayName("getPost 메서드")
    class GetPost {

        @Test
        @DisplayName("게시글 상세 조회 성공")
        void getPost_success() {
            // Arrange
            Long postId = 1L;
            User user = createTestUser(1L);
            CommunityPost post = CommunityPost.builder()
                    .user(user).title("테스트 게시글").content("상세 내용").category(CommunityPost.PostCategory.DAILY)
                    .build();

            given(postRepository.findById(postId)).willReturn(Optional.of(post));
            given(commentRepository.countByPostId(postId)).willReturn(5L);

            // Act
            CommunityPostResponse response = communityService.getPost(postId);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.title()).isEqualTo("테스트 게시글");
            assertThat(response.commentCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("존재하지 않는 게시글 조회 시 NOT_FOUND 예외")
        void getPost_notFound() {
            // Arrange
            given(postRepository.findById(999L)).willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> communityService.getPost(999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("게시글을 찾을 수 없습니다")
                    .extracting("status")
                    .isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("deletePost 메서드")
    class DeletePost {

        @Test
        @DisplayName("본인 게시글 삭제 성공")
        void deletePost_owner_success() {
            // Arrange
            Long userId = 1L;
            Long postId = 1L;
            User user = createTestUser(userId);
            CommunityPost post = CommunityPost.builder()
                    .user(user).title("삭제할 게시글").content("내용").category(CommunityPost.PostCategory.DAILY)
                    .build();

            given(postRepository.findById(postId)).willReturn(Optional.of(post));

            // Act
            communityService.deletePost(userId, postId);

            // Assert
            verify(postRepository).delete(post);
        }

        @Test
        @DisplayName("타인의 게시글 삭제 시도 시 UNAUTHORIZED 예외")
        void deletePost_notOwner_throwsException() {
            // Arrange
            Long ownerId = 1L;
            Long requesterId = 2L;
            Long postId = 1L;

            User owner = createTestUser(ownerId);
            CommunityPost post = CommunityPost.builder()
                    .user(owner).title("게시글").content("내용").category(CommunityPost.PostCategory.DAILY)
                    .build();

            given(postRepository.findById(postId)).willReturn(Optional.of(post));

            // Act & Assert
            assertThatThrownBy(() -> communityService.deletePost(requesterId, postId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("본인의 게시글만 삭제할 수 있습니다");

            verify(postRepository, never()).delete(any());
        }

        @Test
        @DisplayName("존재하지 않는 게시글 삭제 시 NOT_FOUND 예외")
        void deletePost_notFound() {
            // Arrange
            given(postRepository.findById(999L)).willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> communityService.deletePost(1L, 999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("게시글을 찾을 수 없습니다");
        }
    }
}
