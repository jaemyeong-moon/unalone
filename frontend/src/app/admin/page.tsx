'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import PageLayout from '@/components/common/PageLayout';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import { adminClient } from '@/lib/api';
import { isLoggedIn, getUser } from '@/lib/auth';
import { ApiResponse, DashboardResponse } from '@/types';

interface StatCard {
  label: string;
  value: number | string;
  color: string;
  textColor?: string;
}

interface AlertBreakdown {
  label: string;
  value: number;
  textColor: string;
  bgColor: string;
}

interface QuickLink {
  href: string;
  title: string;
  description: string;
  iconBg: string;
  iconColor: string;
  icon: React.ReactNode;
  badge?: string;
  badgeColor?: string;
}

function getHealthScoreColor(score: number): string {
  if (score > 70) return 'text-emerald-600';
  if (score > 50) return 'text-yellow-600';
  return 'text-red-600';
}

function getHealthScoreBg(score: number): string {
  if (score > 70) return 'bg-emerald-500';
  if (score > 50) return 'bg-yellow-500';
  return 'bg-red-500';
}

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
      const res = await adminClient.get<ApiResponse<DashboardResponse>>('/api/admin/dashboard');
      setDashboard(res.data.data);
    } catch {
      console.error('Failed to fetch dashboard');
    } finally {
      setLoading(false);
    }
  };

  const statCards: StatCard[] = dashboard
    ? [
        { label: '전체 사용자', value: dashboard.totalUsers, color: 'bg-blue-500' },
        { label: '활성 사용자', value: dashboard.activeUsers, color: 'bg-emerald-500' },
        {
          label: '오늘 체크인',
          value: `${dashboard.todayCheckIns} / ${dashboard.todayCheckIns + dashboard.missedCheckIns}`,
          color: 'bg-purple-500',
        },
        { label: '활성 알림', value: dashboard.activeAlerts, color: 'bg-red-500' },
      ]
    : [];

  const newStatCards: StatCard[] = dashboard
    ? [
        {
          label: '활성 에스컬레이션',
          value: dashboard.activeEscalations,
          color: dashboard.activeEscalations > 0 ? 'bg-orange-500' : 'bg-gray-400',
          textColor: dashboard.activeEscalations > 0 ? 'text-orange-600' : undefined,
        },
        {
          label: '자원봉사 승인 대기',
          value: dashboard.pendingVolunteers,
          color: dashboard.pendingVolunteers > 0 ? 'bg-amber-500' : 'bg-gray-400',
        },
        {
          label: '활성 돌봄 매칭',
          value: dashboard.activeCareMatches,
          color: 'bg-teal-500',
        },
        {
          label: '평균 건강 점수',
          value: dashboard.avgHealthScore,
          color: getHealthScoreBg(dashboard.avgHealthScore),
          textColor: getHealthScoreColor(dashboard.avgHealthScore),
        },
        {
          label: '위험 건강 알림 (주간)',
          value: dashboard.criticalHealthAlerts,
          color: dashboard.criticalHealthAlerts > 0 ? 'bg-red-500' : 'bg-gray-400',
          textColor: dashboard.criticalHealthAlerts > 0 ? 'text-red-600' : undefined,
        },
      ]
    : [];

  const alertBreakdown: AlertBreakdown[] = dashboard
    ? [
        { label: '주의', value: dashboard.warningAlerts, textColor: 'text-yellow-600', bgColor: 'bg-yellow-50' },
        { label: '위험', value: dashboard.dangerAlerts, textColor: 'text-orange-600', bgColor: 'bg-orange-50' },
        { label: '긴급', value: dashboard.criticalAlerts, textColor: 'text-red-600', bgColor: 'bg-red-50' },
      ]
    : [];

  const quickLinks: QuickLink[] = [
    {
      href: '/admin/users',
      title: '사용자 관리',
      description: '사용자 목록 조회 및 상태 관리',
      iconBg: 'bg-blue-100',
      iconColor: 'text-blue-600',
      icon: (
        <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
        </svg>
      ),
    },
    {
      href: '/admin/alerts',
      title: '알림 관리',
      description: '이상 감지 알림 확인 및 처리',
      iconBg: 'bg-red-100',
      iconColor: 'text-red-600',
      icon: (
        <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
        </svg>
      ),
    },
    {
      href: '/admin/escalations',
      title: '에스컬레이션 관리',
      description: '미응답 에스컬레이션 현황 및 해결',
      iconBg: 'bg-orange-100',
      iconColor: 'text-orange-600',
      icon: (
        <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z" />
        </svg>
      ),
      badge: dashboard && dashboard.activeEscalations > 0 ? String(dashboard.activeEscalations) : undefined,
      badgeColor: 'bg-orange-500',
    },
    {
      href: '/admin/volunteers',
      title: '자원봉사자 관리',
      description: '자원봉사자 승인 및 현황 관리',
      iconBg: 'bg-emerald-100',
      iconColor: 'text-emerald-600',
      icon: (
        <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
        </svg>
      ),
      badge: dashboard && dashboard.pendingVolunteers > 0 ? String(dashboard.pendingVolunteers) : undefined,
      badgeColor: 'bg-amber-500',
    },
    {
      href: '/admin/care',
      title: '돌봄 방문 보고서',
      description: '돌봄 방문 기록 및 보고서 관리',
      iconBg: 'bg-teal-100',
      iconColor: 'text-teal-600',
      icon: (
        <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
        </svg>
      ),
    },
    {
      href: '/admin/articles',
      title: '기사 관리',
      description: '뉴스 소스 및 기사 게시 관리',
      iconBg: 'bg-indigo-100',
      iconColor: 'text-indigo-600',
      icon: (
        <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z" />
        </svg>
      ),
    },
    {
      href: '/admin/quality',
      title: '품질 관리',
      description: '콘텐츠 품질 점수 및 플래그 관리',
      iconBg: 'bg-yellow-100',
      iconColor: 'text-yellow-600',
      icon: (
        <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      ),
    },
  ];

  return (
    <PageLayout maxWidth="max-w-7xl">
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-900">관리자 대시보드</h1>
        <p className="text-sm text-gray-500 mt-1">사용자 안전 모니터링 현황</p>
      </div>

      {loading ? (
        <LoadingSpinner />
      ) : (
        <div className="space-y-6">
          {/* 기본 통계 카드 */}
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
            {statCards.map((stat) => (
              <div key={stat.label} className="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
                <p className="text-sm text-gray-500">{stat.label}</p>
                <p className={`text-3xl font-bold mt-1 ${stat.textColor || 'text-gray-900'}`}>{stat.value}</p>
                <div className={`h-1 w-12 ${stat.color} rounded-full mt-3`} />
              </div>
            ))}
          </div>

          {/* 신규 기능 통계 카드 */}
          <div>
            <h2 className="text-lg font-semibold text-gray-900 mb-3">신규 기능 현황</h2>
            <div className="grid grid-cols-2 lg:grid-cols-5 gap-4">
              {newStatCards.map((stat) => (
                <div key={stat.label} className="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
                  <p className="text-sm text-gray-500">{stat.label}</p>
                  <p className={`text-3xl font-bold mt-1 ${stat.textColor || 'text-gray-900'}`}>{stat.value}</p>
                  <div className={`h-1 w-12 ${stat.color} rounded-full mt-3`} />
                </div>
              ))}
            </div>
          </div>

          {/* 알림 단계별 현황 */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">알림 단계별 현황</h2>
            <div className="grid grid-cols-3 gap-4">
              {alertBreakdown.map((alert) => (
                <div key={alert.label} className={`${alert.bgColor} rounded-lg p-4 text-center`}>
                  <p className={`text-2xl font-bold ${alert.textColor}`}>{alert.value}</p>
                  <p className={`text-sm font-medium ${alert.textColor} mt-1`}>{alert.label}</p>
                </div>
              ))}
            </div>
          </div>

          {/* 바로가기 */}
          <div>
            <h2 className="text-lg font-semibold text-gray-900 mb-3">바로가기</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {quickLinks.map((link) => (
                <Link
                  key={link.href}
                  href={link.href}
                  className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow flex items-center gap-4 relative"
                >
                  <div className={`w-12 h-12 ${link.iconBg} rounded-lg flex items-center justify-center ${link.iconColor}`}>
                    {link.icon}
                  </div>
                  <div className="flex-1">
                    <h3 className="font-semibold text-gray-900">{link.title}</h3>
                    <p className="text-sm text-gray-500">{link.description}</p>
                  </div>
                  {link.badge && (
                    <span className={`${link.badgeColor || 'bg-red-500'} text-white text-xs font-bold px-2 py-1 rounded-full`}>
                      {link.badge}
                    </span>
                  )}
                </Link>
              ))}
            </div>
          </div>
        </div>
      )}
    </PageLayout>
  );
}
