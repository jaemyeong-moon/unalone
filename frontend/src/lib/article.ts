import apiClient, { adminClient } from '@/lib/api';
import {
  ApiResponse,
  PageResponse,
  ArticleCategory,
  ArticleStatus,
  CrawledArticleResponse,
  ArticleDetailResponse,
  NewsSourceResponse,
  NewsSourceRequest,
  CrawlStatsResponse,
} from '@/types';

/** 기사 카테고리 표시명 매핑 */
export const ARTICLE_CATEGORY_LABELS: Record<ArticleCategory, string> = {
  HEALTH: '건강',
  WELFARE: '복지',
  ELDERLY_CARE: '노인돌봄',
  SAFETY: '안전',
  POLICY: '정책',
  LIFESTYLE: '생활',
};

/** 기사 카테고리 색상 매핑 */
export const ARTICLE_CATEGORY_COLORS: Record<ArticleCategory, { bg: string; text: string }> = {
  HEALTH: { bg: 'bg-emerald-50', text: 'text-emerald-600' },
  WELFARE: { bg: 'bg-blue-50', text: 'text-blue-600' },
  ELDERLY_CARE: { bg: 'bg-purple-50', text: 'text-purple-600' },
  SAFETY: { bg: 'bg-orange-50', text: 'text-orange-600' },
  POLICY: { bg: 'bg-indigo-50', text: 'text-indigo-600' },
  LIFESTYLE: { bg: 'bg-pink-50', text: 'text-pink-600' },
};

/** 기사 상태 표시명 매핑 */
export const ARTICLE_STATUS_LABELS: Record<ArticleStatus, string> = {
  CRAWLED: '수집됨',
  SUMMARIZED: '요약완료',
  PUBLISHED: '게시됨',
  REJECTED: '반려됨',
  FAILED: '실패',
};

/** 필터용 카테고리 목록 */
export const ARTICLE_CATEGORY_FILTER_MAP: Record<string, string> = {
  '전체': '',
  '건강': 'HEALTH',
  '복지': 'WELFARE',
  '노인돌봄': 'ELDERLY_CARE',
  '안전': 'SAFETY',
  '정책': 'POLICY',
  '생활': 'LIFESTYLE',
};

export const ARTICLE_CATEGORY_FILTER_LABELS = Object.keys(ARTICLE_CATEGORY_FILTER_MAP);

// === Public API ===

/** 기사 목록 조회 */
export async function getArticles(
  page: number,
  size: number = 10,
  category?: ArticleCategory
): Promise<PageResponse<CrawledArticleResponse>> {
  const params = new URLSearchParams({ page: String(page), size: String(size) });
  if (category) params.append('category', category);
  const res = await apiClient.get<ApiResponse<PageResponse<CrawledArticleResponse>>>(
    `/api/articles?${params}`
  );
  return res.data.data;
}

/** 기사 상세 조회 */
export async function getArticle(id: number): Promise<ArticleDetailResponse> {
  const res = await apiClient.get<ApiResponse<ArticleDetailResponse>>(`/api/articles/${id}`);
  return res.data.data;
}

/** 인기 기사 조회 (상위 10개) */
export async function getPopularArticles(): Promise<CrawledArticleResponse[]> {
  const res = await apiClient.get<ApiResponse<CrawledArticleResponse[]>>('/api/articles/popular');
  return res.data.data;
}

// === Admin API ===

/** 뉴스 소스 목록 조회 */
export async function getNewsSources(): Promise<NewsSourceResponse[]> {
  const res = await adminClient.get<ApiResponse<NewsSourceResponse[]>>('/api/admin/news-sources');
  return res.data.data;
}

/** 뉴스 소스 생성 */
export async function createNewsSource(request: NewsSourceRequest): Promise<NewsSourceResponse> {
  const res = await adminClient.post<ApiResponse<NewsSourceResponse>>('/api/admin/news-sources', request);
  return res.data.data;
}

/** 뉴스 소스 수정 */
export async function updateNewsSource(id: number, request: NewsSourceRequest): Promise<NewsSourceResponse> {
  const res = await adminClient.put<ApiResponse<NewsSourceResponse>>(`/api/admin/news-sources/${id}`, request);
  return res.data.data;
}

/** 뉴스 소스 삭제 */
export async function deleteNewsSource(id: number): Promise<void> {
  await adminClient.delete(`/api/admin/news-sources/${id}`);
}

/** 수동 크롤링 실행 */
export async function triggerCrawl(): Promise<void> {
  await adminClient.post('/api/admin/news-sources/crawl');
}

/** 관리자 기사 목록 조회 (상태 필터 가능) */
export async function getAdminArticles(
  page: number,
  status?: ArticleStatus
): Promise<PageResponse<ArticleDetailResponse>> {
  const params = new URLSearchParams({ page: String(page), size: '20' });
  if (status) params.append('status', status);
  const res = await adminClient.get<ApiResponse<PageResponse<ArticleDetailResponse>>>(
    `/api/admin/articles?${params}`
  );
  return res.data.data;
}

/** 기사 게시 승인 */
export async function publishArticle(id: number): Promise<void> {
  await adminClient.put(`/api/admin/articles/${id}/publish`);
}

/** 기사 반려 */
export async function rejectArticle(id: number): Promise<void> {
  await adminClient.put(`/api/admin/articles/${id}/reject`);
}

/** 크롤링 통계 조회 */
export async function getCrawlStats(): Promise<CrawlStatsResponse> {
  const res = await adminClient.get<ApiResponse<CrawlStatsResponse>>('/api/admin/articles/stats');
  return res.data.data;
}
