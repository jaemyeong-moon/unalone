'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import PageLayout from '@/components/common/PageLayout';
import FormField from '@/components/common/FormField';
import AlertBanner from '@/components/common/AlertBanner';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import TrustScoreBadge from '@/components/common/TrustScoreBadge';
import DaySelector from '@/components/common/DaySelector';
import { isLoggedIn, getUser } from '@/lib/auth';
import { registerVolunteer, getMyVolunteerStatus, updateVolunteer, withdrawVolunteer } from '@/lib/care';
import { getErrorMessage } from '@/lib/api';
import { DayOfWeek, VolunteerResponse } from '@/types';

const RADIUS_OPTIONS = [
  { value: 1, label: '1km' },
  { value: 3, label: '3km' },
  { value: 5, label: '5km' },
  { value: 10, label: '10km' },
];

interface VolunteerForm {
  name: string;
  phone: string;
  introduction: string;
  availableDays: DayOfWeek[];
  availableStart: string;
  availableEnd: string;
  address: string;
  radiusKm: number;
  agreed: boolean;
}

const initialForm: VolunteerForm = {
  name: '',
  phone: '',
  introduction: '',
  availableDays: [],
  availableStart: '09:00',
  availableEnd: '18:00',
  address: '',
  radiusKm: 3,
  agreed: false,
};

