'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Header from '@/components/common/Header';
import apiClient from '@/lib/api';
import { isLoggedIn } from '@/lib/auth';
import { ApiResponse, GuardianResponse, GuardianRequest } from '@/types';

export default function GuardiansPage() {
  const router = useRouter();
  const [guardians, setGuardians] = useState<GuardianResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState<GuardianRequest>({ name: '', phone: '', relationship: '' });
  const [error, setError] = useState('');

  useEffect(() => {
    if (!isLoggedIn()) {
      router.push('/login');
      return;
    }
    fetchGuardians();
  }, []);

  const fetchGuardians = async () => {
    try {
      const res = await apiClient.get<ApiResponse<GuardianResponse[]>>('/api/guardians');
      setGuardians(res.data.data);
    } catch {
      console.error('Failed to fetch guardians');
    } finally {
      setLoading(false);
    }
  };

  const handleAdd = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      await apiClient.post<ApiResponse<GuardianResponse>>('/api/guardians', {
        ...form,
        relationship: form.relationship || undefined,
      });
      setForm({ name: '', phone: '', relationship: '' });
      setShowForm(false);
      fetchGuardians();
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { message?: string } } };
      setError(axiosErr.response?.data?.message || '보호자 등록에 실패했습니다');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (guardianId: number) => {
    if (!confirm('이 보호자를 삭제하시겠습니까?')) return;
    try {
      await apiClient.delete(`/api/guardians/${guardianId}`);
      fetchGuardians();
    } catch {
      alert('삭제에 실패했습니다');
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">보호자 관리</h1>
            <p className="text-sm text-gray-500 mt-1">미응답 시 알림을 받을 보호자를 등록하세요 (최대 5명)</p>
          </div>
          {guardians.length < 5 && (
            <button
              onClick={() => setShowForm(!showForm)}
              className="px-4 py-2 bg-emerald-600 text-white rounded-lg hover:bg-emerald-700 transition-colors text-sm font-medium"
            >
              {showForm ? '취소' : '+ 추가'}
            </button>
          )}
        </div>

        {error && (
          <div className="mb-4 p-3 bg-red-50 text-red-600 rounded-lg text-sm">{error}</div>
        )}

        {/* 등록 폼 */}
        {showForm && (
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 mb-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">보호자 등록</h2>
            <form onSubmit={handleAdd} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">이름</label>
                <input
                  type="text"
                  value={form.name}
                  onChange={(e) => setForm({ ...form, name: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                  placeholder="보호자 이름"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">전화번호</label>
                <input
                  type="tel"
                  value={form.phone}
                  onChange={(e) => setForm({ ...form, phone: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                  placeholder="010-1234-5678"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  관계 <span className="text-gray-400">(선택)</span>
                </label>
                <input
                  type="text"
                  value={form.relationship || ''}
                  onChange={(e) => setForm({ ...form, relationship: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                  placeholder="예: 자녀, 형제, 이웃"
                />
              </div>
              <button
                type="submit"
                disabled={saving}
                className="w-full py-2.5 bg-emerald-600 text-white rounded-lg hover:bg-emerald-700 disabled:opacity-50 transition-colors font-medium text-sm"
              >
                {saving ? '등록 중...' : '보호자 등록'}
              </button>
            </form>
          </div>
        )}

        {/* 보호자 목록 */}
        {loading ? (
          <div className="flex justify-center py-12">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-emerald-600" />
          </div>
        ) : guardians.length === 0 ? (
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-12 text-center">
            <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
              </svg>
            </div>
            <p className="text-gray-500 mb-2">등록된 보호자가 없습니다</p>
            <p className="text-sm text-gray-400">보호자를 등록하면 미응답 시 알림을 받을 수 있습니다</p>
          </div>
        ) : (
          <div className="space-y-3">
            {guardians.map((guardian) => (
              <div
                key={guardian.id}
                className="bg-white rounded-xl shadow-sm border border-gray-200 p-5 flex items-center justify-between"
              >
                <div className="flex items-center gap-4">
                  <div className="w-10 h-10 bg-orange-100 rounded-full flex items-center justify-center">
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-orange-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                    </svg>
                  </div>
                  <div>
                    <p className="font-medium text-gray-900">{guardian.name}</p>
                    <div className="flex items-center gap-2 text-sm text-gray-500">
                      <span>{guardian.phone}</span>
                      {guardian.relationship && (
                        <>
                          <span className="text-gray-300">|</span>
                          <span>{guardian.relationship}</span>
                        </>
                      )}
                    </div>
                  </div>
                </div>
                <button
                  onClick={() => handleDelete(guardian.id)}
                  className="p-2 text-gray-400 hover:text-red-500 transition-colors"
                  title="삭제"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                  </svg>
                </button>
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
