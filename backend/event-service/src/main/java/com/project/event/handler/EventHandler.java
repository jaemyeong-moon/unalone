package com.project.event.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.event.domain.EventLog;
import com.project.event.repository.EventLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventHandler {

    private final EventLogRepository eventLogRepository;
    private final ObjectMapper objectMapper;

    public void handleEvent(String eventJson) {
        EventLog eventLog = EventLog.builder()
                .eventId(UUID.randomUUID().toString())
                .status("RECEIVED")
                .payload(eventJson)
                .occurredAt(LocalDateTime.now())
                .build();

        try {
            JsonNode jsonNode = objectMapper.readTree(eventJson);

            if (jsonNode.has("eventType")) {
                eventLog.setEventType(jsonNode.get("eventType").asText());
            }
            if (jsonNode.has("source")) {
                eventLog.setSource(jsonNode.get("source").asText());
            }
            if (jsonNode.has("aggregateId")) {
                eventLog.setAggregateId(jsonNode.get("aggregateId").asText());
            }
            if (jsonNode.has("eventId")) {
                eventLog.setEventId(jsonNode.get("eventId").asText());
            }

            eventLog.setStatus("PROCESSING");
            eventLogRepository.save(eventLog);

            processEvent(eventLog);

            eventLog.setStatus("PROCESSED");
            eventLog.setProcessedAt(LocalDateTime.now());
            eventLogRepository.save(eventLog);

            log.info("Event processed successfully: eventId={}, type={}", eventLog.getEventId(), eventLog.getEventType());

        } catch (Exception e) {
            log.error("Failed to process event: {}", e.getMessage(), e);
            eventLog.setStatus("FAILED");
            eventLog.setErrorMessage(e.getMessage());
            eventLog.setProcessedAt(LocalDateTime.now());
            eventLogRepository.save(eventLog);
        }
    }

    private void processEvent(EventLog eventLog) {
        log.debug("Processing event: eventId={}, type={}, source={}",
                eventLog.getEventId(), eventLog.getEventType(), eventLog.getSource());
    }
}
