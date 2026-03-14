'use client';

import { useEffect, useState, useMemo, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import PageLayout from '@/components/common/PageLayout';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import AlertBanner from '@/components/common/AlertBanner';
import FormField from '@/components/common/FormField';
import { isLoggedIn } from '@/lib/auth';
import { getVisits, getMyMatches, scheduleVisit, cancelVisit } from '@/lib/care';
import { getErrorMessage } from '@/lib/api';
import { CareVisitResponse, CareVisitStatus, CareMatchResponse } from '@/types';

const VISIT_STATUS_LABELS: Record<CareVisitStatus, string> = {
  UPCOMING: '예정',
  IN_PROGRESS: '진행중',
  COMPLETED: '완료',
  CANCELLED: '취소',
};

const VISIT_STATUS_STYLES: Record<CareVisitStatus, string> = {
  UPCOMING: 'bg-blue-100 text-blue-700',
  IN_PROGRESS: 'bg-yellow-100 text-yellow-700',
  COMPLETED: 'bg-emerald-100 text-emerald-700',
  CANCELLED: 'bg-gray-100 text-gray-500',
};

const VISIT_BAR_STYLES: Record<CareVisitStatus, string> = {
  UPCOMING: 'bg-blue-500',
  IN_PROGRESS: 'bg-yellow-500',
  COMPLETED: 'bg-emerald-500',
  CANCELLED: 'bg-gray-300',
};

interface CalendarDay {
  date: string;
  dayNumber: number;
  isToday: boolean;
  isOtherMonth: boolean;
  visitCount: number;
}

function buildCalendar(year: number, month: number, visits: CareVisitResponse[]): CalendarDay[] {
  const today = new Date();
  const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`;

  const firstDay = new Date(year, month - 1, 1);
  const lastDay = new Date(year, month, 0);
  const startDow = firstDay.getDay(); // 0=Sunday

  // Count visits per date
  const visitCounts: Record<string, number> = {};
  visits.forEach((v) => {
    const d = v.scheduledAt.slice(0, 10);
    visitCounts[d] = (visitCounts[d] || 0) + 1;
  });

  const days: CalendarDay[] = [];

  // Previous month padding
  const prevMonthLast = new Date(year, month - 1, 0).getDate();
  for (let i = startDow - 1; i >= 0; i--) {
    const dayNum = prevMonthLast - i;
    const m = month - 1 < 1 ? 12 : month - 1;
    const y = month - 1 < 1 ? year - 1 : year;
    const dateStr = `${y}-${String(m).padStart(2, '0')}-${String(dayNum).padStart(2, '0')}`;
    days.push({ date: dateStr, dayNumber: dayNum, isToday: false, isOtherMonth: true, visitCount: visitCounts[dateStr] || 0 });
  }

  // Current month
  for (let d = 1; d <= lastDay.getDate(); d++) {
    const dateStr = `${year}-${String(month).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
    days.push({ date: dateStr, dayNumber: d, isToday: dateStr === todayStr, isOtherMonth: false, visitCount: visitCounts[dateStr] || 0 });
  }

  // Next month padding
  const remaining = 42 - days.length;
  for (let d = 1; d <= remaining; d++) {
    const m = month + 1 > 12 ? 1 : month + 1;
    const y = month + 1 > 12 ? year + 1 : year;
    const dateStr = `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
    days.push({ date: dateStr, dayNumber: d, isToday: false, isOtherMonth: true, visitCount: visitCounts[dateStr] || 0 });
  }

  return days;
}

export default function SchedulePage() {
  const router = useRouter();
  const now = new Date();
  const [year, setYear] = useState(now.getFullYear());
  const [month, setMonth] = useState(now.getMonth() + 1);
  const [selectedDate, setSelectedDate] = useState(
    `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
  );
  const [visits, setVisits] = useState<CareVisitResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Schedule modal state
  const [showModal, setShowModal] = useState(false);
  const [matches, setMatches] = useState<CareMatchResponse[]>([]);
  const [scheduleForm, setScheduleForm] = useState({ matchId: '', scheduledAt: '', duration: '30분' });
  const [scheduling, setScheduling] = useState(false);

  useEffect(() => {
    if (!isLoggedIn()) {
      router.push('/login');
      return;
    }
    fetchVisits();
  }, [router, year, month]);

  const fetchVisits = async () => {
    setLoading(true);
    try {
      const data = await getVisits({ year, month, size: 100 });
      setVisits(data?.content ?? []);
    } catch (err) {
      setError(getErrorMessage(err, '방문 일정을 불러오는데 실패했습니다'));
    } finally {
      setLoading(false);
    }
  };

  const calendarDays = useMemo(() => buildCalendar(year, month, visits), [year, month, visits]);

  const selectedDayVisits = useMemo(
    () => visits.filter((v) => v.scheduledAt.startsWith(selectedDate)),
    [visits, selectedDate]
  );

  const handlePrevMonth = useCallback(() => {
    if (month === 1) { setYear(year - 1); setMonth(12); }
    else { setMonth(month - 1); }
  }, [year, month]);

  const handleNextMonth = useCallback(() => {
    if (month === 12) { setYear(year + 1); setMonth(1); }
    else { setMonth(month + 1); }
  }, [year, month]);

  const openScheduleModal = async () => {
    try {
      const data = await getMyMatches();
      setMatches((data ?? []).filter((m) => m.status === 'ACCEPTED'));
    } catch {
      // ignore
    }
    setScheduleForm({ matchId: '', scheduledAt: `${selectedDate}T10:00`, duration: '30분' });
    setShowModal(true);
  };

  const handleSchedule = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!scheduleForm.matchId || !scheduleForm.scheduledAt) return;
    setScheduling(true);
    try {
      await scheduleVisit({
        matchId: Number(scheduleForm.matchId),
        scheduledAt: scheduleForm.scheduledAt,
        duration: scheduleForm.duration,
      });
      setSuccess('방문 일정이 등록되었습니다');
      setShowModal(false);
      fetchVisits();
    } catch (err) {
      setError(getErrorMessage(err, '일정 등록에 실패했습니다'));
    } finally {
      setScheduling(false);
    }
  };

  const handleCancelVisit = async (id: number) => {
    if (!confirm('이 방문 일정을 취소하시겠습니까?')) return;
    try {
      await cancelVisit(id);
      setSuccess('방문이 취소되었습니다');
      fetchVisits();
    } catch (err) {
      setError(getErrorMessage(err, '취소에 실패했습니다'));
    }
  };

  const formatDate = (dateStr: string) => {
    const [, m, d] = dateStr.split('-');
    return `${Number(m)}월 ${Number(d)}일`;
  };

  return (
    <PageLayout>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">방문 일정</h1>
          <p className="text-sm text-gray-500 mt-1">돌봄 방문 일정을 관리합니다</p>
        </div>
        <button
          onClick={openScheduleModal}
          className="px-4 py-2 bg-emerald-600 text-white rounded-lg text-sm font-medium hover:bg-emerald-700 transition-colors"
        >
          새 방문 예약
        </button>
      </div>

      {error && <AlertBanner message={error} variant="error" />}
      {success && <AlertBanner message={success} variant="success" />}

      {loading ? (
        <LoadingSpinner />
      ) : (
        <div className="space-y-4">
          {/* Month Navigator */}
          <div className="flex items-center justify-between mb-4">
            <button onClick={handlePrevMonth} className="p-2 rounded-lg hover:bg-gray-100 transition-colors" aria-label="이전 달">
              <svg className="h-5 w-5 text-gray-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
              </svg>
            </button>
            <h2 className="text-lg font-semibold text-gray-900">{year}년 {month}월</h2>
            <button onClick={handleNextMonth} className="p-2 rounded-lg hover:bg-gray-100 transition-colors" aria-label="다음 달">
              <svg className="h-5 w-5 text-gray-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
              </svg>
            </button>
          </div>

          {/* Calendar */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
            <div className="grid grid-cols-7 bg-gray-50 border-b border-gray-200">
              {['일', '월', '화', '수', '목', '금', '토'].map((day, i) => (
                <div key={day} className={`py-2 text-center text-xs font-medium
                  ${i === 0 ? 'text-red-500' : i === 6 ? 'text-blue-500' : 'text-gray-500'}`}>
                  {day}
                </div>
              ))}
            </div>
            <div className="grid grid-cols-7">
              {calendarDays.map((day) => (
                <button
                  key={day.date}
                  onClick={() => !day.isOtherMonth && setSelectedDate(day.date)}
                  disabled={day.isOtherMonth}
                  className={`relative h-16 sm:h-20 p-1 border-b border-r border-gray-100 transition-colors
                    ${day.isToday ? 'bg-emerald-50' : ''}
                    ${day.date === selectedDate ? 'ring-2 ring-inset ring-emerald-500' : ''}
                    ${day.isOtherMonth ? 'opacity-30' : 'hover:bg-gray-50'}
                  `}
                  aria-label={`${day.date}, 방문 ${day.visitCount}건`}
                  aria-current={day.isToday ? 'date' : undefined}
                >
                  <span className={`text-sm font-medium ${day.isToday ? 'text-emerald-600' : 'text-gray-900'}`}>
                    {day.dayNumber}
                  </span>
                  {day.visitCount > 0 && (
                    <div className="absolute bottom-1 left-1/2 -translate-x-1/2 flex gap-0.5">
                      {Array.from({ length: Math.min(day.visitCount, 3) }).map((_, i) => (
                        <div key={i} className="w-1.5 h-1.5 rounded-full bg-emerald-500" />
                      ))}
                    </div>
                  )}
                </button>
              ))}
            </div>
          </div>

          {/* Day visits */}
          <div className="mt-4 space-y-3">
            <h3 className="text-sm font-semibold text-gray-700">
              {formatDate(selectedDate)} 방문 일정
            </h3>
            {selectedDayVisits.length === 0 ? (
              <p className="text-sm text-gray-400 py-4 text-center">이 날짜에 예정된 방문이 없습니다</p>
            ) : (
              selectedDayVisits.map((visit) => (
                <div key={visit.id} className="bg-white rounded-lg border border-gray-200 p-4 flex items-center gap-3">
                  <div className={`w-1 h-12 rounded-full shrink-0 ${VISIT_BAR_STYLES[visit.status]}`} />
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-gray-900">{visit.partnerName}</p>
                    <p className="text-xs text-gray-500">{visit.time} | {visit.duration}</p>
                  </div>
                  <span className={`px-2.5 py-1 rounded-full text-xs font-medium ${VISIT_STATUS_STYLES[visit.status]}`}>
                    {VISIT_STATUS_LABELS[visit.status]}
                  </span>
                  {visit.status === 'UPCOMING' && (
                    <button
                      onClick={() => handleCancelVisit(visit.id)}
                      className="text-xs text-red-500 hover:underline"
                    >
                      취소
                    </button>
                  )}
                  {visit.status === 'COMPLETED' && !visit.reportId && (
                    <Link
                      href={`/care/report/new?visitId=${visit.id}`}
                      className="text-xs text-emerald-600 hover:underline"
                    >
                      보고서 작성
                    </Link>
                  )}
                  {visit.reportId && (
                    <Link
                      href={`/care/report/${visit.reportId}`}
                      className="text-xs text-emerald-600 hover:underline"
                    >
                      보고서 보기
                    </Link>
                  )}
                </div>
              ))
            )}
          </div>
        </div>
      )}

      {/* Schedule Modal */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md mx-4 p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">새 방문 예약</h2>
            <form onSubmit={handleSchedule} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">매칭 선택</label>
                <select
                  value={scheduleForm.matchId}
                  onChange={(e) => setScheduleForm({ ...scheduleForm, matchId: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                  required
                >
                  <option value="">매칭을 선택하세요</option>
                  {matches.map((m) => (
                    <option key={m.id} value={m.id}>
                      {m.volunteerName} - {m.recipientName}
                    </option>
                  ))}
                </select>
              </div>
              <FormField
                label="방문 일시"
                type="datetime-local"
                value={scheduleForm.scheduledAt}
                onChange={(e) => setScheduleForm({ ...scheduleForm, scheduledAt: (e.target as HTMLInputElement).value })}
                required
              />
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">방문 시간</label>
                <select
                  value={scheduleForm.duration}
                  onChange={(e) => setScheduleForm({ ...scheduleForm, duration: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                >
                  <option value="30분">30분</option>
                  <option value="1시간">1시간</option>
                  <option value="1시간 30분">1시간 30분</option>
                  <option value="2시간">2시간</option>
                </select>
              </div>
              <div className="flex gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="flex-1 py-2.5 border border-gray-300 text-gray-700 rounded-lg font-medium hover:bg-gray-50 transition-colors"
                >
                  취소
                </button>
                <button
                  type="submit"
                  disabled={scheduling}
                  className="flex-1 py-2.5 bg-emerald-600 text-white rounded-lg font-medium hover:bg-emerald-700 disabled:opacity-50 transition-colors"
                >
                  {scheduling ? '등록 중...' : '예약'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </PageLayout>
  );
}
