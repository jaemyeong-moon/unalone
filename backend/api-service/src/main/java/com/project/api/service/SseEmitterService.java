package com.project.api.service;

import com.project.api.dto.notification.NotificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SseEmitterService {

    private static final long SSE_TIMEOUT = 30 * 60 * 1000L; // 30분

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * 사용자의 SSE 구독을 생성합니다.
     */
    public SseEmitter subscribe(Long userId) {
        // 기존 연결이 있으면 제거
        remove(userId);

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        emitter.onCompletion(() -> {
            log.debug("SSE 연결 완료: userId={}", userId);
            emitters.remove(userId);
        });

        emitter.onTimeout(() -> {
            log.debug("SSE 연결 타임아웃: userId={}", userId);
            emitter.complete();
            emitters.remove(userId);
        });

        emitter.onError(e -> {
            log.debug("SSE 연결 에러: userId={}, error={}", userId, e.getMessage());
            emitters.remove(userId);
        });

        emitters.put(userId, emitter);

        // 연결 확인용 초기 이벤트 전송
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("connected"));
        } catch (IOException e) {
            log.error("SSE 초기 이벤트 전송 실패: userId={}", userId);
            emitters.remove(userId);
        }

        return emitter;
    }

    /**
     * 특정 사용자에게 알림을 전송합니다.
     */
    public void send(Long userId, NotificationResponse notification) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter == null) {
            return;
        }

        try {
            emitter.send(SseEmitter.event()
                    .name("notification")
                    .data(notification));
        } catch (IOException e) {
            log.error("SSE 알림 전송 실패: userId={}, error={}", userId, e.getMessage());
            emitters.remove(userId);
        }
    }

    /**
     * 사용자의 SSE 연결을 제거합니다.
     */
    public void remove(Long userId) {
        SseEmitter emitter = emitters.remove(userId);
        if (emitter != null) {
            try {
                emitter.complete();
            } catch (Exception e) {
                log.debug("SSE emitter 종료 중 에러: userId={}", userId);
            }
        }
    }
}