export default function VolunteerPage() {
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState('');
  const [isError, setIsError] = useState(false);
  const [existing, setExisting] = useState<VolunteerResponse | null>(null);
  const [form, setForm] = useState<VolunteerForm>(initialForm);
  const [editMode, setEditMode] = useState(false);

  useEffect(() => {
    if (!isLoggedIn()) {
      router.push('/login');
      return;
    }
    const user = getUser();
    if (user) {
      setForm((prev) => ({ ...prev, name: user.name }));
    }
    loadExisting();
  }, [router]);

  const loadExisting = async () => {
    setLoading(true);
    try {
      const vol = await getMyVolunteerStatus();
      setExisting(vol);
      setForm({
        name: vol.name,
        phone: vol.phone,
        introduction: vol.introduction,
        availableDays: vol.availableDays,
        availableStart: vol.availableStart,
        availableEnd: vol.availableEnd,
        address: vol.address,
        radiusKm: vol.radiusKm,
        agreed: true,
      });
    } catch {
      // Not registered yet
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setMessage('');
    try {
      const data = {
        phone: form.phone,
        introduction: form.introduction || undefined,
        availableDays: form.availableDays,
        availableStart: form.availableStart,
        availableEnd: form.availableEnd,
        address: form.address,
        radiusKm: form.radiusKm,
      };
      if (existing && editMode) {
        await updateVolunteer(data);
        setMessage('자원봉사 정보가 수정되었습니다');
      } else {
        await registerVolunteer(data);
        setMessage('자원봉사 등록이 완료되었습니다');
      }
      setIsError(false);
      setTimeout(() => router.push('/care'), 1500);
    } catch (err) {
      setMessage(getErrorMessage(err, '처리 중 오류가 발생했습니다'));
      setIsError(true);
    } finally {
      setSubmitting(false);
    }
  };

  const handleWithdraw = async () => {
    if (!confirm('정말 자원봉사를 탈퇴하시겠습니까?')) return;
    try {
      await withdrawVolunteer();
      setMessage('자원봉사가 탈퇴되었습니다');
      setIsError(false);
      setTimeout(() => router.push('/care'), 1500);
    } catch (err) {
      setMessage(getErrorMessage(err, '탈퇴 처리 중 오류가 발생했습니다'));
      setIsError(true);
    }
  };

  if (loading) {
    return (
      <PageLayout maxWidth="max-w-2xl">
        <LoadingSpinner />
      </PageLayout>
    );
  }

  // Already registered - show info view
  if (existing && !editMode) {
    return (
      <PageLayout maxWidth="max-w-2xl">
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-gray-900">자원봉사 관리</h1>
          <p className="text-sm text-gray-500 mt-1">내 자원봉사 정보를 확인하고 관리합니다</p>
        </div>

        {message && <AlertBanner message={message} variant={isError ? 'error' : 'success'} />}

        <div className="space-y-6">
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <div className="flex items-center gap-4 mb-6">
              <div className="w-16 h-16 rounded-full bg-emerald-100 flex items-center justify-center">
                <span className="text-2xl font-bold text-emerald-600">{existing.name.charAt(0)}</span>
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <h2 className="text-xl font-semibold text-gray-900">{existing.name}</h2>
                  <TrustScoreBadge score={existing.trustScore} level={existing.trustLevel} />
                </div>
                <p className="text-sm text-gray-500">방문 {existing.visitCount}회 완료</p>
              </div>
            </div>

            <dl className="space-y-3">
              <div className="flex justify-between py-2 border-b border-gray-100">
                <dt className="text-sm text-gray-600">연락처</dt>
                <dd className="text-sm font-medium text-gray-900">{existing.phone}</dd>
              </div>
              <div className="flex justify-between py-2 border-b border-gray-100">
                <dt className="text-sm text-gray-600">활동 가능 요일</dt>
                <dd className="text-sm font-medium text-gray-900">
                  {existing.availableDays.map((d) => {
                    const labels: Record<string, string> = { MON: '월', TUE: '화', WED: '수', THU: '목', FRI: '금', SAT: '토', SUN: '일' };
                    return labels[d];
                  }).join(', ')}
                </dd>
              </div>
              <div className="flex justify-between py-2 border-b border-gray-100">
                <dt className="text-sm text-gray-600">활동 시간</dt>
                <dd className="text-sm font-medium text-gray-900">{existing.availableStart} ~ {existing.availableEnd}</dd>
              </div>
              <div className="flex justify-between py-2 border-b border-gray-100">
                <dt className="text-sm text-gray-600">활동 지역</dt>
                <dd className="text-sm font-medium text-gray-900">{existing.address}</dd>
              </div>
              <div className="flex justify-between py-2 border-b border-gray-100">
                <dt className="text-sm text-gray-600">활동 반경</dt>
                <dd className="text-sm font-medium text-gray-900">{existing.radiusKm}km</dd>
              </div>
              {existing.introduction && (
                <div className="py-2">
                  <dt className="text-sm text-gray-600 mb-1">자기소개</dt>
                  <dd className="text-sm text-gray-900">{existing.introduction}</dd>
                </div>
              )}
            </dl>
          </div>

          <div className="flex gap-3">
            <button
              onClick={() => setEditMode(true)}
              className="flex-1 py-3 bg-emerald-600 text-white rounded-xl font-semibold hover:bg-emerald-700 transition-colors"
            >
              정보 수정
            </button>
            <button
              onClick={handleWithdraw}
              className="px-6 py-3 border border-red-300 text-red-600 rounded-xl font-semibold hover:bg-red-50 transition-colors"
            >
              탈퇴
            </button>
          </div>
        </div>
      </PageLayout>
    );
  }

  // Registration / Edit form
  return (
    <PageLayout maxWidth="max-w-2xl">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">
          {editMode ? '자원봉사 정보 수정' : '자원봉사 등록'}
        </h1>
        <p className="text-sm text-gray-500 mt-1">이웃의 안전을 함께 지켜주세요</p>
      </div>

      {message && <AlertBanner message={message} variant={isError ? 'error' : 'success'} />}

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* 기본 정보 */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">기본 정보</h2>
          <div className="space-y-4">
            <FormField label="이름" type="text" value={form.name} readOnly
              hint="프로필에서 가져옵니다" />
            <FormField label="연락처" type="tel" value={form.phone}
              onChange={(e) => setForm({ ...form, phone: (e.target as HTMLInputElement).value })}
              placeholder="010-0000-0000" />
            <FormField as="textarea" label="자기소개" value={form.introduction}
              onChange={(e) => setForm({ ...form, introduction: (e.target as HTMLTextAreaElement).value })}
              rows={3} placeholder="간단한 자기소개를 작성해주세요" optional />
          </div>
        </div>

        {/* 활동 가능 정보 */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">활동 가능 시간</h2>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">가능한 요일</label>
              <DaySelector
                selected={form.availableDays}
                onChange={(days) => setForm({ ...form, availableDays: days })}
              />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <FormField label="시작 시간" type="time" value={form.availableStart}
                onChange={(e) => setForm({ ...form, availableStart: (e.target as HTMLInputElement).value })} />
              <FormField label="종료 시간" type="time" value={form.availableEnd}
                onChange={(e) => setForm({ ...form, availableEnd: (e.target as HTMLInputElement).value })} />
            </div>
          </div>
        </div>

        {/* 활동 지역 */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">활동 지역</h2>
          <div className="space-y-4">
            <FormField label="주소" type="text" value={form.address}
              onChange={(e) => setForm({ ...form, address: (e.target as HTMLInputElement).value })}
              placeholder="활동 가능한 기준 주소" />

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">활동 반경</label>
              <div className="flex gap-2">
                {RADIUS_OPTIONS.map((opt) => (
                  <button
                    key={opt.value}
                    type="button"
                    onClick={() => setForm({ ...form, radiusKm: opt.value })}
                    className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors
                      ${form.radiusKm === opt.value
                        ? 'bg-emerald-600 text-white'
                        : 'bg-white text-gray-600 border border-gray-300 hover:bg-gray-50'
                      }`}
                    aria-pressed={form.radiusKm === opt.value}
                  >
                    {opt.label}
                  </button>
                ))}
              </div>
            </div>
          </div>
        </div>

        {/* 동의 */}
        {!editMode && (
          <label className="flex items-start gap-3 cursor-pointer">
            <input
              type="checkbox"
              checked={form.agreed}
              onChange={(e) => setForm({ ...form, agreed: e.target.checked })}
              className="mt-0.5 h-5 w-5 rounded border-gray-300 text-emerald-600 focus:ring-emerald-500"
            />
            <span className="text-sm text-gray-700">
              이웃 돌봄 자원봉사 활동 가이드라인에 동의합니다.
              <button type="button" className="text-emerald-600 hover:underline ml-1">상세 보기</button>
            </span>
          </label>
        )}

        <div className="flex gap-3">
          {editMode && (
            <button
              type="button"
              onClick={() => setEditMode(false)}
              className="px-6 py-3 border border-gray-300 text-gray-700 rounded-xl font-semibold hover:bg-gray-50 transition-colors"
            >
              취소
            </button>
          )}
          <button
            type="submit"
            disabled={submitting || (!editMode && !form.agreed)}
            className="flex-1 py-3 bg-emerald-600 text-white rounded-xl font-semibold hover:bg-emerald-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            {submitting ? (editMode ? '수정 중...' : '등록 중...') : (editMode ? '정보 수정' : '자원봉사 등록')}
          </button>
        </div>
      </form>
    </PageLayout>
  );
}
