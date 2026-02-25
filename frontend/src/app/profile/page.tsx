'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Header from '@/components/common/Header';
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
      setProfile(res.data.data);
      setForm({
        checkIntervalHours: res.data.data.checkIntervalHours,
        activeHoursStart: res.data.data.activeHoursStart || '',
        activeHoursEnd: res.data.data.activeHoursEnd || '',
        address: res.data.data.address || '',
        emergencyNote: res.data.data.emergencyNote || '',
      });
    } catch {
      console.error('Failed to fetch profile');
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async () => {
    setSaving(true);
    setMessage('');
    try {
      const res = await apiClient.put<ApiResponse<ProfileResponse>>('/api/profile', form);
      setProfile(res.data.data);
      setEditing(false);
      setMessage('프로필이 저장되었습니다');
      setTimeout(() => setMessage(''), 3000);
    } catch {
      setMessage('저장에 실패했습니다');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-gray-900">프로필 설정</h1>
          <p className="text-sm text-gray-500 mt-1">체크인 주기와 생활 패턴을 설정하세요</p>
        </div>

        {message && (
          <div className={`mb-4 p-3 rounded-lg text-sm ${
            message.includes('실패') ? 'bg-red-50 text-red-600' : 'bg-emerald-50 text-emerald-600'
          }`}>
            {message}
          </div>
        )}

        {loading ? (
          <div className="flex justify-center py-12">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-emerald-600" />
          </div>
        ) : profile ? (
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 divide-y divide-gray-100">
            {/* 기본 정보 */}
            <div className="p-6">
              <h2 className="text-sm font-medium text-gray-500 mb-3">기본 정보</h2>
              <div className="space-y-3">
                <div className="flex justify-between">
                  <span className="text-sm text-gray-600">이름</span>
                  <span className="text-sm font-medium text-gray-900">{profile.userName}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-gray-600">이메일</span>
                  <span className="text-sm font-medium text-gray-900">{profile.email}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-gray-600">전화번호</span>
                  <span className="text-sm font-medium text-gray-900">{profile.phone || '-'}</span>
                </div>
              </div>
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
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      체크인 주기 (시간)
                    </label>
                    <input
                      type="number"
                      min={1}
                      max={72}
                      value={form.checkIntervalHours || ''}
                      onChange={(e) => setForm({ ...form, checkIntervalHours: Number(e.target.value) })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                    />
                    <p className="text-xs text-gray-400 mt-1">1~72시간 사이로 설정 (미응답 시 알림 발생)</p>
                  </div>
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">활동 시작 시간</label>
                      <input
                        type="time"
                        value={form.activeHoursStart || ''}
                        onChange={(e) => setForm({ ...form, activeHoursStart: e.target.value })}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">활동 종료 시간</label>
                      <input
                        type="time"
                        value={form.activeHoursEnd || ''}
                        onChange={(e) => setForm({ ...form, activeHoursEnd: e.target.value })}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                      />
                    </div>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">주소</label>
                    <input
                      type="text"
                      value={form.address || ''}
                      onChange={(e) => setForm({ ...form, address: e.target.value })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                      placeholder="거주지 주소"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">긴급 메모</label>
                    <textarea
                      value={form.emergencyNote || ''}
                      onChange={(e) => setForm({ ...form, emergencyNote: e.target.value })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                      rows={3}
                      placeholder="긴급 상황 시 참고할 메모 (지병, 복용 약물 등)"
                    />
                  </div>
                  <div className="flex gap-3">
                    <button
                      onClick={handleSave}
                      disabled={saving}
                      className="px-4 py-2 bg-emerald-600 text-white rounded-lg hover:bg-emerald-700 disabled:opacity-50 transition-colors text-sm font-medium"
                    >
                      {saving ? '저장 중...' : '저장'}
                    </button>
                    <button
                      onClick={() => {
                        setEditing(false);
                        setForm({
                          checkIntervalHours: profile.checkIntervalHours,
                          activeHoursStart: profile.activeHoursStart || '',
                          activeHoursEnd: profile.activeHoursEnd || '',
                          address: profile.address || '',
                          emergencyNote: profile.emergencyNote || '',
                        });
                      }}
                      className="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors text-sm"
                    >
                      취소
                    </button>
                  </div>
                </div>
              ) : (
                <div className="space-y-3">
                  <div className="flex justify-between">
                    <span className="text-sm text-gray-600">체크인 주기</span>
                    <span className="text-sm font-medium text-gray-900">
                      {profile.checkIntervalHours ? `${profile.checkIntervalHours}시간` : '미설정'}
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-sm text-gray-600">활동 시간</span>
                    <span className="text-sm font-medium text-gray-900">
                      {profile.activeHoursStart && profile.activeHoursEnd
                        ? `${profile.activeHoursStart} ~ ${profile.activeHoursEnd}`
                        : '미설정'}
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-sm text-gray-600">주소</span>
                    <span className="text-sm font-medium text-gray-900">{profile.address || '미설정'}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-sm text-gray-600">긴급 메모</span>
                    <span className="text-sm font-medium text-gray-900">{profile.emergencyNote || '미설정'}</span>
                  </div>
                </div>
              )}
            </div>
          </div>
        ) : (
          <div className="text-center py-12 text-gray-500">프로필을 불러올 수 없습니다</div>
        )}
      </main>
    </div>
  );
}
