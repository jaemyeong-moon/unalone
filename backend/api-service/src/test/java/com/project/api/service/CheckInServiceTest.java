package com.project.api.service;

import com.project.api.domain.CheckIn;
import com.project.api.domain.User;
import com.project.api.dto.checkin.CheckInRequest;
import com.project.api.dto.checkin.CheckInResponse;
import com.project.api.exception.BusinessException;
import com.project.api.kafka.producer.ApiEventProducer;
import com.project.api.repository.CheckInRepository;
import com.project.api.repository.CheckInScheduleRepository;
import com.project.api.repository.UserRepository;
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
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CheckInService 테스트")
class CheckInServiceTest {

    @InjectMocks
    private CheckInService checkInService;

    @Mock
    private CheckInRepository checkInRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApiEventProducer eventProducer;

    @Mock
    private EscalationService escalationService;

    @Mock
    private CheckInScheduleRepository checkInScheduleRepository;

    @Mock
    private CheckInScheduleService checkInScheduleService;

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
    @DisplayName("checkIn 메서드")
    class CheckInMethod {

        @Test
        @DisplayName("체크인 성공 시 CheckInResponse 반환 및 이벤트 발행")
        void checkIn_success() {
            // Arrange
            Long userId = 1L;
            User user = createTestUser();
            CheckInRequest request = new CheckInRequest("기분 좋아요", 8);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(checkInRepository.save(any(CheckIn.class))).willAnswer(inv -> inv.getArgument(0));
            given(checkInScheduleRepository.findByUserId(userId)).willReturn(Optional.empty());

            // Act
            CheckInResponse response = checkInService.checkIn(userId, request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.status()).isEqualTo("CHECKED");
            assertThat(response.message()).isEqualTo("기분 좋아요");
            assertThat(response.moodScore()).isEqualTo(8);

            verify(checkInRepository).save(any(CheckIn.class));
            verify(escalationService).resolveByCheckIn(userId);
            verify(eventProducer).publishEvent(anyString(), any());
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 체크인 시 NOT_FOUND 예외 발생")
        void checkIn_userNotFound_throwsBusinessException() {
            // Arrange
            Long userId = 999L;
            CheckInRequest request = new CheckInRequest("테스트", 5);
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> checkInService.checkIn(userId, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("사용자를 찾을 수 없습니다")
                    .extracting("status")
                    .isEqualTo(HttpStatus.NOT_FOUND);

            verify(checkInRepository, never()).save(any());
        }

        @Test
        @DisplayName("Kafka 이벤트 발행 실패해도 체크인은 성공")
        void checkIn_kafkaFails_stillSucceeds() {
            // Arrange
            Long userId = 1L;
            User user = createTestUser();
            CheckInRequest request = new CheckInRequest("테스트", 5);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(checkInRepository.save(any(CheckIn.class))).willAnswer(inv -> inv.getArgument(0));
            given(checkInScheduleRepository.findByUserId(userId)).willReturn(Optional.empty());
            doThrow(new RuntimeException("Kafka 연결 실패"))
                    .when(eventProducer).publishEvent(anyString(), any());

            // Act
            CheckInResponse response = checkInService.checkIn(userId, request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.status()).isEqualTo("CHECKED");
            verify(checkInRepository).save(any(CheckIn.class));
        }
    }

    @Nested
    @DisplayName("getMyCheckIns 메서드")
    class GetMyCheckIns {

        @Test
        @DisplayName("페이지네이션된 체크인 목록 반환")
        void getMyCheckIns_success() {
            // Arrange
            Long userId = 1L;
            User user = createTestUser();
            Pageable pageable = PageRequest.of(0, 20);

            CheckIn checkIn = CheckIn.builder()
                    .user(user)
                    .status(CheckIn.CheckInStatus.CHECKED)
                    .message("테스트")
                    .moodScore(7)
                    .build();

            Page<CheckIn> checkInPage = new PageImpl<>(List.of(checkIn), pageable, 1);
            given(checkInRepository.findByUserIdOrderByCheckedAtDesc(userId, pageable)).willReturn(checkInPage);

            // Act
            Page<CheckInResponse> result = checkInService.getMyCheckIns(userId, pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).message()).isEqualTo("테스트");
            assertThat(result.getTotalElements()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("getLatestCheckIn 메서드")
    class GetLatestCheckIn {

        @Test
        @DisplayName("최신 체크인 조회 성공")
        void getLatestCheckIn_success() {
            // Arrange
            Long userId = 1L;
            User user = createTestUser();
            CheckIn checkIn = CheckIn.builder()
                    .user(user)
                    .status(CheckIn.CheckInStatus.CHECKED)
                    .message("최신 체크인")
                    .moodScore(9)
                    .build();

            given(checkInRepository.findLatestByUserId(userId)).willReturn(Optional.of(checkIn));

            // Act
            CheckInResponse response = checkInService.getLatestCheckIn(userId);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.message()).isEqualTo("최신 체크인");
            assertThat(response.moodScore()).isEqualTo(9);
        }

        @Test
        @DisplayName("체크인 기록이 없을 때 NOT_FOUND 예외 발생")
        void getLatestCheckIn_notFound() {
            // Arrange
            Long userId = 1L;
            given(checkInRepository.findLatestByUserId(userId)).willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> checkInService.getLatestCheckIn(userId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("체크인 기록이 없습니다")
                    .extracting("status")
                    .isEqualTo(HttpStatus.NOT_FOUND);
        }
    }
}
