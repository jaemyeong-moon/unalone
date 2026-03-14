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
import { ApiResponse, PageResponse, UserDetailResponse, UserStatus, UserRole } from '@/types';

const STATUS_BADGE_CLASSES: Record<UserStatus, string> = {
  ACTIVE: 'bg-emerald-50 text-emerald-700',
  INACTIVE: 'bg-gray-100 text-gray-600',
  SUSPENDED: 'bg-red-50 text-red-700',
};

const ROLE_BADGE_CLASSES: Record<UserRole, string> = {
  ROLE_ADMIN: 'bg-purple-50 text-purple-700',
  ROLE_USER: 'bg-blue-50 text-blue-700',
};

const ROLE_LABELS: Record<UserRole, string> = {
  ROLE_ADMIN: 'ADMIN',
  ROLE_USER: 'USER',
};

const STATUS_OPTIONS: { value: UserStatus; label: string }[] = [
  { value: 'ACTIVE', label: '활성화' },
  { value: 'INACTIVE', label: '비활성화' },
  { value: 'SUSPENDED', label: '정지' },
];

export default function AdminUsersPage() {
  const router = useRouter();
  const [users, setUsers] = useState<UserDetailResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    const user = getUser();
    if (!isLoggedIn() || user?.role !== 'ROLE_ADMIN') {
      router.push('/');
      return;
    }
    fetchUsers();
  }, [page]);

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const res = await adminClient.get<ApiResponse<PageResponse<UserDetailResponse>>>(
        `/api/admin/users?page=${page}&size=20`
      );
      setUsers(res.data.data.content);
      setTotalPages(res.data.data.totalPages);
    } catch {
      console.error('Failed to fetch users');
    } finally {
      setLoading(false);
    }
  };

  const updateStatus = async (userId: number, status: UserStatus) => {
    if (!confirm(`사용자 상태를 ${status}(으)로 변경하시겠습니까?`)) return;
    try {
      await adminClient.patch(`/api/admin/users/${userId}/status?status=${status}`);
      fetchUsers();
    } catch {
      alert('상태 변경에 실패했습니다');
    }
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
          <h1 className="text-2xl font-bold text-gray-900">사용자 관리</h1>
          <p className="text-sm text-gray-500 mt-1">등록된 사용자 목록 및 상태 관리</p>
        </div>
      </div>

      {loading ? (
        <LoadingSpinner />
      ) : (
        <>
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    {['이름', '이메일', '전화번호', '권한', '상태', '마지막 체크인', '가입일', ''].map((th) => (
                      <th
                        key={th}
                        className={`px-6 py-3 text-xs font-medium text-gray-500 uppercase ${
                          th === '' ? 'text-right' : 'text-left'
                        }`}
                      >
                        {th}
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {users.map((user) => (
                    <tr key={user.id} className="hover:bg-gray-50">
                      <td className="px-6 py-4 text-sm font-medium text-gray-900">{user.name}</td>
                      <td className="px-6 py-4 text-sm text-gray-500">{user.email}</td>
                      <td className="px-6 py-4 text-sm text-gray-500">{user.phone || '-'}</td>
                      <td className="px-6 py-4 text-sm">
                        <span className={`px-2 py-0.5 rounded text-xs font-medium ${ROLE_BADGE_CLASSES[user.role]}`}>
                          {ROLE_LABELS[user.role]}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-sm">
                        <span className={`px-2 py-0.5 rounded text-xs font-medium ${STATUS_BADGE_CLASSES[user.status]}`}>
                          {user.status}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-500">
                        {formatShortDateTime(user.lastCheckInAt)}
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-500">
                        {formatShortDateTime(user.createdAt)}
                      </td>
                      <td className="px-6 py-4 text-right">
                        <select
                          value=""
                          onChange={(e) => {
                            if (e.target.value) updateStatus(user.id, e.target.value as UserStatus);
                          }}
                          className="text-xs border border-gray-300 rounded px-2 py-1"
                        >
                          <option value="">상태 변경</option>
                          {STATUS_OPTIONS.map((opt) => (
                            <option key={opt.value} value={opt.value}>{opt.label}</option>
                          ))}
                        </select>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          <Pagination
            page={page}
            totalPages={totalPages}
            onPrev={() => setPage(Math.max(0, page - 1))}
            onNext={() => setPage(Math.min(totalPages - 1, page + 1))}
          />
        </>
      )}
    </PageLayout>
  );
}
