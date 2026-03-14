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
import { ApiResponse, PageResponse, VolunteerAdminResponse } from '@/types';

interface StatusFilter {
  value: string;
  label: string;
}

const STATUS_FILTERS: StatusFilter[] = [
  { value: '', label: '전체' },
  { value: 'PENDING', label: '대기' },
  { value: 'APPROVED', label: '승인' },
  { value: 'SUSPENDED', label: '정지' },
];

interface StatusBadge {
  bg: string;
  text: string;
  label: string;
}

const STATUS_BADGES: Record<string, StatusBadge> = {
  PENDING: { bg: 'bg-amber-50', text: 'text-amber-700', label: '대기' },
  APPROVED: { bg: 'bg-emerald-50', text: 'text-emerald-700', label: '승인' },
  SUSPENDED: { bg: 'bg-red-50', text: 'text-red-700', label: '정지' },
  WITHDRAWN: { bg: 'bg-gray-50', text: 'text-gray-500', label: '탈퇴' },
};

function getTrustScoreColor(score: number): string {
  if (score >= 80) return 'text-emerald-600';
  if (score >= 60) return 'text-blue-600';
  if (score >= 40) return 'text-yellow-600';
  return 'text-red-600';
}

function getTrustScoreBg(score: number): string {
  if (score >= 80) return 'bg-emerald-100';
  if (score >= 60) return 'bg-blue-100';
  if (score >= 40) return 'bg-yellow-100';
  return 'bg-red-100';
}

export default function AdminVolunteersPage() {
  const router = useRouter();
  const [volunteers, setVolunteers] = useState<VolunteerAdminResponse[]>([]);
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
    fetchVolunteers();
  }, [page, statusFilter]);

  const fetchVolunteers = async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams({ page: String(page), size: '20' });
      if (statusFilter) params.append('status', statusFilter);
      const res = await adminClient.get<ApiResponse<PageResponse<VolunteerAdminResponse>>>(
        `/api/admin/volunteers?${params}`
      );
      setVolunteers(res.data.data.content);
      setTotalPages(res.data.data.totalPages);
    } catch {
      console.error('Failed to fetch volunteers');
    } finally {
      setLoading(false);
    }
  };

  const approveVolunteer = async (id: number) => {
    if (!confirm('이 자원봉사자를 승인하시겠습니까?')) return;
    try {
      await adminClient.put(`/api/admin/volunteers/${id}/approve`);
      fetchVolunteers();
    } catch {
      alert('자원봉사자 승인에 실패했습니다');
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
          <h1 className="text-2xl font-bold text-gray-900">자원봉사자 관리</h1>
          <p className="text-sm text-gray-500 mt-1">자원봉사자 승인 및 현황을 관리하세요</p>
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
      ) : volunteers.length === 0 ? (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-12 text-center">
          <p className="text-gray-500">자원봉사자가 없습니다</p>
        </div>
      ) : (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">상태</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">신뢰 점수</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">방문 횟수</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">등록일</th>
                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">조치</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {volunteers.map((vol) => {
                const badge = STATUS_BADGES[vol.status] || { bg: 'bg-gray-50', text: 'text-gray-500', label: vol.status };
                return (
                  <tr key={vol.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-medium text-gray-900">봉사자 #{vol.id}</div>
                      <div className="text-xs text-gray-500">사용자 ID: {vol.userId}</div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex px-2.5 py-0.5 rounded-full text-xs font-bold ${badge.text} ${badge.bg}`}>
                        {badge.label}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-lg text-sm font-bold ${getTrustScoreColor(vol.trustScore)} ${getTrustScoreBg(vol.trustScore)}`}>
                        {vol.trustScore}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {vol.totalVisits}회
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {formatShortDateTime(vol.createdAt)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right">
                      {vol.status === 'PENDING' && (
                        <button
                          onClick={() => approveVolunteer(vol.id)}
                          className="px-4 py-2 bg-emerald-600 text-white rounded-lg hover:bg-emerald-700 transition-colors text-sm font-medium"
                        >
                          승인
                        </button>
                      )}
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
