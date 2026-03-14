'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import PageLayout from '@/components/common/PageLayout';
import AlertBanner from '@/components/common/AlertBanner';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import { isLoggedIn } from '@/lib/auth';
import { getHealthTrends, getHealthSummary } from '@/lib/health';
import { getErrorMessage } from '@/lib/api';
import { HealthTrendResponse, HealthSummary, MoodTrendItem, SymptomFrequencyItem, MealComplianceItem } from '@/types';

const MOOD_LABEL: Record<number, string> = {
  1: '매우 나쁨', 2: '나쁨', 3: '보통', 4: '좋음', 5: '매우 좋음',
};

const MOOD_EMOJI: Record<number, string> = {
  1: '\u{1F622}', 2: '\u{1F61F}', 3: '\u{1F610}', 4: '\u{1F642}', 5: '\u{1F604}',
};

const MOOD_DOT_COLOR: Record<number, string> = {
  1: 'bg-red-500', 2: 'bg-orange-500', 3: 'bg-yellow-500', 4: 'bg-emerald-500', 5: 'bg-blue-500',
};

function getAverageMoodEmoji(avg: number): string {
  const rounded = Math.round(avg);
  return MOOD_EMOJI[rounded] || MOOD_EMOJI[3];
}

export default function HealthTrendsPage() {
  const router = useRouter();
  const [period, setPeriod] = useState<'weekly' | 'monthly'>('weekly');
  const [trends, setTrends] = useState<HealthTrendResponse | null>(null);
  const [summary, setSummary] = useState<HealthSummary | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!isLoggedIn()) { router.push('/login'); return; }
    fetchData();
  }, [period]);

  const fetchData = async () => {
    setLoading(true);
    setError('');
    try {
      const [trendData, summaryData] = await Promise.all([
        getHealthTrends(period),
        getHealthSummary().catch(() => null),
      ]);
      setTrends(trendData);
      if (summaryData) setSummary(summaryData);
    } catch (err) {
      setError(getErrorMessage(err, '데이터를 불러올 수 없습니다'));
    } finally {
      setLoading(false);
    }
  };

  const maxSymptomCount = trends?.symptomFrequency?.length
    ? Math.max(...trends.symptomFrequency.map((s) => s.count), 1)
    : 1;

  return (
    <PageLayout maxWidth="max-w-4xl">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">건강 트렌드</h1>
          <p className="text-sm text-gray-500 mt-1">건강 패턴을 확인하세요</p>
        </div>
        <Link href="/health" className="text-sm text-emerald-600 hover:text-emerald-700 font-medium">
          일지 목록
        </Link>
      </div>

      {/* Period Tab */}
      <div className="flex gap-2 mb-6">
        {([
          { value: 'weekly' as const, label: '주간' },
          { value: 'monthly' as const, label: '월간' },
        ]).map((tab) => (
          <button
            key={tab.value}
            onClick={() => setPeriod(tab.value)}
            className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors
              ${period === tab.value
                ? 'bg-emerald-600 text-white'
                : 'bg-white text-gray-600 border border-gray-300 hover:bg-gray-50'
              }`}
            aria-pressed={period === tab.value}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {error && <AlertBanner message={error} variant="error" />}

      {loading ? (
        <LoadingSpinner />
      ) : (
        <div className="space-y-6">
          {/* Health Stat Cards */}
          {summary && (
            <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
              <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-4">
                <p className="text-xs text-gray-500 mb-1">평균 기분</p>
                <div className="flex items-center gap-2">
                  <span className="text-2xl" aria-hidden="true">{getAverageMoodEmoji(summary.avgMood)}</span>
                  <span className="text-xl font-bold text-gray-900">{summary.avgMood > 0 ? summary.avgMood.toFixed(1) : '-'}</span>
                </div>
                <p className={`text-xs mt-1 ${summary.moodTrend >= 0 ? 'text-emerald-600' : 'text-red-600'}`}>
                  {summary.moodTrend >= 0 ? '+' : ''}{summary.moodTrend.toFixed(1)} 지난 주 대비
                </p>
              </div>

              <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-4">
                <p className="text-xs text-gray-500 mb-1">기록률</p>
                <p className="text-xl font-bold text-gray-900">{summary.journalRate}%</p>
                <p className="text-xs text-gray-400 mt-1">{summary.journalDays}/{summary.totalDays}일 기록</p>
              </div>

              <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-4">
                <p className="text-xs text-gray-500 mb-1">식사 이행률</p>
                <p className="text-xl font-bold text-gray-900">{summary.mealRate}%</p>
                <div className="w-full bg-gray-200 rounded-full h-1.5 mt-2">
                  <div
                    className="bg-emerald-500 h-1.5 rounded-full transition-all duration-500"
                    style={{ width: `${summary.mealRate}%` }}
                    role="progressbar"
                    aria-valuenow={summary.mealRate}
                    aria-valuemin={0}
                    aria-valuemax={100}
                  />
                </div>
              </div>

              <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-4">
                <p className="text-xs text-gray-500 mb-1">가장 많은 증상</p>
                <p className="text-base font-bold text-gray-900">{summary.topSymptom || '없음'}</p>
                <p className="text-xs text-gray-400 mt-1">{summary.topSymptomCount}회 기록</p>
              </div>
            </div>
          )}

          {/* Mood Trend Chart */}
          {trends && trends.moodTrend.length > 0 && (
            <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">기분 변화</h2>

              <div className="flex gap-2">
                {/* Y axis */}
                <div className="flex flex-col justify-between text-xs text-gray-400 py-1 w-12 shrink-0">
                  <span>매우 좋음</span>
                  <span>좋음</span>
                  <span>보통</span>
                  <span>나쁨</span>
                  <span>매우 나쁨</span>
                </div>

                {/* Chart */}
                <div className="flex-1 relative h-48 border-l border-b border-gray-200">
                  {/* Horizontal grid lines */}
                  {[1, 2, 3, 4].map((i) => (
                    <div
                      key={i}
                      className="absolute w-full border-t border-gray-100"
                      style={{ bottom: `${(i / 5) * 100}%` }}
                    />
                  ))}

                  {/* Data points as bars */}
                  <div className="absolute inset-0 flex items-end justify-between px-1">
                    {trends.moodTrend.map((d, i) => (
                      <div key={i} className="flex-1 flex flex-col items-center px-0.5">
                        <div
                          className={`w-full max-w-[24px] rounded-t-sm transition-all duration-300 ${d.mood ? MOOD_DOT_COLOR[d.mood] || 'bg-gray-300' : 'bg-gray-200'}`}
                          style={{ height: `${d.mood ? (d.mood / 5) * 100 : 5}%` }}
                          aria-label={`${d.label}: ${d.mood ? MOOD_LABEL[d.mood] : '기록 없음'}`}
                        />
                      </div>
                    ))}
                  </div>
                </div>
              </div>

              {/* X axis */}
              <div className="flex justify-between pl-14 mt-2">
                {trends.moodTrend.map((d) => (
                  <span key={d.date} className="text-xs text-gray-400 flex-1 text-center">{d.label}</span>
                ))}
              </div>
            </div>
          )}

          {/* Symptom Frequency */}
          {trends && (
            <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">증상 빈도</h2>

              {!trends.symptomFrequency || trends.symptomFrequency.length === 0 ? (
                <p className="text-sm text-gray-400 py-4 text-center">기록된 증상이 없습니다</p>
              ) : (
                <div className="space-y-3">
                  {trends.symptomFrequency.map((item) => (
                    <div key={item.symptom} className="flex items-center gap-3">
                      <span className="text-sm text-gray-700 w-20 shrink-0 text-right">{item.label}</span>
                      <div className="flex-1 bg-gray-100 rounded-full h-5 overflow-hidden">
                        <div
                          className="bg-red-400 h-full rounded-full transition-all duration-500 flex items-center justify-end pr-2"
                          style={{ width: `${(item.count / maxSymptomCount) * 100}%`, minWidth: item.count > 0 ? '2rem' : '0' }}
                        >
                          {item.count > 0 && (
                            <span className="text-xs text-white font-medium">{item.count}</span>
                          )}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* Meal Compliance */}
          {trends && trends.mealCompliance && trends.mealCompliance.length > 0 && (
            <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">식사 이행률</h2>

              <div className="grid grid-cols-3 gap-4">
                {trends.mealCompliance.map((meal) => (
                  <div key={meal.type} className="text-center">
                    {/* Circular progress */}
                    <div className="relative w-20 h-20 mx-auto mb-2">
                      <svg className="w-20 h-20 -rotate-90" viewBox="0 0 36 36">
                        <circle
                          cx="18" cy="18" r="15.91549431"
                          fill="none" stroke="#e5e7eb" strokeWidth="3"
                        />
                        <circle
                          cx="18" cy="18" r="15.91549431"
                          fill="none" stroke="#10b981" strokeWidth="3"
                          strokeDasharray={`${meal.rate} ${100 - meal.rate}`}
                          strokeLinecap="round"
                          role="progressbar"
                          aria-valuenow={meal.rate}
                          aria-valuemin={0}
                          aria-valuemax={100}
                        />
                      </svg>
                      <div className="absolute inset-0 flex items-center justify-center">
                        <span className="text-sm font-bold text-gray-900">{meal.rate}%</span>
                      </div>
                    </div>
                    <p className="text-sm font-medium text-gray-700">{meal.label}</p>
                    <p className="text-xs text-gray-400">{meal.completed}/{meal.total}일</p>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* No data fallback */}
          {trends && trends.moodTrend.length === 0 && (
            <div className="text-center py-16 bg-white rounded-xl shadow-sm border border-gray-200">
              <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-gray-100 mb-4">
                <svg className="h-8 w-8 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                </svg>
              </div>
              <p className="text-gray-500 mb-2">데이터가 부족합니다</p>
              <p className="text-sm text-gray-400 mb-4">일지를 작성하면 트렌드를 확인할 수 있습니다</p>
              <Link
                href="/health/new"
                className="inline-flex px-4 py-2 bg-emerald-600 text-white rounded-lg text-sm font-medium hover:bg-emerald-700 transition-colors"
              >
                일지 작성하기
              </Link>
            </div>
          )}
        </div>
      )}
    </PageLayout>
  );
}
