import apiClient from '@/lib/api';
import {
  ApiResponse,
  PageResponse,
  HealthJournalResponse,
  HealthJournalRequest,
  HealthTrendResponse,
  HealthSummary,
} from '@/types';

/** 건강 일지 생성 */
export async function createHealthJournal(
  request: HealthJournalRequest
): Promise<HealthJournalResponse> {
  const res = await apiClient.post<ApiResponse<HealthJournalResponse>>(
    '/api/health/journals',
    request
  );
  return res.data.data;
}

/** 건강 일지 목록 조회 */
export async function getHealthJournals(
  page = 0,
  size = 20
): Promise<PageResponse<HealthJournalResponse>> {
  const res = await apiClient.get<ApiResponse<PageResponse<HealthJournalResponse>>>(
    `/api/health/journals?page=${page}&size=${size}`
  );
  return res.data.data;
}

/** 건강 일지 상세 조회 */
export async function getHealthJournal(id: number): Promise<HealthJournalResponse> {
  const res = await apiClient.get<ApiResponse<HealthJournalResponse>>(
    `/api/health/journals/${id}`
  );
  return res.data.data;
}

/** 건강 일지 수정 */
export async function updateHealthJournal(
  id: number,
  request: HealthJournalRequest
): Promise<HealthJournalResponse> {
  const res = await apiClient.put<ApiResponse<HealthJournalResponse>>(
    `/api/health/journals/${id}`,
    request
  );
  return res.data.data;
}

/** 건강 일지 삭제 */
export async function deleteHealthJournal(id: number): Promise<void> {
  await apiClient.delete(`/api/health/journals/${id}`);
}

/** 건강 트렌드 조회 */
export async function getHealthTrends(
  period: 'weekly' | 'monthly' = 'weekly'
): Promise<HealthTrendResponse> {
  const res = await apiClient.get<ApiResponse<HealthTrendResponse>>(
    `/api/health/trends?period=${period}`
  );
  return res.data.data;
}

/** 건강 요약 조회 (대시보드용) */
export async function getHealthSummary(): Promise<HealthSummary> {
  const res = await apiClient.get<ApiResponse<HealthSummary>>('/api/health/summary');
  return res.data.data;
}
