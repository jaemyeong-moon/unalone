package com.project.event.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.event.domain.EventLog;
import com.project.event.domain.EventLogStatus;
import com.project.event.repository.EventLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventHandler {

    private final EventLogRepository eventLogRepository;
    private final ObjectMapper objectMapper;

    public void handleEvent(String eventJson) {
        EventLog eventLog = parseToEventLog(eventJson);

        try {
            eventLog.markProcessing();
            eventLogRepository.save(eventLog);

            eventLog.markProcessed();
            eventLogRepository.save(eventLog);

            log.info("Event processed: eventId={}, type={}", eventLog.getEventId(), eventLog.getEventType());

        } catch (Exception e) {
            log.error("Event processing failed: {}", e.getMessage(), e);
            eventLog.markFailed(e.getMessage());
            eventLogRepository.save(eventLog);
        }
    }

    private EventLog parseToEventLog(String eventJson) {
        EventLog.EventLogBuilder builder = EventLog.builder()
                .eventId(UUID.randomUUID().toString())
                .status(EventLogStatus.RECEIVED)
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
