'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import PageLayout from '@/components/common/PageLayout';
import AlertBanner from '@/components/common/AlertBanner';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import MoodSelector from '@/components/common/MoodSelector';
import FloatingActionButton from '@/components/common/FloatingActionButton';
import { isLoggedIn } from '@/lib/auth';
import { getHealthJournals, getHealthSummary } from '@/lib/health';
import apiClient from '@/lib/api';
import { getErrorMessage } from '@/lib/api';
import { HealthJournalResponse, HealthSummary, MoodTrendItem, ApiResponse } from '@/types';

const MOOD_EMOJI: Record<number, string> = {
  1: '\u{1F622}', 2: '\u{1F61F}', 3: '\u{1F610}', 4: '\u{1F642}', 5: '\u{1F604}',
};

const MOOD_LABEL: Record<number, string> = {
  1: '매우 나쁨', 2: '나쁨', 3: '보통', 4: '좋음', 5: '매우 좋음',
};

const MOOD_BG: Record<number, string> = {
  1: 'bg-red-50', 2: 'bg-orange-50', 3: 'bg-yellow-50', 4: 'bg-emerald-50', 5: 'bg-blue-50',
};

const MOOD_BAR_COLOR: Record<number, string> = {
  1: 'bg-red-400', 2: 'bg-orange-400', 3: 'bg-yellow-400', 4: 'bg-emerald-400', 5: 'bg-blue-400',
};

function formatDate(dateStr: string): string {
  return new Date(dateStr).toLocaleDateString('ko-KR', {
    month: 'long',
    day: 'numeric',
    weekday: 'short',
  });
}

