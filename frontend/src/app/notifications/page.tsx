'use client';

import { useEffect, useState, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import PageLayout from '@/components/common/PageLayout';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import Pagination from '@/components/common/Pagination';
import NotificationItem from '@/components/common/NotificationItem';
import { isLoggedIn } from '@/lib/auth';
import { getNotificationsByType, markAllAsRead } from '@/lib/notification';
import { NotificationResponse } from '@/types';

const FILTER_TABS = [
  { key: '', label: '전체' },
  { key: 'CHECKIN_REMINDER', label: '체크인' },
  { key: 'HEALTH_ALERT', label: '건강' },
  { key: 'CARE_MATCH,CARE_VISIT', label: '돌봄' },
  { key: 'COMMUNITY_REPLY', label: '커뮤니티' },
] as const;

export default function NotificationsPage() {
  const router = useRouter();
  const [notifications, setNotifications] = useState<NotificationResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [filter, setFilter] = useState('');

  const fetchNotifications = useCallback(async () => {
    setLoading(true);
    try {
      const data = await getNotificationsByType(filter, page, 15);
      setNotifications(data.content);
      setTotalPages(data.totalPages);
    } catch {
      console.error('Failed to fetch notifications');
    } finally {
      setLoading(false);
    }
  }, [filter, page]);

  useEffect(() => {
    if (!isLoggedIn()) {
      router.push('/login');
      return;
    }
    fetchNotifications();
  }, [fetchNotifications, router]);

  const handleMarkAllRead = async () => {
    try {
      await markAllAsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
    } catch {
      alert('읽음 처리에 실패했습니다');
    }
  };

  const handleItemRead = (id: number) => {
    setNotifications((prev) =>
      prev.map((n) => (n.id === id ? { ...n, read: true } : n))
    );
  };

  const handleFilterChange = (key: string) => {
    setFilter(key);
    setPage(0);
  };

  const hasUnread = notifications.some((n) => !n.read);

  return (
    <PageLayout maxWidth="max-w-3xl">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">알림</h1>
          <p className="text-sm text-gray-500 mt-1">나의 알림 내역을 확인하세요</p>
        </div>
        {hasUnread && (
          <button
            onClick={handleMarkAllRead}
            className="px-4 py-2 text-sm font-medium text-emerald-600 border border-emerald-300 rounded-lg hover:bg-emerald-50 transition-colors"
          >
            모두 읽음 처리
          </button>
        )}
      </div>

      {/* Filter Tabs */}
      <div className="flex gap-2 mb-4 overflow-x-auto pb-1">
        {FILTER_TABS.map((tab) => (
          <button
            key={tab.key}
            onClick={() => handleFilterChange(tab.key)}
            className={`px-3 py-1.5 rounded-full text-sm font-medium whitespace-nowrap transition-colors ${
              filter === tab.key
                ? 'bg-emerald-600 text-white'
                : 'bg-white text-gray-600 border border-gray-300 hover:bg-gray-50'
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Notification List */}
      {loading ? (
        <LoadingSpinner />
      ) : notifications.length === 0 ? (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-12 text-center">
          <svg xmlns="http://www.w3.org/2000/svg" className="h-12 w-12 mx-auto text-gray-300 mb-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
          </svg>
          <p className="text-gray-500">알림이 없습니다</p>
          <p className="text-sm text-gray-400 mt-1">새로운 알림이 오면 여기에 표시됩니다</p>
        </div>
      ) : (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden divide-y divide-gray-50">
          {notifications.map((notification) => (
            <NotificationItem
              key={notification.id}
              notification={notification}
              onRead={handleItemRead}
            />
          ))}
        </div>
      )}

      <Pagination
        page={page}
        totalPages={totalPages}
        onPrev={() => setPage(Math.max(0, page - 1))}
        onNext={() => setPage(Math.min(totalPages - 1, page + 1))}
      />
    </PageLayout>
  );
}
