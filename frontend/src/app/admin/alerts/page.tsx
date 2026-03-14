'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import PageLayout from '@/components/common/PageLayout';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import Pagination from '@/components/common/Pagination';
import { adminClient } from '@/lib/api';
import { isLoggedIn, getUser } from '@/lib/auth';
import { formatShortDateTime } from '@/lib/utils';
import { ApiResponse, PageResponse, AlertResponse, AlertLevel } from '@/types';

interface StatusFilter {
  value: string;
  label: string;
}

const STATUS_FILTERS: StatusFilter[] = [
  { value: '', label: '전체' },
  { value: 'ACTIVE', label: '활성' },
  { value: 'RESOLVED', label: '해결됨' },
];

interface LevelBadge {
  bg: string;
  text: string;
  label: string;
}

const LEVEL_BADGES: Record<AlertLevel, LevelBadge> = {
  WARNING: { bg: 'bg-yellow-50 border-yellow-200', text: 'text-yellow-700', label: '주의' },
  DANGER: { bg: 'bg-orange-50 border-orange-200', text: 'text-orange-700', label: '위험' },
  CRITICAL: { bg: 'bg-red-50 border-red-200', text: 'text-red-700', label: '긴급' },
};

const DEFAULT_BADGE: LevelBadge = {
  bg: 'bg-gray-50 border-gray-200',
  text: 'text-gray-700',
  label: '',
};

function getLevelBadge(level: AlertLevel): LevelBadge {
  return LEVEL_BADGES[level] ?? DEFAULT_BADGE;
}

export default function AdminAlertsPage() {
  const router = useRouter();
  const [alerts, setAlerts] = useState<AlertResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [statusFilter, setStatusFilter] = useState('');

  useEffect(() => {
    const user = getUser();
    if (!isLoggedIn() || user?.role !== 'ROLE_ADMIN') {
      router.push('/');
      return;
    }
    fetchAlerts();
  }, [page, statusFilter]);

  const fetchAlerts = async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams({ page: String(page), size: '20' });
      if (statusFilter) params.append('status', statusFilter);
      const res = await adminClient.get<ApiResponse<PageResponse<AlertResponse>>>(
        `/api/admin/alerts?${params}`
      );
      setAlerts(res.data.data.content);
      setTotalPages(res.data.data.totalPages);
    } catch {
      console.error('Failed to fetch alerts');
    } finally {
      setLoading(false);
    }
  };

  const resolveAlert = async (alertId: string) => {
    if (!confirm('이 알림을 해결 처리하시겠습니까?')) return;
    try {
      await adminClient.patch(`/api/admin/alerts/${alertId}/resolve`);
      fetchAlerts();
    } catch {
      alert('알림 해결에 실패했습니다');
    }
  };

  const handleFilterChange = (value: string) => {
    setStatusFilter(value);
    setPage(0);
  };

  return (
    <PageLayout maxWidth="max-w-7xl">
      <div className="flex items-center gap-3 mb-6">
        <Link href="/admin" className="text-gray-400 hover:text-gray-600">
          <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
        </Link>
        <div>
          <h1 className="text-2xl font-bold text-gray-900">알림 관리</h1>
          <p className="text-sm text-gray-500 mt-1">이상 감지 알림을 확인하고 처리하세요</p>
        </div>
      </div>

      {/* 필터 */}
      <div className="flex gap-2 mb-4">
        {STATUS_FILTERS.map((filter) => (
          <button
            key={filter.value}
            onClick={() => handleFilterChange(filter.value)}
            className={`px-3 py-1.5 rounded-full text-sm font-medium transition-colors ${
              statusFilter === filter.value
                ? 'bg-emerald-600 text-white'
                : 'bg-white text-gray-600 border border-gray-300 hover:bg-gray-50'
            }`}
          >
            {filter.label}
          </button>
        ))}
      </div>

      {loading ? (
        <LoadingSpinner />
      ) : alerts.length === 0 ? (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-12 text-center">
          <p className="text-gray-500">알림이 없습니다</p>
        </div>
      ) : (
        <div className="space-y-3">
          {alerts.map((alert) => {
            const badge = getLevelBadge(alert.level);
            const isResolved = alert.status === 'RESOLVED';
            return (
              <div
                key={alert.id}
                className={`bg-white rounded-xl shadow-sm border p-5 ${
                  isResolved ? 'border-gray-200 opacity-75' : badge.bg
                }`}
              >
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-2">
                      <span className={`px-2 py-0.5 rounded text-xs font-bold ${badge.text} ${badge.bg}`}>
                        {badge.label || alert.level}
                      </span>
                      <span className={`px-2 py-0.5 rounded text-xs font-medium ${
                        isResolved ? 'bg-gray-100 text-gray-500' : 'bg-red-100 text-red-700'
                      }`}>
                        {isResolved ? '해결됨' : '활성'}
                      </span>
                    </div>
                    <p className="text-sm font-medium text-gray-900 mb-1">{alert.message}</p>
                    <div className="flex items-center gap-3 text-xs text-gray-500">
                      <span>사용자: {alert.userName} (ID: {alert.userId})</span>
                      <span>|</span>
                      <span>발생: {formatShortDateTime(alert.createdAt)}</span>
                      {alert.resolvedAt && (
                        <>
                          <span>|</span>
                          <span>해결: {formatShortDateTime(alert.resolvedAt)}</span>
                        </>
                      )}
                    </div>
                  </div>
                  {!isResolved && (
                    <button
                      onClick={() => resolveAlert(alert.id)}
                      className="ml-4 px-4 py-2 bg-emerald-600 text-white rounded-lg hover:bg-emerald-700 transition-colors text-sm font-medium whitespace-nowrap"
                    >
                      해결 처리
                    </button>
                  )}
                </div>
              </div>
            );
          })}
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
