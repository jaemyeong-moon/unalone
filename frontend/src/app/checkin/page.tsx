'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import PageLayout from '@/components/common/PageLayout';
import AlertBanner from '@/components/common/AlertBanner';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import MoodSelector from '@/components/common/MoodSelector';
import HealthQuickSelect from '@/components/common/HealthQuickSelect';
import { isLoggedIn } from '@/lib/auth';
import { createCheckIn, getCheckInHistory } from '@/lib/checkin';
import { getErrorMessage } from '@/lib/api';
import { EnhancedCheckInResponse } from '@/types';
import { formatDateTime, getTimeSince } from '@/lib/utils';

const MOOD_EMOJI: Record<number, string> = {
  1: '\u{1F622}', 2: '\u{1F61F}', 3: '\u{1F610}', 4: '\u{1F642}', 5: '\u{1F604}',
};

export default function CheckInPage() {
  const router = useRouter();
  const [mood, setMood] = useState<number | null>(null);
  const [healthTags, setHealthTags] = useState<string[]>([]);
  const [message, setMessage] = useState('');
  const [checkingIn, setCheckingIn] = useState(false);
  const [banner, setBanner] = useState<{ text: string; variant: 'success' | 'error' } | null>(null);
  const [recentCheckIns, setRecentCheckIns] = useState<EnhancedCheckInResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isLoggedIn()) {
      router.push('/login');
      return;
    }
    fetchRecent();
  }, []);

  const fetchRecent = async () => {
    try {
      const data = await getCheckInHistory(0, 3);
      setRecentCheckIns(data.content);
    } catch {
      // silent
    } finally {
      setLoading(false);
    }
  };

  const handleCheckIn = async () => {
    if (!mood) return;
    setCheckingIn(true);
    setBanner(null);
    try {
      await createCheckIn({
        mood,
        healthTags,
        message: message || undefined,
      });
      setBanner({ text: '체크인이 완료되었습니다!', variant: 'success' });
      setTimeout(() => router.push('/'), 2000);
    } catch (error) {
      setBanner({ text: getErrorMessage(error, '체크인에 실패했습니다. 다시 시도해주세요.'), variant: 'error' });
    } finally {
      setCheckingIn(false);
    }
  };

  return (
    <PageLayout maxWidth="max-w-2xl">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">스마트 체크인</h1>
        <p className="text-sm text-gray-500 mt-1">오늘의 기분과 건강 상태를 알려주세요</p>
      </div>

      {banner && <AlertBanner message={banner.text} variant={banner.variant} />}

      {loading ? (
        <LoadingSpinner />
      ) : (
        <div className="space-y-6">
          <MoodSelector value={mood} onChange={setMood} />

          <HealthQuickSelect selected={healthTags} onChange={setHealthTags} />

          {/* 메시지 입력 + 체크인 버튼 */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              하고 싶은 말 <span className="text-gray-400 font-normal">(선택)</span>
            </label>
            <textarea
              value={message}
              onChange={(e) => setMessage(e.target.value)}
              placeholder="오늘 하루는 어떠셨나요?"
              rows={3}
              disabled={checkingIn}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent text-sm resize-none"
            />
            <button
              onClick={handleCheckIn}
              disabled={checkingIn || !mood}
              className="w-full mt-4 py-4 bg-emerald-600 text-white rounded-xl font-semibold text-lg hover:bg-emerald-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
              aria-label="체크인 하기"
            >
              {checkingIn ? (
                <span className="flex items-center justify-center gap-2">
                  <LoadingSpinner className="" />
                  체크인 중...
                </span>
              ) : '체크인 하기'}
            </button>
          </div>

          {/* 최근 체크인 요약 */}
          {recentCheckIns.length > 0 && (
            <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
              <div className="flex items-center justify-between mb-4">
                <h2 className="text-lg font-semibold text-gray-900">최근 체크인</h2>
                <Link href="/checkin/history" className="text-sm text-emerald-600 hover:text-emerald-700 font-medium">
                  전체 보기
                </Link>
              </div>
              <div className="space-y-3">
                {recentCheckIns.map((checkIn) => (
                  <div key={checkIn.id} className="flex items-center justify-between py-2 border-b border-gray-100 last:border-0">
                    <div className="flex items-center gap-2">
                      {checkIn.mood && (
                        <span className="text-xl" aria-hidden="true">{MOOD_EMOJI[checkIn.mood] || ''}</span>
                      )}
                      <div>
                        <p className="text-sm font-medium text-gray-900">
                          {checkIn.status === 'COMPLETED' ? '체크인 완료' : checkIn.status}
                        </p>
                        {checkIn.message && (
                          <p className="text-xs text-gray-500 line-clamp-1">{checkIn.message}</p>
                        )}
                      </div>
                    </div>
                    <span className="text-xs text-gray-400 shrink-0">
                      {formatDateTime(checkIn.checkedAt)} ({getTimeSince(checkIn.checkedAt)})
                    </span>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* 설정 링크 */}
          <div className="flex justify-center gap-4">
            <Link
              href="/checkin/history"
              className="text-sm text-gray-500 hover:text-gray-700 font-medium"
            >
              체크인 기록
            </Link>
            <span className="text-gray-300">|</span>
            <Link
              href="/checkin/settings"
              className="text-sm text-gray-500 hover:text-gray-700 font-medium"
            >
              알림 설정
            </Link>
          </div>
        </div>
      )}
    </PageLayout>
  );
}
