'use client';

import { useState } from 'react';
import { QualityGrade } from '@/types';

interface QualityBadgeProps {
  grade: QualityGrade;
  score?: number;
}

const GRADE_CONFIG: Record<QualityGrade, { label: string; bg: string; text: string; border: string }> = {
  EXCELLENT: { label: '우수', bg: 'bg-yellow-50', text: 'text-yellow-700', border: 'border-yellow-300' },
  GOOD: { label: '좋음', bg: 'bg-emerald-50', text: 'text-emerald-700', border: 'border-emerald-300' },
  NORMAL: { label: '보통', bg: 'bg-blue-50', text: 'text-blue-700', border: 'border-blue-300' },
  LOW: { label: '부족', bg: 'bg-gray-50', text: 'text-gray-600', border: 'border-gray-300' },
  SPAM: { label: '스팸', bg: 'bg-red-50', text: 'text-red-700', border: 'border-red-300' },
};

export default function QualityBadge({ grade, score }: QualityBadgeProps) {
  const [showScore, setShowScore] = useState(false);
  const config = GRADE_CONFIG[grade];

  return (
    <span
      className={`inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-medium border cursor-default ${config.bg} ${config.text} ${config.border}`}
      onClick={() => setShowScore(!showScore)}
      onMouseEnter={() => setShowScore(true)}
      onMouseLeave={() => setShowScore(false)}
      title={score !== undefined ? `품질 점수: ${score}` : undefined}
    >
      {grade === 'EXCELLENT' && (
        <svg xmlns="http://www.w3.org/2000/svg" className="h-3 w-3" viewBox="0 0 20 20" fill="currentColor">
          <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
        </svg>
      )}
      {config.label}
      {showScore && score !== undefined && (
        <span className="ml-0.5 opacity-75">{score}</span>
      )}
    </span>
  );
}
