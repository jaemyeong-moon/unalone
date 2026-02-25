'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import Header from '@/components/common/Header';
import { adminClient } from '@/lib/api';
import { isLoggedIn, getUser } from '@/lib/auth';
import { ApiResponse, DashboardResponse } from '@/types';

export default function AdminPage() {
  const router = useRouter();
  const [dashboard, setDashboard] = useState<DashboardResponse | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const user = getUser();
    if (!isLoggedIn() || user?.role !== 'ROLE_ADMIN') {
      router.push('/');
      return;
    }
    fetchDashboard();
  }, []);

  const fetchDashboard = async () => {
    try {
      const res = await adminClient.get<ApiResponse<DashboardResponse>>('/admin/api/dashboard');
      setDashboard(res.data.data);
    } catch {
      console.error('Failed to fetch dashboard');
    } finally {
      setLoading(false);
    }
  };

  const statCards = dashboard ? [
    { label: '전체 사용자', value: dashboard.totalUsers, color: 'bg-blue-500' },
    { label: '활성 사용자', value: dashboard.activeUsers, color: 'bg-emerald-500' },
    { label: '오늘 체크인', value: dashboard.todayCheckIns, color: 'bg-purple-500' },
    { label: '활성 알림', value: dashboard.activeAlerts, color: 'bg-red-500' },
  ] : [];

  const alertBreakdown = dashboard ? [
    { label: '주의', value: dashboard.warningAlerts, color: 'text-yellow-600', bg: 'bg-yellow-50' },
    { label: '위험', value: dashboard.dangerAlerts, color: 'text-orange-600', bg: 'bg-orange-50' },
    { label: '긴급', value: dashboard.criticalAlerts, color: 'text-red-600', bg: 'bg-red-50' },
  ] : [];

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-8">
          <h1 className="text-2xl font-bold text-gray-900">관리자 대시보드</h1>
          <p className="text-sm text-gray-500 mt-1">사용자 안전 모니터링 현황</p>
        </div>

        {loading ? (
          <div className="flex justify-center py-12">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-emerald-600" />
          </div>
        ) : (
          <div className="space-y-6">
            {/* 통계 카드 */}
            <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
              {statCards.map((stat) => (
                <div key={stat.label} className="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
                  <p className="text-sm text-gray-500">{stat.label}</p>
                  <p className="text-3xl font-bold text-gray-900 mt-1">{stat.value}</p>
                  <div className={`h-1 w-12 ${stat.color} rounded-full mt-3`} />
                </div>
              ))}
            </div>

            {/* 알림 단계별 현황 */}
            <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">알림 단계별 현황</h2>
              <div className="grid grid-cols-3 gap-4">
                {alertBreakdown.map((alert) => (
                  <div key={alert.label} className={`${alert.bg} rounded-lg p-4 text-center`}>
                    <p className={`text-2xl font-bold ${alert.color}`}>{alert.value}</p>
                    <p className={`text-sm font-medium ${alert.color} mt-1`}>{alert.label}</p>
                  </div>
                ))}
              </div>
            </div>

            {/* 바로가기 */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <Link
                href="/admin/users"
                className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow flex items-center gap-4"
              >
                <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
                  </svg>
                </div>
                <div>
                  <h3 className="font-semibold text-gray-900">사용자 관리</h3>
                  <p className="text-sm text-gray-500">사용자 목록 조회 및 상태 관리</p>
                </div>
              </Link>
              <Link
                href="/admin/alerts"
                className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow flex items-center gap-4"
              >
                <div className="w-12 h-12 bg-red-100 rounded-lg flex items-center justify-center">
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
                  </svg>
                </div>
                <div>
                  <h3 className="font-semibold text-gray-900">알림 관리</h3>
                  <p className="text-sm text-gray-500">이상 감지 알림 확인 및 처리</p>
                </div>
              </Link>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}
