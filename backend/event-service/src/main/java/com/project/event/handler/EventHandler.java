package com.project.event.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.event.domain.EventLog;
import com.project.event.repository.EventLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Kafka에서 수신한 모든 이벤트를 MongoDB에 로깅합니다.
 * 이벤트 수신 즉시 메타데이터를 파싱하여 단일 객체를 구성하고,
 * 처리 결과에 따라 PROCESSED / FAILED 상태로 전이합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventHandler {

    private static final String STATUS_PROCESSING = "PROCESSING";
    private static final String STATUS_PROCESSED = "PROCESSED";
    private static final String STATUS_FAILED = "FAILED";

    private final EventLogRepository eventLogRepository;
    private final ObjectMapper objectMapper;

    /**
     * 수신된 이벤트 JSON을 파싱하여 MongoDB에 저장합니다.
     * 단일 파싱 단계에서 메타데이터를 추출하여 불필요한 재파싱을 제거합니다.
     *
     * @param eventJson Kafka에서 수신한 이벤트 JSON 문자열
     */
    public void handleEvent(String eventJson) {
        EventLog eventLog = parseToEventLog(eventJson);

        try {
            eventLog.setStatus(STATUS_PROCESSING);
            eventLogRepository.save(eventLog);

            eventLog.setStatus(STATUS_PROCESSED);
            eventLog.setProcessedAt(LocalDateTime.now());
            eventLogRepository.save(eventLog);

            log.info("Event processed: eventId={}, type={}", eventLog.getEventId(), eventLog.getEventType());

        } catch (Exception e) {
            log.error("Event processing failed: {}", e.getMessage(), e);
            eventLog.setStatus(STATUS_FAILED);
            eventLog.setErrorMessage(e.getMessage());
            eventLog.setProcessedAt(LocalDateTime.now());
            eventLogRepository.save(eventLog);
        }
    }

    /**
     * JSON 문자열을 파싱하여 완성된 {@link EventLog} 인스턴스를 생성합니다.
     * 파싱 실패 시 페이로드만 포함한 기본 로그를 반환합니다.
     */
    private EventLog parseToEventLog(String eventJson) {
        EventLog.EventLogBuilder builder = EventLog.builder()
                .eventId(UUID.randomUUID().toString())
                .status("RECEIVED")
                .payload(eventJson)
                .occurredAt(LocalDateTime.now());

        try {
            JsonNode root = objectMapper.readTree(eventJson);
            extractText(root, "eventId").ifPresent(builder::eventId);
            extractText(root, "eventType").ifPresent(builder::eventType);
            extractText(root, "source").ifPresent(builder::source);
            extractText(root, "aggregateId").ifPresent(builder::aggregateId);
        } catch (Exception e) {
            log.warn("Could not parse event metadata from payload: {}", e.getMessage());
        }

        return builder.build();
    }

    private Optional<String> extractText(JsonNode root, String field) {
        return root.has(field)
                ? Optional.of(root.get(field).asText())
                : Optional.empty();
    }
}
