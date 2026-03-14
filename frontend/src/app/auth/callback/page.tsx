'use client';

import { useEffect } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { Suspense } from 'react';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import { saveAuth } from '@/lib/auth';
import apiClient from '@/lib/api';
import { ApiResponse, LoginResponse } from '@/types';

function CallbackContent() {
  const router = useRouter();
  const searchParams = useSearchParams();

  useEffect(() => {
    const code = searchParams.get('code');
    const provider = searchParams.get('provider');
    const error = searchParams.get('error');

    if (error) {
      router.replace(`/login?error=${encodeURIComponent('소셜 로그인에 실패했습니다')}`);
      return;
    }

    if (code && provider) {
      apiClient
        .post<ApiResponse<LoginResponse>>(`/api/auth/oauth/${provider}`, { code })
        .then((res) => {
          saveAuth(res.data.data);
          router.replace('/');
        })
        .catch(() => {
          router.replace(`/login?error=${encodeURIComponent('소셜 로그인 처리 중 오류가 발생했습니다')}`);
        });
    }
  }, [searchParams, router]);

  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gray-50">
      <LoadingSpinner className="mb-4" />
      <p className="text-lg font-medium text-gray-900">로그인 중...</p>
      <p className="text-sm text-gray-500 mt-1">잠시만 기다려 주세요</p>
    </div>
  );
}

export default function OAuthCallbackPage() {
  return (
    <Suspense fallback={
      <div className="min-h-screen flex flex-col items-center justify-center bg-gray-50">
        <LoadingSpinner className="mb-4" />
        <p className="text-lg font-medium text-gray-900">로그인 중...</p>
        <p className="text-sm text-gray-500 mt-1">잠시만 기다려 주세요</p>
      </div>
    }>
      <CallbackContent />
    </Suspense>
  );
}
