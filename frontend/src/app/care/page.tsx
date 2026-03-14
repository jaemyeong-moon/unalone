'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import PageLayout from '@/components/common/PageLayout';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import AlertBanner from '@/components/common/AlertBanner';
import TrustScoreBadge from '@/components/common/TrustScoreBadge';
import { isLoggedIn } from '@/lib/auth';
import { getMyVolunteerStatus, getMyMatches, getVisits } from '@/lib/care';
import { getErrorMessage } from '@/lib/api';
import { formatDateTime } from '@/lib/utils';
import { VolunteerResponse, CareMatchResponse, CareVisitResponse, CareMatchStatus } from '@/types';

type TabKey = 'request' | 'volunteer' | 'matches';

const TAB_ITEMS: { key: TabKey; label: string }[] = [
  { key: 'request', label: '돌봄 요청' },
  { key: 'volunteer', label: '자원봉사' },
  { key: 'matches', label: '매칭 현황' },
];

const MATCH_STATUS_STYLES: Record<CareMatchStatus, string> = {
  PENDING: 'bg-yellow-100 text-yellow-700',
  ACCEPTED: 'bg-emerald-100 text-emerald-700',
  COMPLETED: 'bg-gray-100 text-gray-600',
  CANCELLED: 'bg-red-100 text-red-600',
  REJECTED: 'bg-gray-100 text-gray-500',
};

const MATCH_STATUS_LABELS: Record<CareMatchStatus, string> = {
  PENDING: '대기중',
  ACCEPTED: '매칭중',
  COMPLETED: '완료',
  CANCELLED: '취소',
  REJECTED: '거절',
};

