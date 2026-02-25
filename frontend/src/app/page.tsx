'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import Header from '@/components/common/Header';
import apiClient from '@/lib/api';
import { isLoggedIn, getUser } from '@/lib/auth';
import { ApiResponse, CheckInResponse, PageResponse } from '@/types';

export default function Home() {
  const router = useRouter();
  const [latestCheckIn, setLatestCheckIn] = useState<CheckInResponse | null>(null);
  const [recentCheckIns, setRecentCheckIns] = useState<CheckInResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [checkingIn, setCheckingIn] = useState(false);
  const [message, setMessage] = useState('');
  const [user, setUser] = useState<{ name: string; role: string } | null>(null);

  useEffect(() => {
    if (isLoggedIn()) {
      setUser(getUser());
      fetchData();
    } else {
      setLoading(false);
    }
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [latestRes, historyRes] = await Promise.all([
        apiClient.get<ApiResponse<CheckInResponse>>('/api/checkins/latest').catch(() => null),
        apiClient.get<ApiResponse<PageResponse<CheckInResponse>>>('/api/checkins?page=0&size=5'),
      ]);
      if (latestRes?.data?.data) {
        setLatestCheckIn(latestRes.data.data);
      }
      if (historyRes?.data?.data?.content) {
        setRecentCheckIns(historyRes.data.data.content);
      }
    } catch {
      console.error('Failed to fetch check-in data');
    } finally {
      setLoading(false);
    }
  };

  const handleCheckIn = async () => {
    if (!isLoggedIn()) {
      router.push('/login');
      return;
    }
    setCheckingIn(true);
    try {
      const res = await apiClient.post<ApiResponse<CheckInResponse>>('/api/checkins', {
        message: message || undefined,
      });
      setLatestCheckIn(res.data.data);
      setMessage('');
      fetchData();
    } catch {
      alert('체크인에 실패했습니다. 다시 시도해주세요.');
    } finally {
      setCheckingIn(false);
    }
  };

  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    return date.toLocaleDateString('ko-KR', {
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const getTimeSince = (dateStr: string) => {
    const diff = Date.now() - new Date(dateStr).getTime();
    const hours = Math.floor(diff / (1000 * 60 * 60));
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
    if (hours > 24) return `${Math.floor(hours / 24)}일 전`;
    if (hours > 0) return `${hours}시간 ${minutes}분 전`;
    return `${minutes}분 전`;
  };

  // 비로그인 랜딩 페이지
  if (!isLoggedIn()) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Header />
        <main className="max-w-4xl mx-auto px-4 py-16 text-center">
          <div className="mb-8">
            <div className="inline-flex items-center justify-center w-20 h-20 rounded-full bg-emerald-100 mb-6">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-10 w-10 text-emerald-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
              </svg>
            </div>
            <h1 className="text-4xl font-bold text-gray-900 mb-4">Unalone</h1>
            <p className="text-xl text-gray-600 mb-2">혼자가 아닌 세상을 만듭니다</p>
            <p className="text-gray-500">일일 안부 체크를 통해 소중한 사람들의 안전을 지키는 커뮤니티 서비스</p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-12">
            <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
              <div className="w-12 h-12 bg-emerald-100 rounded-lg flex items-center justify-center mx-auto mb-4">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 text-emerald-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <h3 className="font-semibold text-gray-900 mb-2">안부 체크</h3>
              <p className="text-sm text-gray-500">매일 간단한 체크인으로 안전을 확인합니다</p>
            </div>
            <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
              <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center mx-auto mb-4">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z" />
                </svg>
              </div>
              <h3 className="font-semibold text-gray-900 mb-2">보호자 연결</h3>
              <p className="text-sm text-gray-500">미응답 시 보호자에게 자동으로 알림을 보냅니다</p>
            </div>
            <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
              <div className="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center mx-auto mb-4">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 text-purple-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 8h2a2 2 0 012 2v6a2 2 0 01-2 2h-2v4l-4-4H9a1.994 1.994 0 01-1.414-.586m0 0L11 14h4a2 2 0 002-2V6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2v4l.586-.586z" />
                </svg>
              </div>
              <h3 className="font-semibold text-gray-900 mb-2">커뮤니티</h3>
              <p className="text-sm text-gray-500">이웃과 소통하며 함께하는 일상을 만듭니다</p>
            </div>
          </div>

          <div className="flex justify-center gap-4">
            <Link
              href="/signup"
              className="px-8 py-3 bg-emerald-600 text-white rounded-lg font-medium hover:bg-emerald-700 transition-colors"
            >
              시작하기
            </Link>
            <Link
              href="/login"
              className="px-8 py-3 border border-gray-300 text-gray-700 rounded-lg font-medium hover:bg-gray-50 transition-colors"
            >
              로그인
            </Link>
          </div>
        </main>
      </div>
    );
  }

  // 로그인 대시보드
  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* 환영 메시지 */}
        <div className="mb-8">
          <h1 className="text-2xl font-bold text-gray-900">
            안녕하세요, {user?.name || '사용자'}님
          </h1>
          <p className="mt-1 text-gray-600">오늘도 안부를 전해주세요</p>
        </div>

        {loading ? (
          <div className="flex justify-center py-12">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-emerald-600" />
          </div>
        ) : (
          <div className="space-y-6">
            {/* 체크인 카드 */}
            <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">안부 체크</h2>

              {latestCheckIn && (
                <div className="mb-4 p-4 bg-emerald-50 rounded-lg border border-emerald-200">
                  <div className="flex items-center gap-2 mb-1">
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-emerald-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    <span className="text-sm font-medium text-emerald-700">마지막 체크인</span>
                  </div>
                  <p className="text-sm text-emerald-600">
                    {formatDate(latestCheckIn.checkedAt)} ({getTimeSince(latestCheckIn.checkedAt)})
                  </p>
                  {latestCheckIn.message && (
                    <p className="text-sm text-gray-600 mt-1">&quot;{latestCheckIn.message}&quot;</p>
                  )}
                </div>
              )}

              <div className="flex gap-3">
                <input
                  type="text"
                  value={message}
                  onChange={(e) => setMessage(e.target.value)}
                  placeholder="오늘 하루는 어떠셨나요? (선택)"
                  className="flex-1 px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent text-sm"
                />
                <button
                  onClick={handleCheckIn}
                  disabled={checkingIn}
                  className="px-6 py-3 bg-emerald-600 text-white rounded-lg font-medium hover:bg-emerald-700 disabled:opacity-50 transition-colors whitespace-nowrap"
                >
                  {checkingIn ? '체크인 중...' : '체크인'}
                </button>
              </div>
            </div>

            {/* 빠른 메뉴 */}
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              <Link href="/profile" className="bg-white rounded-xl shadow-sm border border-gray-200 p-4 hover:shadow-md transition-shadow text-center">
                <div className="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center mx-auto mb-2">
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                  </svg>
                </div>
                <span className="text-sm font-medium text-gray-700">프로필 설정</span>
              </Link>
              <Link href="/guardians" className="bg-white rounded-xl shadow-sm border border-gray-200 p-4 hover:shadow-md transition-shadow text-center">
                <div className="w-10 h-10 bg-orange-100 rounded-lg flex items-center justify-center mx-auto mb-2">
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-orange-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
                  </svg>
                </div>
                <span className="text-sm font-medium text-gray-700">보호자 관리</span>
              </Link>
              <Link href="/community" className="bg-white rounded-xl shadow-sm border border-gray-200 p-4 hover:shadow-md transition-shadow text-center">
                <div className="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center mx-auto mb-2">
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-purple-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 8h2a2 2 0 012 2v6a2 2 0 01-2 2h-2v4l-4-4H9a1.994 1.994 0 01-1.414-.586m0 0L11 14h4a2 2 0 002-2V6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2v4l.586-.586z" />
                  </svg>
                </div>
                <span className="text-sm font-medium text-gray-700">커뮤니티</span>
              </Link>
              {user?.role === 'ROLE_ADMIN' && (
                <Link href="/admin" className="bg-white rounded-xl shadow-sm border border-gray-200 p-4 hover:shadow-md transition-shadow text-center">
                  <div className="w-10 h-10 bg-red-100 rounded-lg flex items-center justify-center mx-auto mb-2">
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.066 2.573c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.573 1.066c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.066-2.573c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                    </svg>
                  </div>
                  <span className="text-sm font-medium text-gray-700">관리자</span>
                </Link>
              )}
            </div>

            {/* 최근 체크인 기록 */}
            <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">최근 체크인 기록</h2>
              {recentCheckIns.length === 0 ? (
                <p className="text-center py-8 text-gray-500">아직 체크인 기록이 없습니다</p>
              ) : (
                <div className="space-y-3">
                  {recentCheckIns.map((checkIn) => (
                    <div key={checkIn.id} className="flex items-center justify-between py-3 border-b border-gray-100 last:border-0">
                      <div className="flex items-center gap-3">
                        <div className={`w-2.5 h-2.5 rounded-full ${
                          checkIn.status === 'COMPLETED' ? 'bg-emerald-500' : 'bg-yellow-500'
                        }`} />
                        <div>
                          <p className="text-sm font-medium text-gray-900">
                            {checkIn.status === 'COMPLETED' ? '체크인 완료' : checkIn.status}
                          </p>
                          {checkIn.message && (
                            <p className="text-xs text-gray-500">{checkIn.message}</p>
                          )}
                        </div>
                      </div>
                      <span className="text-xs text-gray-400">{formatDate(checkIn.checkedAt)}</span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        )}
      </main>
    </div>
  );
}
