package com.project.common.event;

/**
 * 이벤트 직렬화/역직렬화 실패 시 발생하는 예외.
 * {@link EventPublisher}에서 JSON 처리 실패 시 사용됩니다.
 */
public class EventSerializationException extends RuntimeException {

    public EventSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
