'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import apiClient, { getErrorMessage } from '@/lib/api';
import { saveAuth } from '@/lib/auth';
import { ApiResponse, LoginResponse } from '@/types';
import AlertBanner from '@/components/common/AlertBanner';
import FormField from '@/components/common/FormField';
import SocialLoginButton from '@/components/common/SocialLoginButton';

export default function SignupPage() {
  const router = useRouter();
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [phone, setPhone] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const res = await apiClient.post<ApiResponse<LoginResponse>>('/api/auth/signup', {
        name,
        email,
        password,
        phone: phone || undefined,
      });
      saveAuth(res.data.data);
      router.push('/');
    } catch (err) {
      setError(getErrorMessage(err, '회원가입에 실패했습니다'));
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
          <h1 className="text-2xl font-bold text-gray-900">회원가입</h1>
          <p className="text-sm text-gray-500 mt-1">함께하는 안심 네트워크에 참여하세요</p>
        </div>

        {error && <AlertBanner message={error} variant="error" />}

        {/* 소셜 가입 버튼 */}
        <div className="space-y-3">
          <SocialLoginButton provider="kakao" label="카카오로 시작하기" />
          <SocialLoginButton provider="google" label="구글로 시작하기" />
        </div>

        {/* 구분선 */}
        <div className="my-6 flex items-center gap-3">
          <div className="flex-1 h-px bg-gray-200" />
          <span className="text-sm text-gray-400 whitespace-nowrap">또는 이메일로 가입</span>
          <div className="flex-1 h-px bg-gray-200" />
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <FormField
            label="이름"
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="홍길동"
            required
          />
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
            minLength={8}
          />
          <FormField
            label="전화번호"
            type="tel"
            value={phone}
            onChange={(e) => setPhone(e.target.value)}
            placeholder="010-1234-5678"
            optional
          />
          <button
            type="submit"
            disabled={loading}
            className="w-full py-2.5 px-4 bg-emerald-600 text-white rounded-lg hover:bg-emerald-700 disabled:opacity-50 transition-colors font-medium"
          >
            {loading ? '가입 중...' : '회원가입'}
          </button>
        </form>

        <p className="mt-4 text-center text-sm text-gray-600">
          이미 계정이 있으신가요?{' '}
          <Link href="/login" className="text-emerald-600 hover:underline font-medium">로그인</Link>
        </p>
      </div>
    </div>
  );
}
