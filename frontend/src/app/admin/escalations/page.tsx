'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import PageLayout from '@/components/common/PageLayout';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import Pagination from '@/components/common/Pagination';
import { adminClient } from '@/lib/api';
import { isLoggedIn, getUser } from '@/lib/auth';
import { formatShortDateTime, getTimeSince } from '@/lib/utils';
import { ApiResponse, PageResponse, EscalationAdminResponse, EscalationStage } from '@/types';

interface StageFilter {
  value: string;
  label: string;
}

const STAGE_FILTERS: StageFilter[] = [
  { value: '', label: '전체' },
  { value: 'REMINDER', label: '리마인더' },
  { value: 'WARNING', label: '주의' },
  { value: 'DANGER', label: '위험' },
  { value: 'CRITICAL', label: '긴급' },
];

interface StageBadge {
  bg: string;
  text: string;
  label: string;
}

const STAGE_BADGES: Record<EscalationStage, StageBadge> = {
  REMINDER: { bg: 'bg-blue-50 border-blue-200', text: 'text-blue-700', label: '리마인더' },
  WARNING: { bg: 'bg-yellow-50 border-yellow-200', text: 'text-yellow-700', label: '주의' },
  DANGER: { bg: 'bg-orange-50 border-orange-200', text: 'text-orange-700', label: '위험' },
  CRITICAL: { bg: 'bg-red-50 border-red-200', text: 'text-red-700', label: '긴급' },
};

export default function AdminEscalationsPage() {
  const router = useRouter();
  const [escalations, setEscalations] = useState<EscalationAdminResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [stageFilter, setStageFilter] = useState('');

  useEffect(() => {
    const user = getUser();
    if (!isLoggedIn() || user?.role !== 'ROLE_ADMIN') {
      router.push('/');
      return;
    }
    fetchEscalations();
  }, [page, stageFilter]);

  const fetchEscalations = async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams({ page: String(page), size: '20' });
      if (stageFilter) params.append('stage', stageFilter);
      const res = await adminClient.get<ApiResponse<PageResponse<EscalationAdminResponse>>>(
        `/api/admin/escalations?${params}`
      );
      setEscalations(res.data.data.content);
      setTotalPages(res.data.data.totalPages);
    } catch {
      console.error('Failed to fetch escalations');
    } finally {
      setLoading(false);
    }
  };

  const resolveEscalation = async (id: number) => {
    if (!confirm('이 에스컬레이션을 해결 처리하시겠습니까?')) return;
    try {
      await adminClient.put(`/api/admin/escalations/${id}/resolve`);
      fetchEscalations();
    } catch {
      alert('에스컬레이션 해결에 실패했습니다');
    }
  };

  const handleFilterChange = (value: string) => {
    setStageFilter(value);
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
          <h1 className="text-2xl font-bold text-gray-900">에스컬레이션 관리</h1>
          <p className="text-sm text-gray-500 mt-1">미응답 에스컬레이션을 확인하고 해결하세요</p>
        </div>
      </div>

      {/* 필터 */}
      <div className="flex gap-2 mb-4">
        {STAGE_FILTERS.map((filter) => (
          <button
            key={filter.value}
            onClick={() => handleFilterChange(filter.value)}
            className={`px-3 py-1.5 rounded-full text-sm font-medium transition-colors ${
              stageFilter === filter.value
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
      ) : escalations.length === 0 ? (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-12 text-center">
          <p className="text-gray-500">활성 에스컬레이션이 없습니다</p>
        </div>
      ) : (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">사용자</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">단계</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">발생 시간</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">경과</th>
                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">조치</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {escalations.map((esc) => {
                const badge = STAGE_BADGES[esc.stage];
                return (
                  <tr key={esc.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-medium text-gray-900">{esc.userName}</div>
                      <div className="text-xs text-gray-500">ID: {esc.userId}</div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex px-2.5 py-0.5 rounded-full text-xs font-bold ${badge.text} ${badge.bg}`}>
                        {badge.label}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {formatShortDateTime(esc.triggeredAt)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {getTimeSince(esc.triggeredAt)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right">
                      <button
                        onClick={() => resolveEscalation(esc.id)}
                        className="px-4 py-2 bg-emerald-600 text-white rounded-lg hover:bg-emerald-700 transition-colors text-sm font-medium"
                      >
                        해결
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
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
