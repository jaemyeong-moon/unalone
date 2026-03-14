'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import PageLayout from '@/components/common/PageLayout';
import AlertBanner from '@/components/common/AlertBanner';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import FormField from '@/components/common/FormField';
import apiClient from '@/lib/api';
import { isLoggedIn } from '@/lib/auth';
import { getOAuthConnections, linkOAuthAccount, unlinkOAuthAccount } from '@/lib/oauth';
import { ApiResponse, ProfileResponse, ProfileRequest, OAuthProvider } from '@/types';
import { OAuthConnection } from '@/types/oauth';
import { formatDateTime } from '@/lib/utils';

const PROVIDER_CONFIG: Record<string, { label: string; icon: React.ReactNode; oauthUrl: string }> = {
  kakao: {
    label: '카카오',
    icon: (
      <svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path
          fillRule="evenodd"
          clipRule="evenodd"
          d="M9 0.6C4.029 0.6 0 3.726 0 7.554C0 9.918 1.557 12.006 3.933 13.212L2.934 16.77C2.862 17.022 3.15 17.226 3.372 17.082L7.596 14.37C8.058 14.424 8.526 14.454 9 14.454C13.971 14.454 18 11.328 18 7.554C18 3.726 13.971 0.6 9 0.6Z"
          fill="black"
        />
      </svg>
    ),
    oauthUrl: '/api/auth/oauth/kakao',
  },
  google: {
    label: '구글',
    icon: (
      <svg width="18" height="18" viewBox="0 0 18 18" xmlns="http://www.w3.org/2000/svg">
        <path d="M17.64 9.205c0-.639-.057-1.252-.164-1.841H9v3.481h4.844a4.14 4.14 0 01-1.796 2.716v2.259h2.908c1.702-1.567 2.684-3.875 2.684-6.615z" fill="#4285F4"/>
        <path d="M9 18c2.43 0 4.467-.806 5.956-2.18l-2.908-2.259c-.806.54-1.837.86-3.048.86-2.344 0-4.328-1.584-5.036-3.711H.957v2.332A8.997 8.997 0 009 18z" fill="#34A853"/>
        <path d="M3.964 10.71A5.41 5.41 0 013.682 9c0-.593.102-1.17.282-1.71V4.958H.957A8.996 8.996 0 000 9c0 1.452.348 2.827.957 4.042l3.007-2.332z" fill="#FBBC05"/>
        <path d="M9 3.58c1.321 0 2.508.454 3.44 1.345l2.582-2.58C13.463.891 11.426 0 9 0A8.997 8.997 0 00.957 4.958L3.964 6.29C4.672 4.163 6.656 3.58 9 3.58z" fill="#EA4335"/>
      </svg>
    ),
    oauthUrl: '/api/auth/oauth/google',
  },
};

const ALL_PROVIDERS: OAuthProvider[] = ['kakao', 'google'];

export default function ProfilePage() {
  const router = useRouter();
  const [profile, setProfile] = useState<ProfileResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState<ProfileRequest>({});
  const [message, setMessage] = useState('');
  const [isError, setIsError] = useState(false);

  // OAuth state
  const [oauthConnections, setOauthConnections] = useState<OAuthConnection[]>([]);
  const [oauthLoading, setOauthLoading] = useState(false);
  const [unlinkConfirm, setUnlinkConfirm] = useState<string | null>(null);
  const [unlinkingProvider, setUnlinkingProvider] = useState<string | null>(null);

  useEffect(() => {
    if (!isLoggedIn()) {
      router.push('/login');
      return;
    }
    fetchProfile();
    fetchOAuthConnections();
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

  const fetchOAuthConnections = async () => {
    setOauthLoading(true);
    try {
      const connections = await getOAuthConnections();
      setOauthConnections(connections);
    } catch {
      // OAuth connections may not be available
    } finally {
      setOauthLoading(false);
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

  const handleLinkProvider = (provider: string) => {
    const config = PROVIDER_CONFIG[provider];
    if (config) {
      window.location.href = config.oauthUrl;
    }
  };

  const handleUnlinkProvider = async (provider: string) => {
    setUnlinkingProvider(provider);
    try {
      await unlinkOAuthAccount(provider);
      setOauthConnections((prev) => prev.filter((c) => c.provider !== provider));
      showMessage(`${PROVIDER_CONFIG[provider]?.label ?? provider} 계정 연결이 해제되었습니다`);
    } catch {
      showMessage('계정 연결 해제에 실패했습니다', true);
    } finally {
      setUnlinkingProvider(null);
      setUnlinkConfirm(null);
    }
  };

  const connectedProviders = oauthConnections.map((c) => c.provider);

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
        <div className="space-y-6">
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

          {/* 연결된 소셜 계정 */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <h2 className="text-sm font-medium text-gray-500 mb-4">연결된 소셜 계정</h2>

            {oauthLoading ? (
              <LoadingSpinner className="py-4" />
            ) : (
              <div className="space-y-3">
                {ALL_PROVIDERS.map((provider) => {
                  const config = PROVIDER_CONFIG[provider];
                  const connection = oauthConnections.find((c) => c.provider === provider);
                  const isConnected = !!connection;
                  const isUnlinking = unlinkingProvider === provider;

                  return (
                    <div
                      key={provider}
                      className="flex items-center gap-3 p-3 rounded-lg border border-gray-100"
                    >
                      {/* Provider icon */}
                      <div className="w-10 h-10 rounded-full bg-gray-50 flex items-center justify-center shrink-0">
                        {config.icon}
                      </div>

                      {/* Provider info */}
                      <div className="flex-1 min-w-0">
                        <p className="text-sm font-medium text-gray-900">{config.label}</p>
                        {isConnected ? (
                          <div>
                            {connection.email && (
                              <p className="text-xs text-gray-500 truncate">{connection.email}</p>
                            )}
                            <p className="text-xs text-gray-400">
                              {formatDateTime(connection.connectedAt)} 연결
                            </p>
                          </div>
                        ) : (
                          <p className="text-xs text-gray-400">연결되지 않음</p>
                        )}
                      </div>

                      {/* Action button */}
                      {isConnected ? (
                        unlinkConfirm === provider ? (
                          <div className="flex gap-2 shrink-0">
                            <button
                              onClick={() => handleUnlinkProvider(provider)}
                              disabled={isUnlinking}
                              className="px-3 py-1.5 text-xs font-medium text-red-600 border border-red-300 rounded-lg hover:bg-red-50 disabled:opacity-50 transition-colors"
                            >
                              {isUnlinking ? '해제 중...' : '확인'}
                            </button>
                            <button
                              onClick={() => setUnlinkConfirm(null)}
                              disabled={isUnlinking}
                              className="px-3 py-1.5 text-xs font-medium text-gray-600 border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 transition-colors"
                            >
                              취소
                            </button>
                          </div>
                        ) : (
                          <button
                            onClick={() => setUnlinkConfirm(provider)}
                            className="px-3 py-1.5 text-xs font-medium text-red-600 border border-red-300 rounded-lg hover:bg-red-50 transition-colors shrink-0"
                          >
                            연결 해제
                          </button>
                        )
                      ) : (
                        <button
                          onClick={() => handleLinkProvider(provider)}
                          className="px-3 py-1.5 text-xs font-medium text-emerald-600 border border-emerald-300 rounded-lg hover:bg-emerald-50 transition-colors shrink-0"
                        >
                          연결
                        </button>
                      )}
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        </div>
      ) : (
        <div className="text-center py-12 text-gray-500">프로필을 불러올 수 없습니다</div>
      )}
    </PageLayout>
  );
}
