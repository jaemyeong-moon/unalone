'use client';

import { TranslationStatus } from '@/types';

interface TranslationToggleProps {
  translationStatus?: TranslationStatus;
  showTranslation: boolean;
  onToggle: () => void;
}

export default function TranslationToggle({
  translationStatus,
  showTranslation,
  onToggle,
}: TranslationToggleProps) {
  if (!translationStatus) return null;

  if (translationStatus === 'PENDING') {
    return (
      <span className="inline-flex items-center gap-1 px-3 py-1.5 text-xs font-medium text-amber-600 bg-amber-50 rounded-lg border border-amber-200">
        <svg xmlns="http://www.w3.org/2000/svg" className="h-3.5 w-3.5 animate-spin" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
        </svg>
        번역 중...
      </span>
    );
  }

  if (translationStatus === 'FAILED') {
    return (
      <span className="inline-flex items-center gap-1 px-3 py-1.5 text-xs font-medium text-red-600 bg-red-50 rounded-lg border border-red-200">
        번역 실패
      </span>
    );
  }

  if (translationStatus === 'SKIPPED') {
    return (
      <span className="inline-flex items-center gap-1 px-3 py-1.5 text-xs font-medium text-gray-500 bg-gray-50 rounded-lg border border-gray-200">
        번역 불필요
      </span>
    );
  }

  // TRANSLATED
  return (
    <button
      onClick={onToggle}
      className={`inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium rounded-lg border transition-colors ${
        showTranslation
          ? 'bg-emerald-50 text-emerald-700 border-emerald-300 hover:bg-emerald-100'
          : 'bg-white text-gray-600 border-gray-300 hover:bg-gray-50'
      }`}
    >
      <svg xmlns="http://www.w3.org/2000/svg" className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5h12M9 3v2m1.048 9.5A18.022 18.022 0 016.412 9m6.088 9h7M11 21l5-10 5 10M12.751 5C11.783 10.77 8.07 15.61 3 18.129" />
      </svg>
      {showTranslation ? '원문 보기' : '번역 보기'}
    </button>
  );
}
