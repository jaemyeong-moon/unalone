/** 날짜 문자열을 한국어 상대 시간으로 변환 (예: "3시간 5분 전") */
export function getTimeSince(dateStr: string): string {
  const diff = Date.now() - new Date(dateStr).getTime();
  const hours = Math.floor(diff / (1000 * 60 * 60));
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
  if (hours > 24) return `${Math.floor(hours / 24)}일 전`;
  if (hours > 0) return `${hours}시간 ${minutes}분 전`;
  return `${minutes}분 전`;
}

/** 날짜 문자열을 한국어 절대 시각으로 변환 (예: "3월 14일 09:30") */
export function formatDateTime(dateStr: string): string {
  return new Date(dateStr).toLocaleDateString('ko-KR', {
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

/** 날짜 문자열을 한국어 상세 시각으로 변환 (연도 포함) */
export function formatDateTimeFull(dateStr: string): string {
  return new Date(dateStr).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

/** 날짜 문자열을 커뮤니티용 상대 표기로 변환 */
export function formatRelativeDate(dateStr: string): string {
  const date = new Date(dateStr);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const hours = Math.floor(diff / (1000 * 60 * 60));

  if (hours < 1) return `${Math.floor(diff / (1000 * 60))}분 전`;
  if (hours < 24) return `${hours}시간 전`;
  if (hours < 48) return '어제';
  return date.toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' });
}

/** 날짜 문자열을 짧은 시각으로 변환. null 처리 포함 */
export function formatShortDateTime(dateStr: string | null): string {
  if (!dateStr) return '-';
  return new Date(dateStr).toLocaleDateString('ko-KR', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}
