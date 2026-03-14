'use client';

import { useEffect, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import PageLayout from '@/components/common/PageLayout';
import FormField from '@/components/common/FormField';
import AlertBanner from '@/components/common/AlertBanner';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import { isLoggedIn } from '@/lib/auth';
import { getVisit, getVisits, submitVisitReport } from '@/lib/care';
import { getErrorMessage } from '@/lib/api';
import { formatDateTime } from '@/lib/utils';
import { ReceiverCondition, CareVisitResponse } from '@/types';

const CONDITIONS: {
  value: ReceiverCondition;
  label: string;
  selectedBg: string;
  selectedText: string;
  selectedBorder: string;
}[] = [
  { value: 'GOOD', label: '양호', selectedBg: 'bg-emerald-100', selectedText: 'text-emerald-700', selectedBorder: 'border-emerald-300' },
  { value: 'FAIR', label: '보통', selectedBg: 'bg-yellow-100', selectedText: 'text-yellow-700', selectedBorder: 'border-yellow-300' },
  { value: 'POOR', label: '주의', selectedBg: 'bg-orange-100', selectedText: 'text-orange-700', selectedBorder: 'border-orange-300' },
  { value: 'CRITICAL', label: '위험', selectedBg: 'bg-red-100', selectedText: 'text-red-700', selectedBorder: 'border-red-300' },
];

const CONCERNS = [
  { value: 'HYGIENE', label: '위생 상태 불량' },
  { value: 'MALNUTRITION', label: '식사/영양 부족 의심' },
  { value: 'INJURY', label: '부상 또는 상처 발견' },
  { value: 'MENTAL_HEALTH', label: '정서적 불안 또는 우울 징후' },
  { value: 'MEDICATION', label: '약물 복용 미이행' },
  { value: 'MOBILITY', label: '거동 불편 심화' },
  { value: 'HOME_SAFETY', label: '주거 환경 안전 문제' },
  { value: 'OTHER', label: '기타 (관찰 사항에 기재)' },
];

interface ReportForm {
  visitId: number | null;
  condition: ReceiverCondition | '';
  observation: string;
  concerns: string[];
}

export default function NewReportPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const visitIdParam = searchParams.get('visitId');

  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState('');
  const [isError, setIsError] = useState(false);
  const [visit, setVisit] = useState<CareVisitResponse | null>(null);
  const [recentVisits, setRecentVisits] = useState<CareVisitResponse[]>([]);
  const [form, setForm] = useState<ReportForm>({
    visitId: visitIdParam ? Number(visitIdParam) : null,
    condition: '',
    observation: '',
    concerns: [],
  });

  useEffect(() => {
    if (!isLoggedIn()) {
      router.push('/login');
      return;
    }
    loadData();
  }, [router, visitIdParam]);

  const loadData = async () => {
    setLoading(true);
    try {
      if (visitIdParam) {
        const v = await getVisit(Number(visitIdParam));
        setVisit(v);
        setForm((prev) => ({ ...prev, visitId: v.id }));
      } else {
        // Load recent completed visits without reports
        const data = await getVisits({ size: 20 });
        const completed = (data?.content ?? []).filter((v) => v.status === 'COMPLETED' && !v.reportId);
        setRecentVisits(completed);
      }
    } catch (err) {
      setMessage(getErrorMessage(err, '방문 정보를 불러오는데 실패했습니다'));
      setIsError(true);
    } finally {
      setLoading(false);
    }
  };

  const handleVisitSelect = async (id: number) => {
    try {
      const v = await getVisit(id);
      setVisit(v);
      setForm((prev) => ({ ...prev, visitId: v.id }));
    } catch (err) {
      setMessage(getErrorMessage(err, '방문 정보를 불러오는데 실패했습니다'));
      setIsError(true);
    }
  };

  const toggleConcern = (value: string) => {
    setForm((prev) => ({
      ...prev,
      concerns: prev.concerns.includes(value)
        ? prev.concerns.filter((c) => c !== value)
        : [...prev.concerns, value],
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.visitId || !form.condition || !form.observation) return;

    if (form.condition === 'CRITICAL') {
      if (!confirm('위험 상태를 선택하셨습니다. 관리자에게 즉시 알림이 발송됩니다. 제출하시겠습니까?')) return;
    } else {
      if (!confirm('보고서를 제출하시겠습니까?')) return;
    }

    setSubmitting(true);
    setMessage('');
    try {
      await submitVisitReport({
        visitId: form.visitId,
        condition: form.condition as ReceiverCondition,
        observation: form.observation,
        concerns: form.concerns,
      });
      setMessage('보고서가 제출되었습니다');
      setIsError(false);
      setTimeout(() => router.push('/care'), 1500);
    } catch (err) {
      setMessage(getErrorMessage(err, '보고서 제출에 실패했습니다'));
      setIsError(true);
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <PageLayout maxWidth="max-w-2xl">
        <LoadingSpinner />
      </PageLayout>
    );
  }

  return (
    <PageLayout maxWidth="max-w-2xl">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">방문 보고서 작성</h1>
        <p className="text-sm text-gray-500 mt-1">방문 내용을 기록해주세요</p>
      </div>

      {message && <AlertBanner message={message} variant={isError ? 'error' : 'success'} />}

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* 방문 선택 (no visitId param) */}
        {!visitIdParam && !visit && (
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">방문 선택</h2>
            {recentVisits.length === 0 ? (
              <p className="text-sm text-gray-500 text-center py-4">보고서를 작성할 수 있는 방문이 없습니다</p>
            ) : (
              <div className="space-y-2">
                {recentVisits.map((v) => (
                  <button
                    key={v.id}
                    type="button"
                    onClick={() => handleVisitSelect(v.id)}
                    className="w-full text-left p-3 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
                  >
                    <p className="text-sm font-medium text-gray-900">{v.partnerName}</p>
                    <p className="text-xs text-gray-500">{formatDateTime(v.scheduledAt)} | {v.duration}</p>
                  </button>
                ))}
              </div>
            )}
          </div>
        )}

        {/* 방문 정보 */}
        {visit && (
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">방문 정보</h2>
            <dl className="space-y-3">
              <div className="flex justify-between">
                <dt className="text-sm text-gray-600">방문 대상</dt>
                <dd className="text-sm font-medium text-gray-900">{visit.recipientName || visit.partnerName}</dd>
              </div>
              <div className="flex justify-between">
                <dt className="text-sm text-gray-600">방문 일시</dt>
                <dd className="text-sm font-medium text-gray-900">{formatDateTime(visit.scheduledAt)}</dd>
              </div>
              <div className="flex justify-between">
                <dt className="text-sm text-gray-600">방문 시간</dt>
                <dd className="text-sm font-medium text-gray-900">{visit.duration}</dd>
              </div>
            </dl>
          </div>
        )}

        {/* 돌봄 대상 상태 평가 */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-2">돌봄 대상 상태</h2>
          <p className="text-sm text-gray-500 mb-4">방문 시 관찰한 상태를 선택해주세요</p>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">전반적 상태</label>
            <div className="flex gap-2">
              {CONDITIONS.map((c) => (
                <button
                  key={c.value}
                  type="button"
                  onClick={() => setForm({ ...form, condition: c.value })}
                  className={`flex-1 py-2.5 rounded-lg text-sm font-medium transition-colors border
                    ${form.condition === c.value
                      ? `${c.selectedBg} ${c.selectedText} ${c.selectedBorder}`
                      : 'bg-white text-gray-600 border-gray-300 hover:bg-gray-50'
                    }`}
                  aria-pressed={form.condition === c.value}
                >
                  {c.label}
                </button>
              ))}
            </div>
          </div>
        </div>

        {/* 관찰 사항 */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">관찰 사항</h2>
          <FormField
            as="textarea"
            label="방문 내용"
            value={form.observation}
            onChange={(e) => setForm({ ...form, observation: (e.target as HTMLTextAreaElement).value })}
            rows={5}
            placeholder="방문 시 관찰한 내용을 자유롭게 작성해주세요"
          />
        </div>

        {/* 특이사항 체크리스트 */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">특이사항</h2>
          <div className="space-y-3">
            {CONCERNS.map((concern) => (
              <label key={concern.value} className="flex items-center gap-3 cursor-pointer">
                <input
                  type="checkbox"
                  checked={form.concerns.includes(concern.value)}
                  onChange={() => toggleConcern(concern.value)}
                  className="h-5 w-5 rounded border-gray-300 text-emerald-600 focus:ring-emerald-500"
                />
                <span className="text-sm text-gray-700">{concern.label}</span>
              </label>
            ))}
          </div>
        </div>

        <button
          type="submit"
          disabled={submitting || !form.condition || !form.observation || !form.visitId}
          className="w-full py-3 bg-emerald-600 text-white rounded-xl font-semibold hover:bg-emerald-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        >
          {submitting ? '제출 중...' : '보고서 제출'}
        </button>
      </form>
    </PageLayout>
  );
}