export default function HealthPage() {
  const router = useRouter();
  const [journals, setJournals] = useState<HealthJournalResponse[]>([]);
  const [summary, setSummary] = useState<HealthSummary | null>(null);
  const [todayMood, setTodayMood] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!isLoggedIn()) {
      router.push('/login');
      return;
    }
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    setError('');
    try {
      const [journalData, summaryData] = await Promise.all([
        getHealthJournals(0, 10),
        getHealthSummary().catch(() => null),
      ]);
      setJournals(journalData.content);
      if (summaryData) {
        setSummary(summaryData);
        setTodayMood(summaryData.todayMood);
      }
    } catch (err) {
      setError(getErrorMessage(err, '데이터를 불러올 수 없습니다'));
    } finally {
      setLoading(false);
    }
  };

  const handleMoodSelect = async (mood: number) => {
    setTodayMood(mood);
    try {
      await apiClient.post<ApiResponse<unknown>>('/api/health/mood', {
        date: new Date().toISOString().slice(0, 10),
        mood,
      });
    } catch {
      // silent - optimistic update
    }
  };

  const weeklyData: MoodTrendItem[] = summary?.weeklyData || [];

  return (
    <PageLayout maxWidth="max-w-2xl">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">건강 일지</h1>
          <p className="text-sm text-gray-500 mt-1">매일의 건강 상태를 기록하세요</p>
        </div>
        <Link
          href="/health/trends"
          className="text-sm text-emerald-600 hover:text-emerald-700 font-medium hidden sm:inline"
        >
          트렌드 보기
        </Link>
      </div>

      {error && <AlertBanner message={error} variant="error" />}

      {loading ? (
        <LoadingSpinner />
      ) : (
        <div className="space-y-6">
          {/* Today's Mood */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <div className="flex items-center justify-between mb-4">
              <div>
                <h2 className="text-lg font-semibold text-gray-900">오늘의 기분</h2>
                <p className="text-sm text-gray-500">
                  {new Date().toLocaleDateString('ko-KR', { month: 'long', day: 'numeric', weekday: 'short' })}
                </p>
              </div>
              {todayMood && (
                <span className="text-xs text-emerald-600 font-medium bg-emerald-50 px-2 py-1 rounded-full">
                  기록 완료
                </span>
              )}
            </div>
            <MoodSelector
              value={todayMood}
              onChange={handleMoodSelect}
              title=""
              subtitle=""
            />
          </div>

          {/* Weekly Mini Chart */}
          {weeklyData.length > 0 && (
            <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
              <div className="flex items-center justify-between mb-4">
                <h2 className="text-base font-semibold text-gray-900">이번 주 기분</h2>
                <Link href="/health/trends" className="text-sm text-emerald-600 hover:text-emerald-700 font-medium">
                  상세 보기
                </Link>
              </div>

              <div className="flex items-end justify-between gap-2 h-24" role="img" aria-label="이번 주 기분 차트">
                {weeklyData.map((day) => (
                  <div key={day.date} className="flex-1 flex flex-col items-center gap-1">
                    <div
                      className={`w-full rounded-t-md transition-all duration-300 ${day.mood ? MOOD_BAR_COLOR[day.mood] || 'bg-gray-200' : 'bg-gray-200'}`}
                      style={{ height: `${day.mood ? (day.mood / 5) * 100 : 10}%` }}
                      aria-label={`${day.label}: ${day.mood ? MOOD_LABEL[day.mood] : '기록 없음'}`}
                    />
                    <span className="text-xs text-gray-400">{day.label}</span>
                  </div>
                ))}
              </div>

              <div className="flex justify-between mt-2 text-xs text-gray-400">
                <span>나쁨</span>
                <span>좋음</span>
              </div>
            </div>
          )}

          {/* Summary Cards */}
          {summary && (
            <div className="grid grid-cols-3 gap-4">
              <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-4 text-center">
                <span className="text-3xl block mb-1" aria-hidden="true">
                  {todayMood ? MOOD_EMOJI[todayMood] : '\u{2753}'}
                </span>
                <p className="text-xs text-gray-500">
                  {todayMood ? '오늘 기분' : '기분 미입력'}
                </p>
              </div>
              <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-4 text-center">
                <span className="text-2xl font-bold text-gray-900 block mb-1">
                  {summary.avgMood > 0 ? summary.avgMood.toFixed(1) : '-'}
                </span>
                <p className="text-xs text-gray-500">주간 평균</p>
              </div>
              <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-4 text-center">
                <span className="text-2xl font-bold text-emerald-600 block mb-1">
                  {summary.streak}일
                </span>
                <p className="text-xs text-gray-500">연속 기록</p>
              </div>
            </div>
          )}

          {/* Journal List */}
          <div>
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-semibold text-gray-900">최근 일지</h2>
              <Link
                href="/health/new"
                className="text-sm text-emerald-600 hover:text-emerald-700 font-medium hidden sm:inline"
              >
                + 새 일지 작성
              </Link>
            </div>

            {journals.length === 0 ? (
              <div className="text-center py-16 bg-white rounded-xl shadow-sm border border-gray-200">
                <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-gray-100 mb-4">
                  <svg className="h-8 w-8 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                  </svg>
                </div>
                <p className="text-gray-500 mb-4">아직 건강 일지가 없습니다</p>
                <Link
                  href="/health/new"
                  className="inline-flex px-4 py-2 bg-emerald-600 text-white rounded-lg text-sm font-medium hover:bg-emerald-700 transition-colors"
                >
                  일지 작성하기
                </Link>
              </div>
            ) : (
              <div className="space-y-3">
                {journals.map((journal) => (
                  <Link
                    key={journal.id}
                    href={`/health/${journal.id}`}
                    className="block bg-white rounded-lg border border-gray-200 p-4 hover:shadow-sm transition-shadow"
                  >
                    <div className="flex items-start gap-3">
                      <div className={`w-10 h-10 rounded-full flex items-center justify-center shrink-0 ${MOOD_BG[journal.mood] || 'bg-gray-100'}`}>
                        <span className="text-lg" aria-hidden="true">{MOOD_EMOJI[journal.mood] || ''}</span>
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center justify-between mb-1">
                          <span className="text-sm font-medium text-gray-900">{formatDate(journal.date)}</span>
                          <span className="text-xs text-gray-400">{MOOD_LABEL[journal.mood] || ''}</span>
                        </div>
                        {journal.tags && journal.tags.length > 0 && (
                          <div className="flex flex-wrap gap-1 mb-1.5">
                            {journal.tags.slice(0, 3).map((tag) => (
                              <span key={tag} className="inline-flex px-2 py-0.5 rounded-full text-xs bg-gray-100 text-gray-500">
                                {tag}
                              </span>
                            ))}
                            {journal.tags.length > 3 && (
                              <span className="text-xs text-gray-400">+{journal.tags.length - 3}</span>
                            )}
                          </div>
                        )}
                        {journal.note && (
                          <p className="text-sm text-gray-500 line-clamp-1">{journal.note}</p>
                        )}
                      </div>
                    </div>
                  </Link>
                ))}
              </div>
            )}
          </div>
        </div>
      )}

      <FloatingActionButton href="/health/new" label="건강 일지 작성" />
    </PageLayout>
  );
}
