package com.project.event.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 안부 체크 미응답 알림 MongoDB 도큐먼트.
 * level과 status는 각각 {@link AlertLevel}, {@link AlertStatus}로 관리됩니다.
 */
@Document(collection = "alerts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Alert {

    @Id
    private String id;

    private Long userId;

    /** 알림 심각도 (WARNING / DANGER / CRITICAL) */
    private AlertLevel level;

    private String message;

    /** 알림 처리 상태 (ACTIVE / RESOLVED) */
    private AlertStatus status;

    private Long resolvedBy;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    /**
     * 알림을 해결 처리한 새 인스턴스를 반환합니다.
     * 불변성을 유지하기 위해 기존 객체를 직접 변경하지 않습니다.
     *
     * @param resolvedBy 처리한 관리자 ID
     * @return 해결 상태가 적용된 새 Alert 인스턴스
     */
    public Alert resolve(Long resolvedBy) {
        return Alert.builder()
                .id(this.id)
                .userId(this.userId)
                .level(this.level)
                .message(this.message)
                .status(AlertStatus.RESOLVED)
                .resolvedBy(resolvedBy)
                .createdAt(this.createdAt)
                .resolvedAt(LocalDateTime.now())
                .build();
    }
}
