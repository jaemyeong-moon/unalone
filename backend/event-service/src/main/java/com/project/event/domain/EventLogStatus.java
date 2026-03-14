package com.project.event.domain;

/**
 * 이벤트 로그 처리 상태.
 */
public enum EventLogStatus {
    RECEIVED,
    PROCESSING,
    PROCESSED,
    FAILED
}
