package com.project.api.service;

import com.project.api.domain.Notification;
import com.project.api.domain.enums.NotificationType;
import com.project.api.dto.notification.NotificationResponse;
import com.project.api.exception.BusinessException;
import com.project.api.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SseEmitterService sseEmitterService;

    /**
     * 알림을 생성하고 SSE로 실시간 전송합니다.
     */
    @Transactional
    public NotificationResponse createNotification(Long userId, NotificationType type, String title,
                                                    String message, Long relatedId, String relatedType) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .relatedId(relatedId)
                .relatedType(relatedType)
                .build();

        notificationRepository.save(notification);
        log.info("알림 생성: userId={}, type={}, title={}", userId, type, title);

        NotificationResponse response = NotificationResponse.from(notification);

        // SSE로 실시간 전송
        sseEmitterService.send(userId, response);

        return response;
    }

    /**
     * 사용자의 알림 목록을 페이지네이션으로 조회합니다.
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(Long userId, int page, int size) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
                .map(NotificationResponse::from);
    }

    /**
     * 읽지 않은 알림 수를 반환합니다.
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    /**
     * 특정 알림을 읽음 처리합니다.
     */
    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> BusinessException.notFound("알림을 찾을 수 없습니다"));

        if (!notification.getUserId().equals(userId)) {
            throw BusinessException.unauthorized("본인의 알림만 읽음 처리할 수 있습니다");
        }

        notification.markAsRead();
        notificationRepository.save(notification);
    }

    /**
     * 사용자의 모든 알림을 읽음 처리합니다.
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }
}
