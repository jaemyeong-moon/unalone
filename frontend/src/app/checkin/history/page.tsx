'use client';

import { useEffect, useState, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import PageLayout from '@/components/common/PageLayout';
import AlertBanner from '@/components/common/AlertBanner';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import { isLoggedIn } from '@/lib/auth';
import { getCheckInHistory } from '@/lib/checkin';
import { getErrorMessage } from '@/lib/api';
import { EnhancedCheckInResponse } from '@/types';

const MOOD_EMOJI: Record<number, string> = {
  1: '\u{1F622}', 2: '\u{1F61F}', 3: '\u{1F610}', 4: '\u{1F642}', 5: '\u{1F604}',
};

const MOOD_LABEL: Record<number, string> = {
  1: '매우 나쁨', 2: '나쁨', 3: '보통', 4: '좋음', 5: '매우 좋음',
};

const MOOD_DOT_COLOR: Record<number, string> = {
  1: 'bg-red-500', 2: 'bg-orange-500', 3: 'bg-yellow-500', 4: 'bg-emerald-500', 5: 'bg-blue-500',
};

const TAG_LABELS: Record<string, string> = {
  SLEPT_WELL: '잘 잤어요',
  ATE_MEAL: '식사 했어요',
  EXERCISED: '운동 했어요',
  TOOK_MEDICINE: '약 먹었어요',
  WENT_OUT: '외출 했어요',
  IN_PAIN: '통증 있어요',
  LOW_ENERGY: '기운 없어요',
};

const PERIOD_OPTIONS = [
  { value: 7, label: '7일' },
  { value: 30, label: '30일' },
  { value: 0, label: '전체' },
];

function formatDate(dateStr: string): string {
  return new Date(dateStr).toLocaleDateString('ko-KR', {
    month: 'long',
    day: 'numeric',
    weekday: 'short',
  });
}

function formatTime(dateStr: string): string {
  return new Date(dateStr).toLocaleTimeString('ko-KR', {
    hour: '2-digit',
    minute: '2-digit',
  });
}

function getDateKey(dateStr: string): string {
  return new Date(dateStr).toISOString().slice(0, 10);
}

export default function CheckInHistoryPage() {
  const router = useRouter();
  const [checkIns, setCheckIns] = useState<EnhancedCheckInResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState('');
  const [period, setPeriod] = useState(7);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);

  useEffect(() => {
    if (!isLoggedIn()) {
      router.push('/login');
      return;
    }
    fetchData(true);
  }, [period]);

  const fetchData = useCallback(async (reset = false) => {
    const currentPage = reset ? 0 : page;
    if (reset) {
      setLoading(true);
      setPage(0);
    } else {
      setLoadingMore(true);
    }
    setError('');

    try {
      const data = await getCheckInHistory(currentPage, 20);
      const items = data.content;

      // Filter by period if needed
      let filtered = items;
      if (period > 0) {
        const cutoff = new Date();
        cutoff.setDate(cutoff.getDate() - period);
        filtered = items.filter((item) => new Date(item.checkedAt) >= cutoff);
      }

      if (reset) {
        setCheckIns(filtered);
      } else {
        setCheckIns((prev) => [...prev, ...filtered]);
      }
      setHasMore(data.page < data.totalPages - 1);
      setPage(currentPage + 1);
    } catch (err) {
      setError(getErrorMessage(err, '체크인 기록을 불러올 수 없습니다'));
    } finally {
      setLoading(false);
      setLoadingMore(false);
    }
  }, [period, page]);

  // Group by date
  const groupedByDate = checkIns.reduce<Map<string, EnhancedCheckInResponse[]>>((map, item) => {
    const key = getDateKey(item.checkedAt);
    if (!map.has(key)) map.set(key, []);
    map.get(key)!.push(item);
    return map;
  }, new Map());

  return (
    <PageLayout maxWidth="max-w-2xl">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">체크인 기록</h1>
        <p className="text-sm text-gray-500 mt-1">지난 체크인 기록을 확인하세요</p>
      </div>

      {/* Period Filter */}
      <div className="flex gap-2 mb-6">
        {PERIOD_OPTIONS.map((opt) => (
          <button
            key={opt.value}
            onClick={() => setPeriod(opt.value)}
            className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors
              ${period === opt.value
                ? 'bg-emerald-600 text-white'
                : 'bg-white text-gray-600 border border-gray-300 hover:bg-gray-50'
              }`}
            aria-pressed={period === opt.value}
          >
            {opt.label}
          </button>
        ))}
      </div>

      {error && <AlertBanner message={error} variant="error" />}

      {loading ? (
        <LoadingSpinner />
      ) : checkIns.length === 0 ? (
        <div className="text-center py-16">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-gray-100 mb-4">
            <svg className="h-8 w-8 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <p className="text-gray-500 mb-4">아직 체크인 기록이 없습니다</p>
          <Link
            href="/checkin"
            className="inline-flex px-4 py-2 bg-emerald-600 text-white rounded-lg text-sm font-medium hover:bg-emerald-700 transition-colors"
          >
            체크인 하러 가기
          </Link>
        </div>
      ) : (
        <div className="space-y-8">
          {Array.from(groupedByDate.entries()).map(([date, items]) => (
            <div key={date}>
              <h3 className="text-sm font-semibold text-gray-500 mb-3 sticky top-0 bg-gray-50 py-1 z-10">
                {formatDate(items[0].checkedAt)}
              </h3>
              <div className="relative pl-6 border-l-2 border-gray-200 space-y-4">
                {items.map((item) => (
                  <div key={item.id} className="relative">
                    {/* Timeline dot */}
                    <div className={`absolute -left-[25px] top-1 w-3 h-3 rounded-full border-2 border-white
                      ${item.mood ? MOOD_DOT_COLOR[item.mood] || 'bg-gray-400' : 'bg-gray-400'}`} />

                    <div className="bg-white rounded-lg border border-gray-200 p-4">
                      <div className="flex items-center justify-between mb-2">
                        <div className="flex items-center gap-2">
                          {item.mood && (
                            <span className="text-xl" aria-hidden="true">{MOOD_EMOJI[item.mood]}</span>
                          )}
                          <span className="text-sm font-medium text-gray-900">
                            {item.mood ? MOOD_LABEL[item.mood] : '기분 미입력'}
                          </span>
                        </div>
                        <span className="text-xs text-gray-400">{formatTime(item.checkedAt)}</span>
                      </div>

                      {/* Health tags */}
                      {item.healthTags && item.healthTags.length > 0 && (
                        <div className="flex flex-wrap gap-1.5 mb-2">
                          {item.healthTags.map((tag) => (
                            <span key={tag} className="inline-flex items-center px-2 py-0.5 rounded-full text-xs bg-gray-100 text-gray-600">
                              {TAG_LABELS[tag] || tag}
                            </span>
                          ))}
                        </div>
                      )}

                      {item.message && (
                        <p className="text-sm text-gray-600">{item.message}</p>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          ))}

          {/* Load more */}
          {hasMore && (
            <div className="text-center py-4">
              {loadingMore ? (
                <LoadingSpinner className="py-2" />
              ) : (
                <button
                  onClick={() => fetchData(false)}
                  className="px-6 py-2 border border-gray-300 text-gray-600 rounded-lg text-sm font-medium hover:bg-gray-50 transition-colors"
                >
                  더보기
                </button>
              )}
            </div>
          )}
        </div>
      )}
    </PageLayout>
  );
}