export default function CareMainPage() {
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [volunteer, setVolunteer] = useState<VolunteerResponse | null>(null);
  const [matches, setMatches] = useState<CareMatchResponse[]>([]);
  const [upcomingVisits, setUpcomingVisits] = useState<CareVisitResponse[]>([]);
  const [activeTab, setActiveTab] = useState<TabKey>('request');

  useEffect(() => {
    if (!isLoggedIn()) {
      router.push('/login');
      return;
    }
    fetchData();
  }, [router]);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [vol, mat, vis] = await Promise.all([
        getMyVolunteerStatus().catch(() => null),
        getMyMatches().catch(() => []),
        getVisits({ page: 0, size: 5 }).catch(() => null),
      ]);
      setVolunteer(vol);
      setMatches(mat ?? []);
      setUpcomingVisits(vis?.content ?? []);
    } catch (err) {
      setError(getErrorMessage(err, '데이터를 불러오는데 실패했습니다'));
    } finally {
      setLoading(false);
    }
  };

  const activeMatches = matches.filter((m) => m.status === 'ACCEPTED');
  const matchedVolunteer = activeMatches.length > 0 ? activeMatches[0] : null;

  return (
    <PageLayout>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">이웃 돌봄</h1>
        <p className="text-sm text-gray-500 mt-1">이웃과 함께하는 따뜻한 돌봄 서비스</p>
      </div>

      {error && <AlertBanner message={error} variant="error" />}

      {/* Tab Navigation */}
      <div className="flex bg-gray-100 rounded-lg p-1 w-full sm:w-fit mb-6">
        {TAB_ITEMS.map((tab) => (
          <button
            key={tab.key}
            onClick={() => setActiveTab(tab.key)}
            className={`flex-1 sm:flex-none px-4 py-1.5 rounded-md text-sm font-medium transition-colors
              ${activeTab === tab.key
                ? 'bg-white text-gray-900 shadow-sm'
                : 'text-gray-500 hover:text-gray-700'
              }`}
            aria-pressed={activeTab === tab.key}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {loading ? (
        <LoadingSpinner />
      ) : (
        <div className="space-y-6">
          {/* 돌봄 요청 탭 */}
          {activeTab === 'request' && (
            <>
              {/* 돌봄 상태 */}
              <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
                <h2 className="text-lg font-semibold text-gray-900 mb-4">내 돌봄 상태</h2>
                {matchedVolunteer ? (
                  <div>
                    <div className="flex items-center gap-2 mb-3">
                      <div className="w-3 h-3 rounded-full bg-emerald-500" />
                      <span className="text-sm font-medium text-emerald-700">돌봄 매칭 활성화됨</span>
                    </div>
                    <div className="flex items-center gap-4 p-4 bg-gray-50 rounded-lg">
                      <div className="w-12 h-12 rounded-full bg-emerald-100 flex items-center justify-center shrink-0">
                        <span className="text-lg font-bold text-emerald-600">
                          {matchedVolunteer.volunteerName.charAt(0)}
                        </span>
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2">
                          <p className="text-base font-semibold text-gray-900">{matchedVolunteer.volunteerName}</p>
                          <TrustScoreBadge score={matchedVolunteer.trustScore} level={matchedVolunteer.trustLevel} />
                        </div>
                        <p className="text-sm text-gray-500 mt-0.5">{matchedVolunteer.distance}</p>
                        <p className="text-xs text-gray-400 mt-0.5">방문 {matchedVolunteer.visitCount}회 완료</p>
                      </div>
                      <Link
                        href="/care/schedule"
                        className="px-4 py-2 bg-emerald-600 text-white rounded-lg text-sm font-medium hover:bg-emerald-700 transition-colors whitespace-nowrap"
                      >
                        일정 보기
                      </Link>
                    </div>
                  </div>
                ) : (
                  <div className="text-center py-6">
                    <div className="flex items-center gap-2 justify-center mb-3">
                      <div className="w-3 h-3 rounded-full bg-gray-300" />
                      <span className="text-sm text-gray-500">돌봄 매칭 대기중</span>
                    </div>
                    <p className="text-sm text-gray-400 mb-4">아직 매칭된 자원봉사자가 없습니다</p>
                    <Link
                      href="/care/matches"
                      className="inline-flex px-4 py-2 bg-emerald-600 text-white rounded-lg text-sm font-medium hover:bg-emerald-700 transition-colors"
                    >
                      이웃 찾기
                    </Link>
                  </div>
                )}
              </div>

              {/* 다가오는 방문 일정 */}
              <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
                <div className="flex items-center justify-between mb-4">
                  <h2 className="text-lg font-semibold text-gray-900">다가오는 방문 일정</h2>
                  <Link href="/care/schedule" className="text-sm text-emerald-600 hover:underline">
                    전체 보기
                  </Link>
                </div>
                {upcomingVisits.length > 0 ? (
                  <div className="space-y-3">
                    {upcomingVisits.map((visit) => (
                      <div key={visit.id} className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
                        <div className={`w-1 h-12 rounded-full shrink-0 ${
                          visit.status === 'COMPLETED' ? 'bg-emerald-500' :
                          visit.status === 'UPCOMING' ? 'bg-blue-500' :
                          'bg-gray-300'
                        }`} />
                        <div className="flex-1 min-w-0">
                          <p className="text-sm font-medium text-gray-900">{visit.partnerName}</p>
                          <p className="text-xs text-gray-500">{formatDateTime(visit.scheduledAt)}</p>
                        </div>
                        <span className={`px-2.5 py-1 rounded-full text-xs font-medium ${
                          visit.status === 'COMPLETED' ? 'bg-emerald-100 text-emerald-700' :
                          visit.status === 'UPCOMING' ? 'bg-blue-100 text-blue-700' :
                          visit.status === 'CANCELLED' ? 'bg-gray-100 text-gray-500' :
                          'bg-yellow-100 text-yellow-700'
                        }`}>
                          {visit.status === 'UPCOMING' ? '예정' :
                           visit.status === 'COMPLETED' ? '완료' :
                           visit.status === 'CANCELLED' ? '취소' : '진행중'}
                        </span>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-sm text-gray-400 py-4 text-center">예정된 방문 일정이 없습니다</p>
                )}
              </div>
            </>
          )}

          {/* 자원봉사 탭 */}
          {activeTab === 'volunteer' && (
            <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">내 자원봉사 현황</h2>
              {volunteer ? (
                <div>
                  <div className="flex items-center gap-4 mb-6">
                    <div className="w-16 h-16 rounded-full bg-emerald-100 flex items-center justify-center shrink-0">
                      <span className="text-2xl font-bold text-emerald-600">
                        {volunteer.name.charAt(0)}
                      </span>
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2">
                        <p className="text-base font-semibold text-gray-900">{volunteer.name}</p>
                        <TrustScoreBadge score={volunteer.trustScore} level={volunteer.trustLevel} />
                      </div>
                      <p className="text-sm text-gray-500 mt-0.5">
                        상태: {volunteer.status === 'ACTIVE' ? '활동중' : '비활성'}
                      </p>
                      <p className="text-xs text-gray-400 mt-0.5">방문 {volunteer.visitCount}회 완료</p>
                    </div>
                  </div>

                  <dl className="space-y-2 mb-4">
                    <div className="flex justify-between py-2 border-b border-gray-100">
                      <dt className="text-sm text-gray-600">활동 가능 요일</dt>
                      <dd className="text-sm font-medium text-gray-900">
                        {volunteer.availableDays.map((d) => {
                          const labels: Record<string, string> = { MON: '월', TUE: '화', WED: '수', THU: '목', FRI: '금', SAT: '토', SUN: '일' };
                          return labels[d];
                        }).join(', ')}
                      </dd>
                    </div>
                    <div className="flex justify-between py-2 border-b border-gray-100">
                      <dt className="text-sm text-gray-600">활동 시간</dt>
                      <dd className="text-sm font-medium text-gray-900">{volunteer.availableStart} ~ {volunteer.availableEnd}</dd>
                    </div>
                    <div className="flex justify-between py-2 border-b border-gray-100">
                      <dt className="text-sm text-gray-600">활동 지역</dt>
                      <dd className="text-sm font-medium text-gray-900">{volunteer.address}</dd>
                    </div>
                    <div className="flex justify-between py-2">
                      <dt className="text-sm text-gray-600">활동 반경</dt>
                      <dd className="text-sm font-medium text-gray-900">{volunteer.radiusKm}km</dd>
                    </div>
                  </dl>

                  <Link
                    href="/care/volunteer"
                    className="inline-flex px-4 py-2 bg-emerald-600 text-white rounded-lg text-sm font-medium hover:bg-emerald-700 transition-colors"
                  >
                    정보 관리
                  </Link>
                </div>
              ) : (
                <div className="text-center py-8">
                  <div className="w-16 h-16 rounded-full bg-gray-100 flex items-center justify-center mx-auto mb-3">
                    <svg className="h-8 w-8 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5}
                        d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z" />
                    </svg>
                  </div>
                  <p className="text-sm text-gray-500 mb-1">아직 자원봉사자로 등록하지 않았습니다</p>
                  <p className="text-xs text-gray-400 mb-4">이웃의 안전을 함께 지켜주세요</p>
                  <Link
                    href="/care/volunteer"
                    className="inline-flex px-4 py-2 bg-emerald-600 text-white rounded-lg text-sm font-medium hover:bg-emerald-700 transition-colors"
                  >
                    자원봉사 등록
                  </Link>
                </div>
              )}
            </div>
          )}

          {/* 매칭 현황 탭 */}
          {activeTab === 'matches' && (
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <p className="text-sm text-gray-500">총 {matches.length}건의 매칭</p>
                <Link
                  href="/care/matches"
                  className="text-sm text-emerald-600 hover:underline"
                >
                  전체 관리
                </Link>
              </div>

              {matches.length > 0 ? (
                matches.map((match) => (
                  <div key={match.id} className="bg-white rounded-xl shadow-sm border border-gray-200 p-5 hover:shadow-md transition-shadow">
                    <div className="flex items-start gap-4">
                      <div className="w-12 h-12 rounded-full bg-emerald-100 flex items-center justify-center shrink-0">
                        <span className="text-lg font-bold text-emerald-600">
                          {(match.volunteerName || match.recipientName).charAt(0)}
                        </span>
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2 mb-1">
                          <h3 className="text-base font-semibold text-gray-900 truncate">
                            {match.volunteerName} / {match.recipientName}
                          </h3>
                          <TrustScoreBadge score={match.trustScore} level={match.trustLevel} />
                        </div>
                        <div className="flex flex-wrap gap-x-4 gap-y-1 text-xs text-gray-500 mb-2">
                          <span className="inline-flex items-center gap-1">
                            <svg className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                                d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                            </svg>
                            {match.distance}
                          </span>
                          <span>방문 {match.visitCount}회</span>
                          <span>매칭일: {formatDateTime(match.matchedAt)}</span>
                        </div>
                        <span className={`inline-flex px-2.5 py-1 rounded-full text-xs font-medium ${MATCH_STATUS_STYLES[match.status]}`}>
                          {MATCH_STATUS_LABELS[match.status]}
                        </span>
                      </div>
                    </div>
                  </div>
                ))
              ) : (
                <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 text-center">
                  <div className="w-16 h-16 rounded-full bg-gray-100 flex items-center justify-center mx-auto mb-3">
                    <svg className="h-8 w-8 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5}
                        d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z" />
                    </svg>
                  </div>
                  <p className="text-sm text-gray-500 mb-3">아직 매칭 내역이 없습니다</p>
                  <Link
                    href="/care/matches"
                    className="inline-flex px-4 py-2 bg-emerald-600 text-white rounded-lg text-sm font-medium hover:bg-emerald-700 transition-colors"
                  >
                    이웃 찾기
                  </Link>
                </div>
              )}
            </div>
          )}

          {/* 빠른 액션 */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
            <Link
              href="/care/volunteer"
              className="bg-white rounded-xl shadow-sm border border-gray-200 p-4 hover:shadow-md transition-shadow text-center"
            >
              <div className="w-10 h-10 bg-emerald-100 rounded-lg flex items-center justify-center mx-auto mb-2">
                <svg className="h-5 w-5 text-emerald-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" />
                </svg>
              </div>
              <span className="text-sm font-medium text-gray-700">봉사자 등록</span>
            </Link>
            <Link
              href="/care/matches"
              className="bg-white rounded-xl shadow-sm border border-gray-200 p-4 hover:shadow-md transition-shadow text-center"
            >
              <div className="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center mx-auto mb-2">
                <svg className="h-5 w-5 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                </svg>
              </div>
              <span className="text-sm font-medium text-gray-700">매칭 관리</span>
            </Link>
            <Link
              href="/care/schedule"
              className="bg-white rounded-xl shadow-sm border border-gray-200 p-4 hover:shadow-md transition-shadow text-center"
            >
              <div className="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center mx-auto mb-2">
                <svg className="h-5 w-5 text-purple-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
              </div>
              <span className="text-sm font-medium text-gray-700">방문 일정</span>
            </Link>
            <Link
              href="/care/report/new"
              className="bg-white rounded-xl shadow-sm border border-gray-200 p-4 hover:shadow-md transition-shadow text-center"
            >
              <div className="w-10 h-10 bg-orange-100 rounded-lg flex items-center justify-center mx-auto mb-2">
                <svg className="h-5 w-5 text-orange-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
              </div>
              <span className="text-sm font-medium text-gray-700">보고서 작성</span>
            </Link>
          </div>
        </div>
      )}
    </PageLayout>
  );
}
