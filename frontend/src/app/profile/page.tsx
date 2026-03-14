'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import PageLayout from '@/components/common/PageLayout';
import AlertBanner from '@/components/common/AlertBanner';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import FormField from '@/components/common/FormField';
import apiClient from '@/lib/api';
import { isLoggedIn } from '@/lib/auth';
import { ApiResponse, ProfileResponse, ProfileRequest } from '@/types';

export default function ProfilePage() {
  const router = useRouter();
  const [profile, setProfile] = useState<ProfileResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState<ProfileRequest>({});
  const [message, setMessage] = useState('');
  const [isError, setIsError] = useState(false);

  useEffect(() => {
    if (!isLoggedIn()) {
      router.push('/login');
      return;
    }
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    try {
      const res = await apiClient.get<ApiResponse<ProfileResponse>>('/api/profile');
      const data = res.data.data;
      setProfile(data);
      setForm({
        checkIntervalHours: data.checkIntervalHours,
        activeHoursStart: data.activeHoursStart ?? '',
        activeHoursEnd: data.activeHoursEnd ?? '',
        address: data.address ?? '',
        emergencyNote: data.emergencyNote ?? '',
      });
    } catch {
      console.error('Failed to fetch profile');
    } finally {
      setLoading(false);
    }
  };

  const showMessage = (text: string, error = false) => {
    setMessage(text);
    setIsError(error);
    setTimeout(() => setMessage(''), 3000);
  };

  const handleSave = async () => {
    setSaving(true);
    try {
      const res = await apiClient.put<ApiResponse<ProfileResponse>>('/api/profile', form);
      setProfile(res.data.data);
      setEditing(false);
      showMessage('프로필이 저장되었습니다');
    } catch {
      showMessage('저장에 실패했습니다', true);
    } finally {
      setSaving(false);
    }
  };

  const handleCancelEdit = () => {
    if (!profile) return;
    setEditing(false);
    setForm({
      checkIntervalHours: profile.checkIntervalHours,
      activeHoursStart: profile.activeHoursStart ?? '',
      activeHoursEnd: profile.activeHoursEnd ?? '',
      address: profile.address ?? '',
      emergencyNote: profile.emergencyNote ?? '',
    });
  };

  return (
    <PageLayout maxWidth="max-w-2xl">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">프로필 설정</h1>
        <p className="text-sm text-gray-500 mt-1">체크인 주기와 생활 패턴을 설정하세요</p>
      </div>

      {message && <AlertBanner message={message} variant={isError ? 'error' : 'success'} />}

      {loading ? (
        <LoadingSpinner />
      ) : profile ? (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 divide-y divide-gray-100">
          {/* 기본 정보 */}
          <div className="p-6">
            <h2 className="text-sm font-medium text-gray-500 mb-3">기본 정보</h2>
            <dl className="space-y-3">
              {[
                { label: '이름', value: profile.userName },
                { label: '이메일', value: profile.email },
                { label: '전화번호', value: profile.phone || '-' },
              ].map(({ label, value }) => (
                <div key={label} className="flex justify-between">
                  <dt className="text-sm text-gray-600">{label}</dt>
                  <dd className="text-sm font-medium text-gray-900">{value}</dd>
                </div>
              ))}
            </dl>
          </div>

          {/* 체크인 설정 */}
          <div className="p-6">
            <div className="flex items-center justify-between mb-3">
              <h2 className="text-sm font-medium text-gray-500">체크인 설정</h2>
              {!editing && (
                <button
                  onClick={() => setEditing(true)}
                  className="text-sm text-emerald-600 hover:text-emerald-700 font-medium"
                >
                  수정
                </button>
              )}
            </div>

            {editing ? (
              <div className="space-y-4">
                <FormField
                  label="체크인 주기 (시간)"
                  type="number"
                  min={1}
                  max={72}
                  value={form.checkIntervalHours ?? ''}
                  onChange={(e) => setForm({ ...form, checkIntervalHours: Number(e.target.value) })}
                  hint="1~72시간 사이로 설정 (미응답 시 알림 발생)"
                />
                <div className="grid grid-cols-2 gap-4">
                  <FormField
                    label="활동 시작 시간"
                    type="time"
                    value={form.activeHoursStart ?? ''}
                    onChange={(e) => setForm({ ...form, activeHoursStart: e.target.value })}
                  />
                  <FormField
                    label="활동 종료 시간"
                    type="time"
                    value={form.activeHoursEnd ?? ''}
                    onChange={(e) => setForm({ ...form, activeHoursEnd: e.target.value })}
                  />
                </div>
                <FormField
                  label="주소"
                  type="text"
                  value={form.address ?? ''}
                  onChange={(e) => setForm({ ...form, address: e.target.value })}
                  placeholder="거주지 주소"
                />
                <FormField
                  as="textarea"
                  label="긴급 메모"
                  value={form.emergencyNote ?? ''}
                  onChange={(e) => setForm({ ...form, emergencyNote: e.target.value })}
                  rows={3}
                  placeholder="긴급 상황 시 참고할 메모 (지병, 복용 약물 등)"
                />
                <div className="flex gap-3">
                  <button
                    onClick={handleSave}
                    disabled={saving}
                    className="px-4 py-2 bg-emerald-600 text-white rounded-lg hover:bg-emerald-700 disabled:opacity-50 transition-colors text-sm font-medium"
                  >
                    {saving ? '저장 중...' : '저장'}
                  </button>
                  <button
                    onClick={handleCancelEdit}
                    className="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors text-sm"
                  >
                    취소
                  </button>
                </div>
              </div>
            ) : (
              <dl className="space-y-3">
                {[
                  {
                    label: '체크인 주기',
                    value: profile.checkIntervalHours ? `${profile.checkIntervalHours}시간` : '미설정',
                  },
                  {
                    label: '활동 시간',
                    value:
                      profile.activeHoursStart && profile.activeHoursEnd
                        ? `${profile.activeHoursStart} ~ ${profile.activeHoursEnd}`
                        : '미설정',
                  },
                  { label: '주소', value: profile.address || '미설정' },
                  { label: '긴급 메모', value: profile.emergencyNote || '미설정' },
                ].map(({ label, value }) => (
                  <div key={label} className="flex justify-between">
                    <dt className="text-sm text-gray-600">{label}</dt>
                    <dd className="text-sm font-medium text-gray-900">{value}</dd>
                  </div>
                ))}
              </dl>
            )}
          </div>
        </div>
      ) : (
        <div className="text-center py-12 text-gray-500">프로필을 불러올 수 없습니다</div>
      )}
    </PageLayout>
  );
}
