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
import { ApiResponse, PageResponse, CareVisitAdminResponse, ReceiverCondition } from '@/types';

interface ConditionFilter {
  value: string;
  label: string;
}

const CONDITION_FILTERS: ConditionFilter[] = [
  { value: '', label: '전체' },
  { value: 'GOOD', label: '양호' },
  { value: 'FAIR', label: '보통' },
  { value: 'POOR', label: '나쁨' },
  { value: 'CRITICAL', label: '위험' },
];

interface ConditionBadge {
  bg: string;
  text: string;
  label: string;
}

const CONDITION_BADGES: Record<ReceiverCondition, ConditionBadge> = {
  GOOD: { bg: 'bg-emerald-50', text: 'text-emerald-700', label: '양호' },
  FAIR: { bg: 'bg-blue-50', text: 'text-blue-700', label: '보통' },
  POOR: { bg: 'bg-orange-50', text: 'text-orange-700', label: '나쁨' },
  CRITICAL: { bg: 'bg-red-50', text: 'text-red-700', label: '위험' },
};

const STATUS_LABELS: Record<string, string> = {
  SCHEDULED: '예정',
  COMPLETED: '완료',
  CANCELLED: '취소',
  NO_SHOW: '미방문',
};

export default function AdminCarePage() {
  const router = useRouter();
  const [visits, setVisits] = useState<CareVisitAdminResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [conditionFilter, setConditionFilter] = useState('');
  const [selectedVisit, setSelectedVisit] = useState<CareVisitAdminResponse | null>(null);

  useEffect(() => {
    const user = getUser();
    if (!isLoggedIn() || user?.role !== 'ROLE_ADMIN') {
      router.push('/');
      return;
    }
    fetchVisits();
  }, [page, conditionFilter]);

  const fetchVisits = async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams({ page: String(page), size: '20' });
      if (conditionFilter) params.append('condition', conditionFilter);
      const res = await adminClient.get<ApiResponse<PageResponse<CareVisitAdminResponse>>>(
        `/api/admin/care/reports?${params}`
      );
      setVisits(res.data.data.content);
      setTotalPages(res.data.data.totalPages);
    } catch {
      console.error('Failed to fetch care visits');
    } finally {
      setLoading(false);
    }
  };

  const handleFilterChange = (value: string) => {
    setConditionFilter(value);
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
          <h1 className="text-2xl font-bold text-gray-900">돌봄 방문 보고서</h1>
          <p className="text-sm text-gray-500 mt-1">돌봄 방문 기록 및 보고서를 확인하세요</p>
        </div>
      </div>

      {/* 필터 */}
      <div className="flex gap-2 mb-4">
        {CONDITION_FILTERS.map((filter) => (
          <button
            key={filter.value}
            onClick={() => handleFilterChange(filter.value)}
            className={`px-3 py-1.5 rounded-full text-sm font-medium transition-colors ${
              conditionFilter === filter.value
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
      ) : visits.length === 0 ? (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-12 text-center">
          <p className="text-gray-500">돌봄 방문 기록이 없습니다</p>
        </div>
      ) : (
        <div className="space-y-3">
          {visits.map((visit) => {
            const condBadge = visit.receiverCondition
              ? CONDITION_BADGES[visit.receiverCondition]
              : null;
            return (
              <div
                key={visit.id}
                className="bg-white rounded-xl shadow-sm border border-gray-200 p-5 hover:shadow-md transition-shadow cursor-pointer"
                onClick={() => setSelectedVisit(selectedVisit?.id === visit.id ? null : visit)}
              >
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-2">
                      <span className="px-2 py-0.5 rounded text-xs font-medium bg-gray-100 text-gray-600">
                        {STATUS_LABELS[visit.status] || visit.status}
                      </span>
                      {condBadge && (
                        <span className={`px-2 py-0.5 rounded text-xs font-bold ${condBadge.text} ${condBadge.bg}`}>
                          {condBadge.label}
                        </span>
                      )}
                    </div>
                    <div className="flex items-center gap-4 text-sm text-gray-600">
                      <span>봉사자 ID: {visit.volunteerId}</span>
                      <span>|</span>
                      <span>수혜자 ID: {visit.receiverId}</span>
                      <span>|</span>
                      <span>방문일: {visit.scheduledDate}</span>
                      <span>{visit.scheduledTime}</span>
                    </div>
                  </div>
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    className={`h-5 w-5 text-gray-400 transition-transform ${selectedVisit?.id === visit.id ? 'rotate-180' : ''}`}
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                  >
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                  </svg>
                </div>

                {/* 상세 보기 */}
                {selectedVisit?.id === visit.id && (
                  <div className="mt-4 pt-4 border-t border-gray-100 space-y-3">
                    {visit.reportContent && (
                      <div>
                        <p className="text-xs font-medium text-gray-500 mb-1">보고 내용</p>
                        <p className="text-sm text-gray-800 bg-gray-50 rounded-lg p-3">{visit.reportContent}</p>
                      </div>
                    )}
                    {visit.specialNotes && (
                      <div>
                        <p className="text-xs font-medium text-gray-500 mb-1">특이사항</p>
                        <p className="text-sm text-gray-800 bg-yellow-50 rounded-lg p-3">{visit.specialNotes}</p>
                      </div>
                    )}
                    <div className="flex gap-4 text-xs text-gray-400">
                      {visit.visitedAt && <span>실제 방문: {formatShortDateTime(visit.visitedAt)}</span>}
                      <span>등록: {formatShortDateTime(visit.createdAt)}</span>
                    </div>
                  </div>
                )}
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
