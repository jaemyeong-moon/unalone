'use client';

import { useState, useEffect, Suspense } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import Link from 'next/link';
import apiClient, { getErrorMessage } from '@/lib/api';
import { saveAuth } from '@/lib/auth';
import { ApiResponse, LoginResponse } from '@/types';
import AlertBanner from '@/components/common/AlertBanner';
import FormField from '@/components/common/FormField';
import SocialLoginButton from '@/components/common/SocialLoginButton';

function LoginContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const errorParam = searchParams.get('error');
    if (errorParam) {
      setError(decodeURIComponent(errorParam));
    }
  }, [searchParams]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const res = await apiClient.post<ApiResponse<LoginResponse>>('/api/auth/login', { email, password });
      saveAuth(res.data.data);
      router.push('/');
    } catch (err) {
      setError(getErrorMessage(err, '로그인에 실패했습니다'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="max-w-md w-full bg-white rounded-xl shadow-sm border border-gray-200 p-8">
        <div className="text-center mb-6">
          <Link href="/" className="inline-flex items-center gap-2 text-emerald-700 mb-4">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
            </svg>
            <span className="text-xl font-bold">Unalone</span>
          </Link>
          <h1 className="text-2xl font-bold text-gray-900">로그인</h1>
          <p className="text-sm text-gray-500 mt-1">안부를 전하러 오셨군요</p>
        </div>

        {error && <AlertBanner message={error} variant="error" />}

        <form onSubmit={handleSubmit} className="space-y-4">
          <FormField
            label="이메일"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="email@example.com"
            required
          />
          <FormField
            label="비밀번호"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="8자 이상"
            required
          />
          <button
            type="submit"
            disabled={loading}
            className="w-full py-2.5 px-4 bg-emerald-600 text-white rounded-lg hover:bg-emerald-700 disabled:opacity-50 transition-colors font-medium"
          >
            {loading ? '로그인 중...' : '로그인'}
          </button>
        </form>

        {/* 구분선 */}
        <div className="my-6 flex items-center gap-3">
          <div className="flex-1 h-px bg-gray-200" />
          <span className="text-sm text-gray-400 whitespace-nowrap">또는 소셜 계정으로 로그인</span>
          <div className="flex-1 h-px bg-gray-200" />
        </div>

        {/* 소셜 로그인 버튼 */}
        <div className="space-y-3">
          <SocialLoginButton provider="kakao" />
          <SocialLoginButton provider="google" />
        </div>

        <p className="mt-4 text-center text-sm text-gray-600">
          계정이 없으신가요?{' '}
          <Link href="/signup" className="text-emerald-600 hover:underline font-medium">회원가입</Link>
        </p>
      </div>
    </div>
  );
}

export default function LoginPage() {
  return (
    <Suspense>
      <LoginContent />
    </Suspense>
  );
}
