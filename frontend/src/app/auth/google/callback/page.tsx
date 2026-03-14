'use client';

import { useEffect, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { Suspense } from 'react';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import AlertBanner from '@/components/common/AlertBanner';
import { saveAuth } from '@/lib/auth';
import { oauthLogin } from '@/lib/oauth';

function GoogleCallbackContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [error, setError] = useState('');

  useEffect(() => {
    const code = searchParams.get('code');
    const errorParam = searchParams.get('error');

    if (errorParam) {
      router.replace(`/login?error=${encodeURIComponent('구글 로그인에 실패했습니다')}`);
      return;
    }

    if (code) {
      const redirectUri = `${window.location.origin}/auth/google/callback`;
      oauthLogin('google', code, redirectUri)
        .then((data) => {
          saveAuth(data);
          router.replace('/');
        })
        .catch(() => {
          setError('구글 로그인 처리 중 오류가 발생했습니다');
          setTimeout(() => {
            router.replace(`/login?error=${encodeURIComponent('구글 로그인 처리 중 오류가 발생했습니다')}`);
          }, 2000);
        });
    } else {
      router.replace('/login');
    }
  }, [searchParams, router]);

  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gray-50">
      {error ? (
        <div className="max-w-md w-full px-4">
          <AlertBanner message={error} variant="error" />
          <p className="text-sm text-gray-500 text-center mt-2">로그인 페이지로 이동합니다...</p>
        </div>
      ) : (
        <>
          <LoadingSpinner className="mb-4" />
          <p className="text-lg font-medium text-gray-900">구글 로그인 중...</p>
          <p className="text-sm text-gray-500 mt-1">잠시만 기다려 주세요</p>
        </>
      )}
    </div>
  );
}

export default function GoogleCallbackPage() {
  return (
    <Suspense
      fallback={
        <div className="min-h-screen flex flex-col items-center justify-center bg-gray-50">
          <LoadingSpinner className="mb-4" />
          <p className="text-lg font-medium text-gray-900">구글 로그인 중...</p>
          <p className="text-sm text-gray-500 mt-1">잠시만 기다려 주세요</p>
        </div>
      }
    >
      <GoogleCallbackContent />
    </Suspense>
  );
}
