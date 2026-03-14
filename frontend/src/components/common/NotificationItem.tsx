'use client';

import { useRouter } from 'next/navigation';
import { NotificationResponse, NotificationType } from '@/types';
import { formatTimeAgo } from '@/lib/utils';
import { markAsRead } from '@/lib/notification';

interface NotificationItemProps {
  notification: NotificationResponse;
  onRead?: (id: number) => void;
}

/** 알림 타입별 아이콘 */
function NotificationIcon({ type }: { type: NotificationType }) {
  const iconClass = 'h-5 w-5 flex-shrink-0';

  switch (type) {
    case 'CHECKIN_REMINDER':
      return (
        <svg xmlns="http://www.w3.org/2000/svg" className={`${iconClass} text-emerald-500`} fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
        </svg>
      );
    case 'ESCALATION':
      return (
        <svg xmlns="http://www.w3.org/2000/svg" className={`${iconClass} text-amber-500`} fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L4.082 16.5c-.77.833.192 2.5 1.732 2.5z" />
        </svg>
      );
    case 'HEALTH_ALERT':
      return (
        <svg xmlns="http://www.w3.org/2000/svg" className={`${iconClass} text-red-500`} fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
        </svg>
      );
    case 'CARE_MATCH':
    case 'CARE_VISIT':
      return (
        <svg xmlns="http://www.w3.org/2000/svg" className={`${iconClass} text-blue-500`} fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z" />
        </svg>
      );
    case 'COMMUNITY_REPLY':
      return (
        <svg xmlns="http://www.w3.org/2000/svg" className={`${iconClass} text-purple-500`} fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
        </svg>
      );
    case 'SYSTEM':
    default:
      return (
        <svg xmlns="http://www.w3.org/2000/svg" className={`${iconClass} text-gray-500`} fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      );
  }
}

/** 알림 타입에 따라 이동할 경로 반환 */
function getNotificationLink(notification: NotificationResponse): string | null {
  const { relatedType, relatedId } = notification;
  if (!relatedId) return null;

  switch (relatedType) {
    case 'CHECKIN':
      return '/checkin';
    case 'HEALTH':
      return '/health';
    case 'CARE_MATCH':
      return '/care';
    case 'CARE_VISIT':
      return '/care';
    case 'COMMUNITY_POST':
      return `/community/${relatedId}`;
    default:
      return null;
  }
}

export default function NotificationItem({ notification, onRead }: NotificationItemProps) {
  const router = useRouter();

  const handleClick = async () => {
    if (!notification.read) {
      try {
        await markAsRead(notification.id);
        onRead?.(notification.id);
      } catch {
        // 읽음 처리 실패 시 무시하고 네비게이션
      }
    }

    const link = getNotificationLink(notification);
    if (link) {
      router.push(link);
    }
  };

  return (
    <button
      onClick={handleClick}
      className={`w-full text-left flex items-start gap-3 px-4 py-3 transition-colors hover:bg-gray-50 ${
        !notification.read ? 'bg-emerald-50/50 border-l-2 border-emerald-500' : 'border-l-2 border-transparent'
      }`}
    >
      <div className="mt-0.5">
        <NotificationIcon type={notification.type} />
      </div>
      <div className="flex-1 min-w-0">
        <p className={`text-sm ${!notification.read ? 'font-semibold text-gray-900' : 'text-gray-700'}`}>
          {notification.title}
        </p>
        <p className="text-xs text-gray-500 mt-0.5 line-clamp-2">{notification.message}</p>
        <p className="text-xs text-gray-400 mt-1">{formatTimeAgo(notification.createdAt)}</p>
      </div>
      {!notification.read && (
        <div className="mt-2 h-2 w-2 rounded-full bg-emerald-500 flex-shrink-0" />
      )}
    </button>
  );
}
