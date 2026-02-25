// === 공통 응답 ===

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

// === 인증 ===

export interface LoginResponse {
  token: string;
  userId: number;
  email: string;
  name: string;
  role: string;
}

export interface SignupRequest {
  name: string;
  email: string;
  password: string;
  phone?: string;
}

// === 체크인 ===

export interface CheckInResponse {
  id: number;
  userId: number;
  userName: string;
  status: string;
  message: string;
  checkedAt: string;
}

export interface CheckInRequest {
  message?: string;
}

// === 프로필 ===

export interface ProfileResponse {
  userId: number;
  userName: string;
  email: string;
  phone: string;
  checkIntervalHours: number;
  activeHoursStart: string;
  activeHoursEnd: string;
  address: string;
  emergencyNote: string;
}

export interface ProfileRequest {
  checkIntervalHours?: number;
  activeHoursStart?: string;
  activeHoursEnd?: string;
  address?: string;
  emergencyNote?: string;
}

// === 보호자 ===

export interface GuardianResponse {
  id: number;
  name: string;
  phone: string;
  relationship: string;
}

export interface GuardianRequest {
  name: string;
  phone: string;
  relationship?: string;
}

// === 커뮤니티 ===

export interface CommunityPostResponse {
  id: number;
  userId: number;
  userName: string;
  title: string;
  content: string;
  category: string;
  createdAt: string;
}

export interface CommunityPostRequest {
  title: string;
  content: string;
  category?: string;
}

// === 알림 (Alert) ===

export interface AlertResponse {
  id: string;
  userId: number;
  userName: string;
  level: string;
  message: string;
  status: string;
  createdAt: string;
  resolvedAt: string | null;
}

// === Admin 대시보드 ===

export interface DashboardResponse {
  totalUsers: number;
  activeUsers: number;
  todayCheckIns: number;
  activeAlerts: number;
  warningAlerts: number;
  dangerAlerts: number;
  criticalAlerts: number;
}

// === Admin 사용자 상세 ===

export interface UserDetailResponse {
  id: number;
  email: string;
  name: string;
  phone: string;
  role: string;
  status: string;
  createdAt: string;
  lastCheckInAt: string | null;
}
