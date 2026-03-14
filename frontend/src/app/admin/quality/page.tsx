'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import PageLayout from '@/components/common/PageLayout';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import Pagination from '@/components/common/Pagination';
import QualityBadge from '@/components/common/QualityBadge';
import { isLoggedIn, getUser } from '@/lib/auth';
import { getErrorMessage } from '@/lib/api';
import {
  getQualityStats,
  getFlaggedPosts,
  overrideQualityGrade,
} from '@/lib/community';
import { formatRelativeDate } from '@/lib/utils';
import {
  QualityGrade,
  QualityStatsResponse,
  CommunityPostResponse,
} from '@/types';

const GRADE_LABELS: Record<QualityGrade, string> = {
  EXCELLENT: '우수',
  GOOD: '좋음',
  NORMAL: '보통',
  LOW: '부족',
  SPAM: '스팸',
};

const GRADE_COLORS: Record<QualityGrade, string> = {
  EXCELLENT: 'bg-yellow-400',
  GOOD: 'bg-emerald-400',
  NORMAL: 'bg-blue-400',
  LOW: 'bg-gray-400',
  SPAM: 'bg-red-400',
};

export default function AdminQualityPage() {
  const router = useRouter();
  const [stats, setStats] = useState<QualityStatsResponse | null>(null);
  const [flaggedPosts, setFlaggedPosts] = useState<CommunityPostResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [flaggedPage, setFlaggedPage] = useState(0);
  const [flaggedTotalPages, setFlaggedTotalPages] = useState(0);

  useEffect(() => {
    const user = getUser();
    if (!isLoggedIn() || user?.role !== 'ROLE_ADMIN') {
      router.push('/');
      return;
    }
    fetchData();
  }, []);

  useEffect(() => {
    fetchFlagged();
  }, [flaggedPage]);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [statsData] = await Promise.all([
        getQualityStats(),
        fetchFlagged(),
      ]);
      setStats(statsData);
    } catch {
      console.error('Failed to fetch quality data');
    } finally {
      setLoading(false);
    }
  };

  const fetchFlagged = async () => {
    try {
      const data = await getFlaggedPosts(flaggedPage);
      setFlaggedPosts(data.content);
      setFlaggedTotalPages(data.totalPages);
    } catch {
      console.error('Failed to fetch flagged posts');
    }
  };

  const handleOverrideGrade = async (postId: number, grade: QualityGrade) => {
    try {
      await overrideQualityGrade(postId, grade);
      fetchFlagged();
      // Refresh stats too
      try {
        const statsData = await getQualityStats();
        setStats(statsData);
      } catch {
        // ignore
      }
    } catch (err) {
      alert(getErrorMessage(err, '등급 변경에 실패했습니다'));
    }
  };

  const totalDistribution = stats
    ? Object.values(stats.gradeDistribution).reduce((a, b) => a + b, 0)
    : 0;

  return (
    <PageLayout maxWidth="max-w-7xl">
      <div className="flex items-center gap-4 mb-6">
        <Link href="/admin" className="text-sm text-gray-500 hover:text-gray-700">
          <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 inline mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
          대시보드
        </Link>
        <div>
          <h1 className="text-2xl font-bold text-gray-900">품질 관리</h1>
          <p className="text-sm text-gray-500 mt-1">콘텐츠 품질 점수 현황 및 관리</p>
        </div>
      </div>

      {loading ? (
        <LoadingSpinner />
      ) : (
        <div className="space-y-6">
          {/* Overview Cards */}
          {stats && (
            <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
              <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
                <p className="text-sm text-gray-500">평균 점수</p>
                <p className="text-3xl font-bold text-gray-900 mt-1">{stats.averageScore.toFixed(1)}</p>
                <div className="h-1 w-12 bg-emerald-500 rounded-full mt-3" />
              </div>
              <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
                <p className="text-sm text-gray-500">총 평가 수</p>
                <p className="text-3xl font-bold text-gray-900 mt-1">{stats.totalScored}</p>
                <div className="h-1 w-12 bg-blue-500 rounded-full mt-3" />
              </div>
              <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
                <p className="text-sm text-gray-500">플래그된 콘텐츠</p>
                <p className={`text-3xl font-bold mt-1 ${stats.flaggedCount > 0 ? 'text-red-600' : 'text-gray-900'}`}>
                  {stats.flaggedCount}
                </p>
                <div className={`h-1 w-12 ${stats.flaggedCount > 0 ? 'bg-red-500' : 'bg-gray-400'} rounded-full mt-3`} />
              </div>
              <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
                <p className="text-sm text-gray-500">우수 콘텐츠</p>
                <p className="text-3xl font-bold text-yellow-600 mt-1">
                  {stats.gradeDistribution.EXCELLENT ?? 0}
                </p>
                <div className="h-1 w-12 bg-yellow-400 rounded-full mt-3" />
              </div>
            </div>
          )}

          {/* Grade Distribution */}
          {stats && (
            <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">등급 분포</h2>
              <div className="space-y-3">
                {(Object.entries(GRADE_LABELS) as [QualityGrade, string][]).map(([grade, label]) => {
                  const count = stats.gradeDistribution[grade] ?? 0;
                  const pct = totalDistribution > 0 ? (count / totalDistribution) * 100 : 0;
                  return (
                    <div key={grade} className="flex items-center gap-3">
                      <span className="w-12 text-sm font-medium text-gray-600">{label}</span>
                      <div className="flex-1 bg-gray-100 rounded-full h-6 overflow-hidden">
                        <div
                          className={`h-full ${GRADE_COLORS[grade]} rounded-full transition-all duration-500 flex items-center justify-end pr-2`}
                          style={{ width: `${Math.max(pct, pct > 0 ? 8 : 0)}%` }}
                        >
                          {pct > 10 && (
                            <span className="text-xs font-medium text-white">{count}</span>
                          )}
                        </div>
                      </div>
                      <span className="w-16 text-right text-sm text-gray-500">{pct.toFixed(1)}%</span>
                    </div>
                  );
                })}
              </div>
            </div>
          )}

          {/* Flagged Posts */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-200">
            <div className="p-6 border-b border-gray-100">
              <h2 className="text-lg font-semibold text-gray-900">플래그된 게시글</h2>
              <p className="text-sm text-gray-500 mt-1">품질 점수 30 미만의 게시글</p>
            </div>
            {flaggedPosts.length === 0 ? (
              <div className="p-8 text-center text-gray-500">플래그된 게시글이 없습니다</div>
            ) : (
              <div className="divide-y divide-gray-100">
                {flaggedPosts.map((post) => (
                  <div key={post.id} className="p-4 hover:bg-gray-50">
                    <div className="flex items-start justify-between gap-4">
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2 mb-1">
                          {post.qualityGrade && (
                            <QualityBadge grade={post.qualityGrade} score={post.qualityScore} />
                          )}
                          <Link
                            href={`/community/${post.id}`}
                            className="text-sm font-medium text-gray-900 hover:text-emerald-600 truncate"
                          >
                            {post.title}
                          </Link>
                        </div>
                        <p className="text-xs text-gray-500 line-clamp-1">{post.content}</p>
                        <div className="flex items-center gap-2 mt-1 text-xs text-gray-400">
                          <span>{post.userName}</span>
                          <span>|</span>
                          <span>{formatRelativeDate(post.createdAt)}</span>
                          {post.qualityScore !== undefined && (
                            <>
                              <span>|</span>
                              <span>점수: {post.qualityScore}</span>
                            </>
                          )}
                        </div>
                      </div>
                      <div className="flex-shrink-0">
                        <select
                          defaultValue=""
                          onChange={(e) => {
                            if (e.target.value) {
                              handleOverrideGrade(post.id, e.target.value as QualityGrade);
                              e.target.value = '';
                            }
                          }}
                          className="text-xs border border-gray-300 rounded px-2 py-1 focus:ring-2 focus:ring-emerald-500"
                        >
                          <option value="">등급 변경</option>
                          {(Object.entries(GRADE_LABELS) as [QualityGrade, string][]).map(([grade, label]) => (
                            <option key={grade} value={grade}>{label}</option>
                          ))}
                        </select>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          <Pagination
            page={flaggedPage}
            totalPages={flaggedTotalPages}
            onPrev={() => setFlaggedPage(Math.max(0, flaggedPage - 1))}
            onNext={() => setFlaggedPage(Math.min(flaggedTotalPages - 1, flaggedPage + 1))}
          />
        </div>
      )}
    </PageLayout>
  );
}
