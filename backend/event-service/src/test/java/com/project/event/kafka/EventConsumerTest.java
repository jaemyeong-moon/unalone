package com.project.event.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.event.handler.AlertHandler;
import com.project.event.handler.EventHandler;
import com.project.event.kafka.consumer.EventConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventConsumer 테스트")
class EventConsumerTest {

    @InjectMocks
    private EventConsumer eventConsumer;

    @Mock
    private EventHandler eventHandler;

    @Mock
    private AlertHandler alertHandler;

    @Spy
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Nested
    @DisplayName("consume 메서드")
    class Consume {

        @Test
        @DisplayName("CHECKIN_COMPLETED 이벤트 처리 성공")
        void consume_checkInCompleted() {
            // Arrange
            String message = """
                    {
                        "eventType": "CHECKIN_COMPLETED",
                        "userId": 1,
                        "message": "체크인 완료"
                    }
                    """;

            // Act
            eventConsumer.consume(message);

            // Assert
            verify(eventHandler).handleEvent(message);
            // CHECKIN_COMPLETED는 로그만 남기고 추가 처리 없음
            verify(alertHandler, never()).handleCheckInMissed(anyLong(), anyInt());
        }

        @Test
        @DisplayName("CHECKIN_MISSED 이벤트 처리 - Alert 생성 트리거")
        void consume_checkInMissed_triggersAlert() {
            // Arrange
            String message = """
                    {
                        "eventType": "CHECKIN_MISSED",
                        "userId": 1,
                        "missedCount": 2
                    }
                    """;

            // Act
            eventConsumer.consume(message);

            // Assert
            verify(eventHandler).handleEvent(message);
            verify(alertHandler).handleCheckInMissed(1L, 2);
        }

        @Test
        @DisplayName("CHECKIN_MISSED 이벤트 - missedCount 기본값 1")
        void consume_checkInMissed_defaultMissedCount() {
            // Arrange
            String message = """
                    {
                        "eventType": "CHECKIN_MISSED",
                        "userId": 5
                    }
                    """;

            // Act
            eventConsumer.consume(message);

            // Assert
            verify(alertHandler).handleCheckInMissed(5L, 1);
        }

        @Test
        @DisplayName("ALERT_RESOLVED 이벤트 처리 성공")
        void consume_alertResolved() {
            // Arrange
            String message = """
                    {
                        "eventType": "ALERT_RESOLVED",
                        "alertId": "alert-123",
                        "resolvedBy": 1
                    }
                    """;

            // Act
            eventConsumer.consume(message);

            // Assert
            verify(eventHandler).handleEvent(message);
            verify(alertHandler).handleAlertResolved("alert-123", 1L);
        }

        @Test
        @DisplayName("알 수 없는 이벤트 타입은 로깅만 수행")
        void consume_unknownEventType_logsOnly() {
            // Arrange
            String message = """
                    {
                        "eventType": "UNKNOWN_EVENT",
                        "data": "some-data"
                    }
                    """;

            // Act
            eventConsumer.consume(message);

            // Assert
            verify(eventHandler).handleEvent(message);
            verify(alertHandler, never()).handleCheckInMissed(anyLong(), anyInt());
            verify(alertHandler, never()).handleAlertResolved(anyString(), anyLong());
        }

        @Test
        @DisplayName("잘못된 JSON 메시지 처리 시 예외를 삼키고 계속 동작")
        void consume_invalidJson_doesNotThrow() {
            // Arrange
            String invalidMessage = "this-is-not-json";

            // Act - 예외가 발생하지 않아야 함
            eventConsumer.consume(invalidMessage);

            // Assert - eventHandler.handleEvent가 호출되지 않음 (파싱 실패)
            verify(eventHandler, never()).handleEvent(anyString());
        }
    }
}
