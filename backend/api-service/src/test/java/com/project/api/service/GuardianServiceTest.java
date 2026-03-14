package com.project.api.service;

import com.project.api.domain.Guardian;
import com.project.api.domain.User;
import com.project.api.dto.guardian.GuardianRequest;
import com.project.api.dto.guardian.GuardianResponse;
import com.project.api.exception.BusinessException;
import com.project.api.repository.GuardianRepository;
import com.project.api.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("GuardianService 테스트")
class GuardianServiceTest {

    @InjectMocks
    private GuardianService guardianService;

    @Mock
    private GuardianRepository guardianRepository;

    @Mock
    private UserRepository userRepository;

    private User createTestUser() {
        return User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .name("홍길동")
                .phone("01012345678")
                .role(User.Role.ROLE_USER)
                .build();
    }

    @Nested
    @DisplayName("addGuardian 메서드")
    class AddGuardian {

        @Test
        @DisplayName("보호자 등록 성공")
        void addGuardian_success() {
            // Arrange
            Long userId = 1L;
            User user = createTestUser();
            GuardianRequest request = new GuardianRequest("김보호", "01098765432", "부모");

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(guardianRepository.countByUserId(userId)).willReturn(2L);
            given(guardianRepository.save(any(Guardian.class))).willAnswer(inv -> inv.getArgument(0));

            // Act
            GuardianResponse response = guardianService.addGuardian(userId, request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.name()).isEqualTo("김보호");
            assertThat(response.phone()).isEqualTo("01098765432");
            assertThat(response.relationship()).isEqualTo("부모");
            verify(guardianRepository).save(any(Guardian.class));
        }

        @Test
        @DisplayName("보호자 최대 5명 초과 시 BAD_REQUEST 예외")
        void addGuardian_maxLimitReached() {
            // Arrange
            Long userId = 1L;
            User user = createTestUser();
            GuardianRequest request = new GuardianRequest("김보호", "01098765432", "부모");

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(guardianRepository.countByUserId(userId)).willReturn(5L);

            // Act & Assert
            assertThatThrownBy(() -> guardianService.addGuardian(userId, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("보호자는 최대 5명까지 등록할 수 있습니다")
                    .extracting("status")
                    .isEqualTo(HttpStatus.BAD_REQUEST);

            verify(guardianRepository, never()).save(any());
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 보호자 등록 시 NOT_FOUND 예외")
        void addGuardian_userNotFound() {
            // Arrange
            Long userId = 999L;
            GuardianRequest request = new GuardianRequest("김보호", "01098765432", "부모");
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> guardianService.addGuardian(userId, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("사용자를 찾을 수 없습니다")
                    .extracting("status")
                    .isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("getGuardians 메서드")
    class GetGuardians {

        @Test
        @DisplayName("보호자 목록 조회 성공")
        void getGuardians_success() {
            // Arrange
            Long userId = 1L;
            User user = createTestUser();
            List<Guardian> guardians = List.of(
                    Guardian.builder().user(user).name("김보호1").phone("01011111111").relationship("부모").build(),
                    Guardian.builder().user(user).name("김보호2").phone("01022222222").relationship("형제").build()
            );
            given(guardianRepository.findByUserId(userId)).willReturn(guardians);

            // Act
            List<GuardianResponse> result = guardianService.getGuardians(userId);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result.get(0).name()).isEqualTo("김보호1");
            assertThat(result.get(1).name()).isEqualTo("김보호2");
        }

        @Test
        @DisplayName("보호자가 없을 때 빈 리스트 반환")
        void getGuardians_empty() {
            // Arrange
            given(guardianRepository.findByUserId(1L)).willReturn(List.of());

            // Act
            List<GuardianResponse> result = guardianService.getGuardians(1L);

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("removeGuardian 메서드")
    class RemoveGuardian {

        @Test
        @DisplayName("보호자 삭제 성공")
        void removeGuardian_success() {
            // Arrange
            Long userId = 1L;
            Long guardianId = 1L;
            User user = createTestUser();
            Guardian guardian = Guardian.builder()
                    .user(user).name("김보호").phone("01098765432").relationship("부모")
                    .build();

            given(guardianRepository.findById(guardianId)).willReturn(Optional.of(guardian));

            // Act
            guardianService.removeGuardian(userId, guardianId);

            // Assert
            verify(guardianRepository).delete(guardian);
        }

        @Test
        @DisplayName("존재하지 않는 보호자 삭제 시 NOT_FOUND 예외")
        void removeGuardian_notFound() {
            // Arrange
            given(guardianRepository.findById(999L)).willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> guardianService.removeGuardian(1L, 999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("보호자를 찾을 수 없습니다")
                    .extracting("status")
                    .isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("타인의 보호자 삭제 시도 시 UNAUTHORIZED 예외")
        void removeGuardian_notOwner() {
            // Arrange
            Long ownerId = 1L;
            Long requesterId = 2L;
            User owner = createTestUser();
            Guardian guardian = Guardian.builder()
                    .user(owner).name("김보호").phone("01098765432").relationship("부모")
                    .build();

            given(guardianRepository.findById(1L)).willReturn(Optional.of(guardian));

            // Act & Assert
            assertThatThrownBy(() -> guardianService.removeGuardian(requesterId, 1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("본인의 보호자만 삭제할 수 있습니다");

            verify(guardianRepository, never()).delete(any());
        }
    }
}
