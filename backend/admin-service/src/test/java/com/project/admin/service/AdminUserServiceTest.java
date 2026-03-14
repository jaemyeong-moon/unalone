package com.project.admin.service;

import com.project.admin.domain.User;
import com.project.admin.dto.UserDetailResponse;
import com.project.admin.exception.InvalidRequestException;
import com.project.admin.exception.ResourceNotFoundException;
import com.project.admin.repository.CheckInRepository;
import com.project.admin.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminUserService 테스트")
class AdminUserServiceTest {

    @InjectMocks
    private AdminUserService adminUserService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CheckInRepository checkInRepository;

    private User createMockUser(Long id, String name, String email) {
        User user = mock(User.class);
        given(user.getId()).willReturn(id);
        given(user.getName()).willReturn(name);
        given(user.getEmail()).willReturn(email);
        given(user.getPhone()).willReturn("01012345678");
        given(user.getRole()).willReturn(User.Role.ROLE_USER);
        given(user.getStatus()).willReturn(User.UserStatus.ACTIVE);
        given(user.getCreatedAt()).willReturn(null);
        return user;
    }

    @Nested
    @DisplayName("getUsers 메서드")
    class GetUsers {

        @Test
        @DisplayName("사용자 목록 페이지네이션 조회 성공")
        void getUsers_success() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            User user = createMockUser(1L, "홍길동", "test@example.com");
            Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);

            given(userRepository.findAll(pageable)).willReturn(userPage);
            given(checkInRepository.findLatestByUserId(any())).willReturn(Optional.empty());

            // Act
            Page<UserDetailResponse> result = adminUserService.getUsers(pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).name()).isEqualTo("홍길동");
            assertThat(result.getContent().get(0).lastCheckInAt()).isNull();
        }
    }

    @Nested
    @DisplayName("updateUserStatus 메서드")
    class UpdateUserStatus {

        @Test
        @DisplayName("사용자 상태 변경 성공")
        void updateUserStatus_success() {
            // Arrange
            User user = mock(User.class);
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            // Act
            adminUserService.updateUserStatus(1L, "SUSPENDED");

            // Assert
            org.mockito.Mockito.verify(user).updateStatus(User.UserStatus.SUSPENDED);
        }

        @Test
        @DisplayName("존재하지 않는 사용자 상태 변경 시 ResourceNotFoundException")
        void updateUserStatus_userNotFound() {
            // Arrange
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> adminUserService.updateUserStatus(999L, "ACTIVE"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("사용자를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("유효하지 않은 상태값으로 변경 시 InvalidRequestException")
        void updateUserStatus_invalidStatus() {
            // Arrange
            User user = mock(User.class);
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            // Act & Assert
            assertThatThrownBy(() -> adminUserService.updateUserStatus(1L, "INVALID_STATUS"))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("유효하지 않은 사용자 상태");
        }
    }
}
