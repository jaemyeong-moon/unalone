import apiClient from '@/lib/api';
import {
  ApiResponse,
  PageResponse,
  CheckInScheduleResponse,
  CheckInScheduleRequest,
  EscalationResponse,
  EnhancedCheckInRequest,
  EnhancedCheckInResponse,
} from '@/types';

/** 체크인 스케줄 설정 조회 */
export async function getCheckInSchedule(): Promise<CheckInScheduleResponse> {
  const res = await apiClient.get<ApiResponse<CheckInScheduleResponse>>('/api/checkin-settings');
  return res.data.data;
}

/** 체크인 스케줄 설정 수정 */
export async function updateCheckInSchedule(
  request: CheckInScheduleRequest
): Promise<CheckInScheduleResponse> {
  const res = await apiClient.put<ApiResponse<CheckInScheduleResponse>>(
    '/api/checkin-settings',
    request
  );
  return res.data.data;
}

/** 에스컬레이션 목록 조회 */
export async function getEscalations(): Promise<EscalationResponse> {
  const res = await apiClient.get<ApiResponse<EscalationResponse>>('/api/escalations');
  return res.data.data;
}

/** 스마트 체크인 생성 (기분 + 건강 태그 포함) */
export async function createCheckIn(
  request: EnhancedCheckInRequest
): Promise<EnhancedCheckInResponse> {
  const res = await apiClient.post<ApiResponse<EnhancedCheckInResponse>>('/api/checkins', request);
  return res.data.data;
}

/** 체크인 히스토리 조회 */
export async function getCheckInHistory(
  page = 0,
  size = 20
): Promise<PageResponse<EnhancedCheckInResponse>> {
  const res = await apiClient.get<ApiResponse<PageResponse<EnhancedCheckInResponse>>>(
    `/api/checkins?page=${page}&size=${size}`
  );
  return res.data.data;
}
