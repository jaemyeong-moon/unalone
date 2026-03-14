'use client';

import { OAuthProvider } from '@/types';

interface SocialLoginButtonProps {
  provider: OAuthProvider;
  label?: string;
  disabled?: boolean;
  className?: string;
}

const DEFAULT_LABELS: Record<OAuthProvider, string> = {
  kakao: '카카오 로그인',
  google: '구글 로그인',
};

const OAUTH_URLS: Record<OAuthProvider, string> = {
  kakao: '/api/auth/oauth/kakao',
  google: '/api/auth/oauth/google',
};

function KakaoIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path
        fillRule="evenodd"
        clipRule="evenodd"
        d="M9 0.6C4.029 0.6 0 3.726 0 7.554C0 9.918 1.557 12.006 3.933 13.212L2.934 16.77C2.862 17.022 3.15 17.226 3.372 17.082L7.596 14.37C8.058 14.424 8.526 14.454 9 14.454C13.971 14.454 18 11.328 18 7.554C18 3.726 13.971 0.6 9 0.6Z"
        fill="black"
      />
    </svg>
  );
}

function GoogleIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 18 18" xmlns="http://www.w3.org/2000/svg">
      <path d="M17.64 9.205c0-.639-.057-1.252-.164-1.841H9v3.481h4.844a4.14 4.14 0 01-1.796 2.716v2.259h2.908c1.702-1.567 2.684-3.875 2.684-6.615z" fill="#4285F4"/>
      <path d="M9 18c2.43 0 4.467-.806 5.956-2.18l-2.908-2.259c-.806.54-1.837.86-3.048.86-2.344 0-4.328-1.584-5.036-3.711H.957v2.332A8.997 8.997 0 009 18z" fill="#34A853"/>
      <path d="M3.964 10.71A5.41 5.41 0 013.682 9c0-.593.102-1.17.282-1.71V4.958H.957A8.996 8.996 0 000 9c0 1.452.348 2.827.957 4.042l3.007-2.332z" fill="#FBBC05"/>
      <path d="M9 3.58c1.321 0 2.508.454 3.44 1.345l2.582-2.58C13.463.891 11.426 0 9 0A8.997 8.997 0 00.957 4.958L3.964 6.29C4.672 4.163 6.656 3.58 9 3.58z" fill="#EA4335"/>
    </svg>
  );
}

export default function SocialLoginButton({
  provider,
  label,
  disabled = false,
  className = '',
}: SocialLoginButtonProps) {
  const displayLabel = label || DEFAULT_LABELS[provider];

  const handleClick = () => {
    if (!disabled) {
      window.location.href = OAUTH_URLS[provider];
    }
  };

  if (provider === 'kakao') {
    return (
      <button
        type="button"
        onClick={handleClick}
        disabled={disabled}
        className={`w-full h-12 flex items-center justify-center gap-2 rounded-lg font-medium text-sm transition-all duration-200 hover:brightness-95 disabled:opacity-50 disabled:cursor-not-allowed ${className}`}
        style={{ backgroundColor: '#FEE500', color: 'rgba(0, 0, 0, 0.85)' }}
        aria-label={displayLabel}
      >
        <KakaoIcon />
        {displayLabel}
      </button>
    );
  }

  return (
    <button
      type="button"
      onClick={handleClick}
      disabled={disabled}
      className={`w-full h-12 flex items-center justify-center gap-2 bg-white border border-gray-300 rounded-lg font-medium text-sm text-gray-700 transition-all duration-200 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed ${className}`}
      aria-label={displayLabel}
    >
      <GoogleIcon />
      {displayLabel}
    </button>
  );
}
