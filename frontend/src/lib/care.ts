import apiClient from '@/lib/api';
import {
  ApiResponse,
  VolunteerRequest,
  VolunteerResponse,
  CareMatchCandidate,
  CareMatchResponse,
  CareVisitRequest,
  CareVisitResponse,
  CareVisitReportRequest,
  CareVisitReport,
  PageResponse,
} from '@/types';

// === 자원봉사 ===

export async function registerVolunteer(data: VolunteerRequest) {
  const res = await apiClient.post<ApiResponse<VolunteerResponse>>('/api/care/volunteers', data);
  return res.data.data;
}

export async function getMyVolunteerStatus() {
  const res = await apiClient.get<ApiResponse<VolunteerResponse>>('/api/care/volunteers/me');
  return res.data.data;
}

export async function updateVolunteer(data: VolunteerRequest) {
  const res = await apiClient.put<ApiResponse<VolunteerResponse>>('/api/care/volunteers/me', data);
  return res.data.data;
}

export async function withdrawVolunteer() {
  const res = await apiClient.delete<ApiResponse<void>>('/api/care/volunteers/me');
  return res.data;
}

export async function findNearbyVolunteers(lat: number, lng: number, radius: number, day?: string) {
  const params: Record<string, string | number> = { lat, lng, distance: radius };
  if (day) params.day = day;
  const res = await apiClient.get<ApiResponse<CareMatchCandidate[]>>('/api/care/matches', { params });
  return res.data.data;
}

// === 매칭 ===

export async function createCareMatch(candidateId: number) {
  const res = await apiClient.post<ApiResponse<CareMatchResponse>>(`/api/care/matches/${candidateId}/request`);
  return res.data.data;
}

export async function getMyMatches() {
  const res = await apiClient.get<ApiResponse<CareMatchResponse[]>>('/api/care/matches/me');
  return res.data.data;
}

export async function acceptMatch(id: number) {
  const res = await apiClient.put<ApiResponse<CareMatchResponse>>(`/api/care/matches/${id}/accept`);
  return res.data.data;
}

export async function completeMatch(id: number) {
  const res = await apiClient.put<ApiResponse<CareMatchResponse>>(`/api/care/matches/${id}/complete`);
  return res.data.data;
}

export async function cancelMatch(id: number) {
  const res = await apiClient.put<ApiResponse<CareMatchResponse>>(`/api/care/matches/${id}/cancel`);
  return res.data.data;
}

// === 방문 ===

export async function scheduleVisit(data: CareVisitRequest) {
  const res = await apiClient.post<ApiResponse<CareVisitResponse>>('/api/care/visits', data);
  return res.data.data;
}

export async function getVisits(params: { year?: number; month?: number; page?: number; size?: number } = {}) {
  const res = await apiClient.get<ApiResponse<PageResponse<CareVisitResponse>>>('/api/care/visits', { params });
  return res.data.data;
}

export async function getVisit(id: number) {
  const res = await apiClient.get<ApiResponse<CareVisitResponse>>(`/api/care/visits/${id}`);
  return res.data.data;
}

export async function cancelVisit(id: number) {
  const res = await apiClient.put<ApiResponse<CareVisitResponse>>(`/api/care/visits/${id}`, { status: 'CANCELLED' });
  return res.data.data;
}

// === 보고서 ===

export async function submitVisitReport(data: CareVisitReportRequest) {
  const res = await apiClient.post<ApiResponse<CareVisitReport>>('/api/care/reports', data);
  return res.data.data;
}

export async function getVisitReport(id: number) {
  const res = await apiClient.get<ApiResponse<CareVisitReport>>(`/api/care/reports/${id}`);
  return res.data.data;
}

export async function getVisitReports(params: { page?: number; size?: number } = {}) {
  const res = await apiClient.get<ApiResponse<PageResponse<CareVisitReport>>>('/api/care/reports', { params });
  return res.data.data;
}

// === 평가 ===

export async function rateVisit(matchId: number, rating: number, review: string) {
  const res = await apiClient.post<ApiResponse<void>>(`/api/care/matches/${matchId}/rate`, {
    rating,
    review,
  });
  return res.data;
}
