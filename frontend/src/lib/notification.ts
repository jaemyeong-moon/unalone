import apiClient from '@/lib/api';
import { getToken } from '@/lib/auth';
import { ApiResponse, PageResponse, NotificationResponse } from '@/types';

/** 알림 목록 조회 (페이징) */
export async function getNotifications(page: number = 0, size: number = 10) {
  const res = await apiClient.get<ApiResponse<PageResponse<NotificationResponse>>>(
    `/api/notifications?page=${page}&size=${size}`
  );
  return res.data.data;
}

/** 읽지 않은 알림 수 조회 */
export async function getUnreadCount(): Promise<number> {
  const res = await apiClient.get<ApiResponse<number>>('/api/notifications/unread-count');
  return res.data.data;
}

/** 단건 알림 읽음 처리 */
export async function markAsRead(id: number): Promise<void> {
  await apiClient.patch(`/api/notifications/${id}/read`);
}

/** 전체 알림 읽음 처리 */
export async function markAllAsRead(): Promise<void> {
  await apiClient.patch('/api/notifications/read-all');
}

/** 타입별 알림 목록 조회 */
export async function getNotificationsByType(
  type: string,
  page: number = 0,
  size: number = 10
) {
  const params = new URLSearchParams({ page: String(page), size: String(size) });
  if (type) params.append('type', type);
  const res = await apiClient.get<ApiResponse<PageResponse<NotificationResponse>>>(
    `/api/notifications?${params}`
  );
  return res.data.data;
}

/** SSE 실시간 알림 구독 */
export function subscribeSSE(
  onMessage: (notification: NotificationResponse) => void,
  onError?: () => void
): EventSource | null {
  if (typeof window === 'undefined') return null;

  const token = getToken();
  if (!token) return null;

  const url = `/api/notifications/stream?token=${encodeURIComponent(token)}`;
  const eventSource = new EventSource(url);

  eventSource.onmessage = (event) => {
    try {
      const notification = JSON.parse(event.data) as NotificationResponse;
      onMessage(notification);
    } catch {
      // heartbeat 또는 파싱 불가 메시지 무시
    }
  };

  eventSource.onerror = () => {
    if (onError) onError();
    // 자동 재연결은 EventSource 기본 동작
  };

  return eventSource;
}
