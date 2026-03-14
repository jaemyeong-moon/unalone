import { CommunityCategory } from '@/types';

/** 카테고리 표시명 매핑 */
export const CATEGORY_LABELS: Record<CommunityCategory, string> = {
  DAILY: '일상',
  HEALTH: '건강',
  HOBBY: '취미',
  HELP: '도움요청',
  NOTICE: '공지',
};

/** 필터용 카테고리 목록 (전체 포함) */
export const CATEGORY_FILTER_MAP: Record<string, string> = {
  '전체': '',
  '일상': 'DAILY',
  '건강': 'HEALTH',
  '취미': 'HOBBY',
  '도움요청': 'HELP',
  '공지': 'NOTICE',
};

export const CATEGORY_FILTER_LABELS = Object.keys(CATEGORY_FILTER_MAP);
