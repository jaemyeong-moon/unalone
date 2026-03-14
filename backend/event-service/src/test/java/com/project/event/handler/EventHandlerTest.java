package com.project.event.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.event.domain.EventLog;
import com.project.event.domain.EventLogStatus;
import com.project.event.repository.EventLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventHandler 테스트")
class EventHandlerTest {

    @InjectMocks
    private EventHandler eventHandler;

    @Mock
    private EventLogRepository eventLogRepository;

    @Spy
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Nested
    @DisplayName("handleEvent 메서드")
    class HandleEvent {

        @Test
        @DisplayName("유효한 이벤트 JSON 처리 시 PROCESSED 상태로 저장")
        void handleEvent_validJson_processed() {
            // Arrange
            String eventJson = """
                    {
                        "eventId": "evt-123",
                        "eventType": "CHECKIN_COMPLETED",
                        "source": "api-service",
                        "aggregateId": "1",
                        "userId": 1,
                        "message": "체크인 완료"
                    }
                    """;
            given(eventLogRepository.save(any(EventLog.class))).willAnswer(inv -> inv.getArgument(0));

            // Act
            eventHandler.handleEvent(eventJson);

            // Assert
            ArgumentCaptor<EventLog> captor = ArgumentCaptor.forClass(EventLog.class);
            verify(eventLogRepository, times(2)).save(captor.capture());

            // 마지막 저장된 이벤트가 PROCESSED 상태여야 함
            EventLog lastSaved = captor.getAllValues().get(1);
            assertThat(lastSaved.getStatus()).isEqualTo(EventLogStatus.PROCESSED);
            assertThat(lastSaved.getEventType()).isEqualTo("CHECKIN_COMPLETED");
            assertThat(lastSaved.getSource()).isEqualTo("api-service");
        }

        @Test
        @DisplayName("이벤트 메타데이터 없는 JSON도 처리 가능")
        void handleEvent_minimalJson() {
            // Arrange
            String eventJson = """
                    {"data": "some-data"}
                    """;
            given(eventLogRepository.save(any(EventLog.class))).willAnswer(inv -> inv.getArgument(0));

            // Act
            eventHandler.handleEvent(eventJson);

            // Assert
            verify(eventLogRepository, times(2)).save(any(EventLog.class));
        }

        @Test
        @DisplayName("저장소 오류 시 FAILED 상태로 저장")
        void handleEvent_repositoryError_failed() {
            // Arrange
            String eventJson = """
                    {"eventType": "CHECKIN_COMPLETED", "userId": 1}
                    """;
            given(eventLogRepository.save(any(EventLog.class)))
                    .willThrow(new RuntimeException("DB 연결 오류"))
                    .willAnswer(inv -> inv.getArgument(0));

            // Act
            eventHandler.handleEvent(eventJson);

            // Assert
            ArgumentCaptor<EventLog> captor = ArgumentCaptor.forClass(EventLog.class);
            verify(eventLogRepository, times(2)).save(captor.capture());

            EventLog failedLog = captor.getAllValues().get(1);
            assertThat(failedLog.getStatus()).isEqualTo(EventLogStatus.FAILED);
            assertThat(failedLog.getErrorMessage()).isEqualTo("DB 연결 오류");
        }
    }
}
