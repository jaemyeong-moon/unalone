'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import PageLayout from '@/components/common/PageLayout';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import AlertBanner from '@/components/common/AlertBanner';
import TrustScoreBadge from '@/components/common/TrustScoreBadge';
import { isLoggedIn } from '@/lib/auth';
import { getMyMatches, acceptMatch, completeMatch, cancelMatch } from '@/lib/care';
import { getErrorMessage } from '@/lib/api';
import { formatDateTime } from '@/lib/utils';
import { CareMatchResponse, CareMatchStatus } from '@/types';

const STATUS_STYLES: Record<CareMatchStatus, string> = {
  PENDING: 'bg-yellow-100 text-yellow-700',
  ACCEPTED: 'bg-emerald-100 text-emerald-700',
  COMPLETED: 'bg-gray-100 text-gray-600',
  CANCELLED: 'bg-red-100 text-red-600',
  REJECTED: 'bg-gray-100 text-gray-500',
};

const STATUS_LABELS: Record<CareMatchStatus, string> = {
  PENDING: '대기중',
  ACCEPTED: '매칭중',
  COMPLETED: '완료',
  CANCELLED: '취소',
  REJECTED: '거절',
};

export default function MatchesPage() {
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [matches, setMatches] = useState<CareMatchResponse[]>([]);
  const [tab, setTab] = useState<'received' | 'provided'>('received');
  const [actionLoading, setActionLoading] = useState<number | null>(null);

  useEffect(() => {
    if (!isLoggedIn()) {
      router.push('/login');
      return;
    }
    fetchMatches();
  }, [router]);

  const fetchMatches = async () => {
    setLoading(true);
    try {
      const data = await getMyMatches();
      setMatches(data ?? []);
    } catch (err) {
      setError(getErrorMessage(err, '매칭 목록을 불러오는데 실패했습니다'));
    } finally {
      setLoading(false);
    }
  };

  const handleAccept = async (id: number) => {
    setActionLoading(id);
    try {
      await acceptMatch(id);
      setSuccess('매칭을 수락했습니다');
      fetchMatches();
    } catch (err) {
      setError(getErrorMessage(err, '매칭 수락에 실패했습니다'));
    } finally {
      setActionLoading(null);
    }
  };

  const handleComplete = async (id: number) => {
    setActionLoading(id);
    try {
      await completeMatch(id);
      setSuccess('매칭이 완료 처리되었습니다');
      fetchMatches();
    } catch (err) {
      setError(getErrorMessage(err, '완료 처리에 실패했습니다'));
    } finally {
      setActionLoading(null);
    }
  };

  const handleCancel = async (id: number) => {
    if (!confirm('정말 이 매칭을 취소하시겠습니까?')) return;
    setActionLoading(id);
    try {
      await cancelMatch(id);
      setSuccess('매칭이 취소되었습니다');
      fetchMatches();
    } catch (err) {
      setError(getErrorMessage(err, '매칭 취소에 실패했습니다'));
    } finally {
      setActionLoading(null);
    }
  };

  return (
    <PageLayout>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">돌봄 매칭</h1>
        <p className="text-sm text-gray-500 mt-1">내 돌봄 매칭을 관리합니다</p>
      </div>

      {error && <AlertBanner message={error} variant="error" />}
      {success && <AlertBanner message={success} variant="success" />}

      {/* Tab */}
      <div className="flex bg-gray-100 rounded-lg p-1 w-fit mb-6">
        <button
          onClick={() => setTab('received')}
          className={`px-4 py-1.5 rounded-md text-sm font-medium transition-colors
            ${tab === 'received' ? 'bg-white text-gray-900 shadow-sm' : 'text-gray-500 hover:text-gray-700'}`}
          aria-pressed={tab === 'received'}
        >
          받는 돌봄
        </button>
        <button
          onClick={() => setTab('provided')}
          className={`px-4 py-1.5 rounded-md text-sm font-medium transition-colors
            ${tab === 'provided' ? 'bg-white text-gray-900 shadow-sm' : 'text-gray-500 hover:text-gray-700'}`}
          aria-pressed={tab === 'provided'}
        >
          제공하는 돌봄
        </button>
      </div>

      {loading ? (
        <LoadingSpinner />
      ) : matches.length === 0 ? (
        <div className="text-center py-12">
          <div className="w-16 h-16 rounded-full bg-gray-100 flex items-center justify-center mx-auto mb-3">
            <svg className="h-8 w-8 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5}
                d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z" />
            </svg>
          </div>
          <p className="text-sm text-gray-500 mb-3">아직 매칭 내역이 없습니다</p>
          <Link
            href="/care"
            className="inline-flex px-4 py-2 bg-emerald-600 text-white rounded-lg text-sm font-medium hover:bg-emerald-700 transition-colors"
          >
            돌봄 메인으로
          </Link>
        </div>
      ) : (
        <div className="space-y-4">
          {matches.map((match) => {
            const partnerName = tab === 'received' ? match.volunteerName : match.recipientName;
            return (
              <div key={match.id} className="bg-white rounded-xl shadow-sm border border-gray-200 p-5 hover:shadow-md transition-shadow">
                <div className="flex items-start gap-4">
                  <div className="w-12 h-12 rounded-full bg-emerald-100 flex items-center justify-center shrink-0">
                    <span className="text-lg font-bold text-emerald-600">{partnerName.charAt(0)}</span>
                  </div>

                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-1">
                      <h3 className="text-base font-semibold text-gray-900 truncate">{partnerName}</h3>
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

                    <span className={`inline-flex px-2.5 py-1 rounded-full text-xs font-medium ${STATUS_STYLES[match.status]}`}>
                      {STATUS_LABELS[match.status]}
                    </span>
                  </div>

                  <div className="flex flex-col gap-2 shrink-0">
                    {match.status === 'PENDING' && tab === 'received' && (
                      <button
                        onClick={() => handleAccept(match.id)}
                        disabled={actionLoading === match.id}
                        className="px-4 py-2 bg-emerald-600 text-white rounded-lg text-sm font-medium hover:bg-emerald-700 disabled:opacity-50 transition-colors whitespace-nowrap"
                      >
                        {actionLoading === match.id ? '처리중...' : '수락'}
                      </button>
                    )}
                    {match.status === 'ACCEPTED' && (
                      <>
                        <button
                          onClick={() => handleComplete(match.id)}
                          disabled={actionLoading === match.id}
                          className="px-4 py-2 bg-emerald-600 text-white rounded-lg text-sm font-medium hover:bg-emerald-700 disabled:opacity-50 transition-colors whitespace-nowrap"
                        >
                          완료
                        </button>
                        <button
                          onClick={() => handleCancel(match.id)}
                          disabled={actionLoading === match.id}
                          className="px-4 py-2 border border-red-300 text-red-600 rounded-lg text-sm font-medium hover:bg-red-50 disabled:opacity-50 transition-colors whitespace-nowrap"
                        >
                          취소
                        </button>
                      </>
                    )}
                    {match.status === 'PENDING' && tab === 'provided' && (
                      <button
                        onClick={() => handleCancel(match.id)}
                        disabled={actionLoading === match.id}
                        className="px-4 py-2 border border-gray-300 text-gray-600 rounded-lg text-sm font-medium hover:bg-gray-50 disabled:opacity-50 transition-colors whitespace-nowrap"
                      >
                        취소
                      </button>
                    )}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </PageLayout>
  );
}
