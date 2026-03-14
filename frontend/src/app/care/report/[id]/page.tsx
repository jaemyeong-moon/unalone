'use client';

import { useEffect, useState } from 'react';
import { useRouter, useParams } from 'next/navigation';
import Link from 'next/link';
import PageLayout from '@/components/common/PageLayout';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import AlertBanner from '@/components/common/AlertBanner';
import ConditionBadge from '@/components/common/ConditionBadge';
import RatingStars from '@/components/care/RatingStars';
import { isLoggedIn } from '@/lib/auth';
import { getVisitReport, rateVisit } from '@/lib/care';
import { getErrorMessage } from '@/lib/api';
import { formatDateTime } from '@/lib/utils';
import { CareVisitReport } from '@/types';

const CONCERN_LABELS: Record<string, string> = {
  HYGIENE: '위생 상태 불량',
  MALNUTRITION: '식사/영양 부족 의심',
  INJURY: '부상 또는 상처 발견',
  MENTAL_HEALTH: '정서적 불안 또는 우울 징후',
  MEDICATION: '약물 복용 미이행',
  MOBILITY: '거동 불편 심화',
  HOME_SAFETY: '주거 환경 안전 문제',
  OTHER: '기타',
};

export default function ReportDetailPage() {
  const router = useRouter();
  const params = useParams();
  const reportId = Number(params.id);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [report, setReport] = useState<CareVisitReport | null>(null);
  const [rated, setRated] = useState(false);

  useEffect(() => {
    if (!isLoggedIn()) {
      router.push('/login');
      return;
    }
    loadReport();
  }, [router, reportId]);

  const loadReport = async () => {
    setLoading(true);
    try {
      const data = await getVisitReport(reportId);
      setReport(data);
    } catch (err) {
      setError(getErrorMessage(err, '보고서를 불러오는데 실패했습니다'));
    } finally {
      setLoading(false);
    }
  };

  const handleRateSubmit = async (rating: number, review: string) => {
    if (!report) return;
    try {
      await rateVisit(report.visitId, rating, review);
      setSuccess('평가가 제출되었습니다');
      setRated(true);
    } catch (err) {
      setError(getErrorMessage(err, '평가 제출에 실패했습니다'));
    }
  };

  if (loading) {
    return (
      <PageLayout maxWidth="max-w-2xl">
        <LoadingSpinner />
      </PageLayout>
    );
  }

  if (error && !report) {
    return (
      <PageLayout maxWidth="max-w-2xl">
        <AlertBanner message={error || '보고서를 찾을 수 없습니다'} variant="error" />
        <div className="text-center mt-4">
          <Link href="/care" className="text-sm text-emerald-600 hover:underline">
            돌봄 메인으로 돌아가기
          </Link>
        </div>
      </PageLayout>
    );
  }

  if (!report) {
    return (
      <PageLayout maxWidth="max-w-2xl">
        <AlertBanner message="보고서를 찾을 수 없습니다" variant="error" />
        <div className="text-center mt-4">
          <Link href="/care" className="text-sm text-emerald-600 hover:underline">
            돌봄 메인으로 돌아가기
          </Link>
        </div>
      </PageLayout>
    );
  }

  return (
    <PageLayout maxWidth="max-w-2xl">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">방문 보고서</h1>
        <p className="text-sm text-gray-500 mt-1">작성일: {formatDateTime(report.createdAt)}</p>
      </div>

      {error && <AlertBanner message={error} variant="error" />}
      {success && <AlertBanner message={success} variant="success" />}

      <div className="space-y-6">
        {/* 방문 정보 */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">방문 정보</h2>
          <dl className="space-y-3">
            <div className="flex justify-between py-2 border-b border-gray-100">
              <dt className="text-sm text-gray-600">방문자</dt>
              <dd className="text-sm font-medium text-gray-900">{report.volunteerName}</dd>
            </div>
            <div className="flex justify-between py-2 border-b border-gray-100">
              <dt className="text-sm text-gray-600">방문 대상</dt>
              <dd className="text-sm font-medium text-gray-900">{report.recipientName}</dd>
            </div>
            <div className="flex justify-between py-2 border-b border-gray-100">
              <dt className="text-sm text-gray-600">방문 일시</dt>
              <dd className="text-sm font-medium text-gray-900">{formatDateTime(report.scheduledAt)}</dd>
            </div>
            <div className="flex justify-between py-2">
              <dt className="text-sm text-gray-600">방문 시간</dt>
              <dd className="text-sm font-medium text-gray-900">{report.duration}</dd>
            </div>
          </dl>
        </div>

        {/* 돌봄 대상 상태 */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">돌봄 대상 상태</h2>
          <div className="flex items-center gap-2">
            <span className="text-sm text-gray-600">전반적 상태:</span>
            <ConditionBadge condition={report.condition} />
          </div>
        </div>

        {/* 관찰 사항 */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">관찰 사항</h2>
          <p className="text-sm text-gray-700 whitespace-pre-wrap">{report.observation}</p>
        </div>

        {/* 특이사항 */}
        {report.concerns.length > 0 && (
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">특이사항</h2>
            <div className="space-y-2">
              {report.concerns.map((concern) => (
                <div key={concern} className="flex items-center gap-2">
                  <svg className="h-4 w-4 text-orange-500 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L4.082 16.5c-.77.833.192 2.5 1.732 2.5z" />
                  </svg>
                  <span className="text-sm text-gray-700">{CONCERN_LABELS[concern] || concern}</span>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* 방문 평가 */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">방문 평가</h2>
          {rated ? (
            <div className="text-center py-4">
              <svg className="h-10 w-10 text-emerald-500 mx-auto mb-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <p className="text-sm text-gray-600">평가가 완료되었습니다</p>
            </div>
          ) : (
            <RatingStars onSubmit={handleRateSubmit} />
          )}
        </div>

        <div className="flex gap-3">
          <Link
            href="/care/schedule"
            className="flex-1 py-3 border border-gray-300 text-gray-700 rounded-xl font-semibold hover:bg-gray-50 transition-colors text-center"
          >
            일정으로 돌아가기
          </Link>
          <Link
            href="/care"
            className="flex-1 py-3 bg-emerald-600 text-white rounded-xl font-semibold hover:bg-emerald-700 transition-colors text-center"
          >
            돌봄 메인
          </Link>
        </div>
      </div>
    </PageLayout>
  );
}
