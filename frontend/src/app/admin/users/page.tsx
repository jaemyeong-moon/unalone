'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import Header from '@/components/common/Header';
import { adminClient } from '@/lib/api';
import { isLoggedIn, getUser } from '@/lib/auth';
import { ApiResponse, PageResponse, UserDetailResponse } from '@/types';

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
      const res = await adminClient.get<ApiResponse<PageResponse<UserDetailResponse>>>(`/admin/api/users?page=${page}&size=20`);
      setUsers(res.data.data.content);
      setTotalPages(res.data.data.totalPages);
    } catch {
      console.error('Failed to fetch users');
    } finally {
      setLoading(false);
    }
  };

  const updateStatus = async (userId: number, status: string) => {
    if (!confirm(`사용자 상태를 ${status}(으)로 변경하시겠습니까?`)) return;
    try {
      await adminClient.patch(`/admin/api/users/${userId}/status?status=${status}`);
      fetchUsers();
    } catch {
      alert('상태 변경에 실패했습니다');
    }
  };

  const formatDate = (dateStr: string | null) => {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('ko-KR', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return 'bg-emerald-50 text-emerald-700';
      case 'INACTIVE':
        return 'bg-gray-100 text-gray-600';
      case 'SUSPENDED':
        return 'bg-red-50 text-red-700';
      default:
        return 'bg-gray-100 text-gray-600';
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
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
          <div className="flex justify-center py-12">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-emerald-600" />
          </div>
        ) : (
          <>
            <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">이름</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">이메일</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">전화번호</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">권한</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">상태</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">마지막 체크인</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">가입일</th>
                      <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">작업</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100">
                    {users.map((user) => (
                      <tr key={user.id} className="hover:bg-gray-50">
                        <td className="px-6 py-4 text-sm font-medium text-gray-900">{user.name}</td>
                        <td className="px-6 py-4 text-sm text-gray-500">{user.email}</td>
                        <td className="px-6 py-4 text-sm text-gray-500">{user.phone || '-'}</td>
                        <td className="px-6 py-4 text-sm">
                          <span className={`px-2 py-0.5 rounded text-xs font-medium ${
                            user.role === 'ROLE_ADMIN' ? 'bg-purple-50 text-purple-700' : 'bg-blue-50 text-blue-700'
                          }`}>
                            {user.role === 'ROLE_ADMIN' ? 'ADMIN' : 'USER'}
                          </span>
                        </td>
                        <td className="px-6 py-4 text-sm">
                          <span className={`px-2 py-0.5 rounded text-xs font-medium ${getStatusBadge(user.status)}`}>
                            {user.status}
                          </span>
                        </td>
                        <td className="px-6 py-4 text-sm text-gray-500">{formatDate(user.lastCheckInAt)}</td>
                        <td className="px-6 py-4 text-sm text-gray-500">{formatDate(user.createdAt)}</td>
                        <td className="px-6 py-4 text-right">
                          <select
                            value=""
                            onChange={(e) => {
                              if (e.target.value) updateStatus(user.id, e.target.value);
                            }}
                            className="text-xs border border-gray-300 rounded px-2 py-1"
                          >
                            <option value="">상태 변경</option>
                            <option value="ACTIVE">활성화</option>
                            <option value="INACTIVE">비활성화</option>
                            <option value="SUSPENDED">정지</option>
                          </select>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            {totalPages > 1 && (
              <div className="flex justify-center gap-2 mt-6">
                <button
                  onClick={() => setPage(Math.max(0, page - 1))}
                  disabled={page === 0}
                  className="px-4 py-2 border border-gray-300 rounded-lg disabled:opacity-50 hover:bg-gray-50 text-sm"
                >
                  이전
                </button>
                <span className="px-4 py-2 text-sm text-gray-600">{page + 1} / {totalPages}</span>
                <button
                  onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
                  disabled={page >= totalPages - 1}
                  className="px-4 py-2 border border-gray-300 rounded-lg disabled:opacity-50 hover:bg-gray-50 text-sm"
                >
                  다음
                </button>
              </div>
            )}
          </>
        )}
      </main>
    </div>
  );
}
