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

// 로컬스토리지에 저장되는 사용자 정보
export interface AuthUser {
  userId: number;
  email: string;
  name: string;
  role: string;
}

// === OAuth ===

export type OAuthProvider = 'kakao' | 'google';

export interface OAuthAuthorizationResponse {
  authorizationUrl: string;
}

export interface OAuthLinkRequest {
  provider: OAuthProvider;
  authorizationCode: string;
  state: string;
}

export interface OAuthLinkResponse {
  provider: OAuthProvider;
  linkedAt: string;
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

export type CommunityCategory = 'DAILY' | 'HEALTH' | 'HOBBY' | 'HELP' | 'NOTICE';

export interface CommunityPostResponse {
  id: number;
  userId: number;
  userName: string;
  title: string;
  content: string;
  category: CommunityCategory;
  createdAt: string;
}

export interface CommunityPostRequest {
  title: string;
  content: string;
  category?: string;
}

// === 알림 (Alert) ===

export type AlertLevel = 'WARNING' | 'DANGER' | 'CRITICAL';
export type AlertStatus = 'ACTIVE' | 'RESOLVED';

export interface AlertResponse {
  id: string;
  userId: number;
  userName: string;
  level: AlertLevel;
  message: string;
  status: AlertStatus;
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
  activeEscalations: number;
  missedCheckIns: number;
  activeVolunteers: number;
  pendingVolunteers: number;
  activeCareMatches: number;
  avgHealthScore: number;
  criticalHealthAlerts: number;
}

// === Admin 에스컬레이션 ===

export type EscalationStage = 'REMINDER' | 'WARNING' | 'DANGER' | 'CRITICAL';

export interface EscalationAdminResponse {
  id: number;
  userId: number;
  userName: string;
  stage: EscalationStage;
  triggeredAt: string;
  resolvedAt: string | null;
  resolved: boolean;
  notifiedContacts: string | null;
}

// === Admin 돌봄 방문 ===

export interface CareVisitAdminResponse {
  id: number;
  careMatchId: number;
  volunteerId: number;
  receiverId: number;
  scheduledDate: string;
  scheduledTime: string;
  status: string;
  reportContent: string | null;
  receiverCondition: ReceiverCondition | null;
  specialNotes: string | null;
  visitedAt: string | null;
  createdAt: string;
}

// === Admin 자원봉사자 ===

export interface VolunteerAdminResponse {
  id: number;
  userId: number;
  availableDays: string;
  availableTimeStart: string;
  availableTimeEnd: string;
  radius: number;
  latitude: number;
  longitude: number;
  introduction: string;
  status: string;
  trustScore: number;
  totalVisits: number;
  approvedAt: string | null;
  createdAt: string;
}

// === 스마트 체크인 ===

export interface CheckInSchedule {
  intervalHours: number;
  activeStart: string;
  activeEnd: string;
  pushEnabled: boolean;
  emailEnabled: boolean;
  reminderBeforeMinutes: number;
  warningAfterHours: number;
  dangerAfterHours: number;
  criticalAfterHours: number;
  pauseUntil: string | null;
}

export interface CheckInScheduleRequest {
  intervalHours: number;
  activeStart: string;
  activeEnd: string;
  pushEnabled: boolean;
  emailEnabled: boolean;
  reminderBeforeMinutes: number;
  warningAfterHours: number;
  dangerAfterHours: number;
  criticalAfterHours: number;
  pauseUntil?: string | null;
}

export interface CheckInScheduleResponse {
  id: number;
  userId: number;
  intervalHours: number;
  activeStart: string;
  activeEnd: string;
  pushEnabled: boolean;
  emailEnabled: boolean;
  reminderBeforeMinutes: number;
  warningAfterHours: number;
  dangerAfterHours: number;
  criticalAfterHours: number;
  pauseUntil: string | null;
  updatedAt: string;
}

export type EscalationLevel = 'WARNING' | 'DANGER' | 'CRITICAL';

export interface Escalation {
  id: number;
  userId: number;
  level: EscalationLevel;
  triggeredAt: string;
  resolvedAt: string | null;
  notifiedGuardians: boolean;
}

export interface EscalationResponse {
  escalations: Escalation[];
  totalCount: number;
}

export interface EnhancedCheckInRequest {
  message?: string;
  mood: number;
  healthTags: string[];
}

export interface EnhancedCheckInResponse {
  id: number;
  userId: number;
  userName: string;
  status: string;
  message: string;
  mood: number;
  healthTags: string[];
  checkedAt: string;
}

// === 건강 일지 ===

export interface MedicineEntry {
  name: string;
  taken: boolean;
}

export interface MealRecord {
  breakfast: boolean | null;
  lunch: boolean | null;
  dinner: boolean | null;
}

export interface HealthJournal {
  id: number;
  userId: number;
  date: string;
  mood: number;
  symptoms: string[];
  medicines: MedicineEntry[];
  meals: MealRecord;
  sleepHours: number | null;
  sleepQuality: number | null;
  exerciseMinutes: number | null;
  painLevel: number | null;
  note: string;
  tags: string[];
  createdAt: string;
  updatedAt: string;
}

export interface HealthJournalRequest {
  date: string;
  mood: number;
  symptoms: string[];
  medicines: MedicineEntry[];
  meals: MealRecord;
  sleepHours?: number | null;
  sleepQuality?: number | null;
  exerciseMinutes?: number | null;
  painLevel?: number | null;
  note?: string;
}

export interface HealthJournalResponse {
  id: number;
  userId: number;
  date: string;
  mood: number;
  symptoms: string[];
  medicines: MedicineEntry[];
  meals: MealRecord;
  sleepHours: number | null;
  sleepQuality: number | null;
  exerciseMinutes: number | null;
  painLevel: number | null;
  note: string;
  tags: string[];
  createdAt: string;
  updatedAt: string;
}

export interface MoodTrendItem {
  date: string;
  label: string;
  mood: number | null;
}

export interface SymptomFrequencyItem {
  symptom: string;
  label: string;
  count: number;
}

export interface MealComplianceItem {
  type: string;
  label: string;
  rate: number;
  completed: number;
  total: number;
}

export interface HealthTrend {
  moodTrend: MoodTrendItem[];
  symptomFrequency: SymptomFrequencyItem[];
  mealCompliance: MealComplianceItem[];
}

export interface HealthTrendResponse {
  period: 'weekly' | 'monthly';
  moodTrend: MoodTrendItem[];
  symptomFrequency: SymptomFrequencyItem[];
  mealCompliance: MealComplianceItem[];
}

export interface HealthSummary {
  todayMood: number | null;
  avgMood: number;
  moodTrend: number;
  journalRate: number;
  journalDays: number;
  totalDays: number;
  mealRate: number;
  topSymptom: string | null;
  topSymptomCount: number;
  streak: number;
  weeklyData: MoodTrendItem[];
}

// === Admin 사용자 상세 ===

export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';
export type UserRole = 'ROLE_USER' | 'ROLE_ADMIN';

export interface UserDetailResponse {
  id: number;
  email: string;
  name: string;
  phone: string;
  role: UserRole;
  status: UserStatus;
  createdAt: string;
  lastCheckInAt: string | null;
}

// === 이웃 돌봄 (Neighbor Care) ===

export type VolunteerStatus = 'ACTIVE' | 'INACTIVE' | 'SUSPENDED' | 'WITHDRAWN';
export type CareMatchStatus = 'PENDING' | 'ACCEPTED' | 'COMPLETED' | 'CANCELLED' | 'REJECTED';
export type CareVisitStatus = 'UPCOMING' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
export type ReceiverCondition = 'GOOD' | 'FAIR' | 'POOR' | 'CRITICAL';
export type TrustLevel = 'new' | 'bronze' | 'silver' | 'gold';
export type DayOfWeek = 'MON' | 'TUE' | 'WED' | 'THU' | 'FRI' | 'SAT' | 'SUN';

export interface Volunteer {
  id: number;
  userId: number;
  name: string;
  phone: string;
  introduction: string;
  availableDays: DayOfWeek[];
  availableStart: string;
  availableEnd: string;
  address: string;
  latitude: number;
  longitude: number;
  radiusKm: number;
  trustScore: number;
  trustLevel: TrustLevel;
  status: VolunteerStatus;
  visitCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface VolunteerRequest {
  phone: string;
  introduction?: string;
  availableDays: DayOfWeek[];
  availableStart: string;
  availableEnd: string;
  address: string;
  latitude?: number;
  longitude?: number;
  radiusKm: number;
}

export interface VolunteerResponse extends Volunteer {}

export interface CareMatch {
  id: number;
  volunteerId: number;
  volunteerName: string;
  recipientId: number;
  recipientName: string;
  distance: string;
  trustScore: number;
  trustLevel: TrustLevel;
  visitCount: number;
  status: CareMatchStatus;
  matchedAt: string;
  completedAt: string | null;
}

export interface CareMatchResponse extends CareMatch {}

export interface CareMatchCandidate {
  id: number;
  userId: number;
  name: string;
  introduction: string;
  distance: string;
  availableTime: string;
  availableDays: DayOfWeek[];
  trustScore: number;
  trustLevel: TrustLevel;
  visitCount: number;
  requested?: boolean;
}

export interface CareVisit {
  id: number;
  matchId: number;
  volunteerId: number;
  volunteerName: string;
  recipientId: number;
  recipientName: string;
  partnerName: string;
  scheduledAt: string;
  time: string;
  duration: string;
  status: CareVisitStatus;
  reportId: number | null;
  createdAt: string;
}

export interface CareVisitRequest {
  matchId: number;
  scheduledAt: string;
  duration: string;
}

export interface CareVisitResponse extends CareVisit {}

export interface CareVisitReportRequest {
  visitId: number;
  condition: ReceiverCondition;
  observation: string;
  concerns: string[];
}

export interface CareVisitReport {
  id: number;
  visitId: number;
  volunteerId: number;
  volunteerName: string;
  recipientId: number;
  recipientName: string;
  condition: ReceiverCondition;
  observation: string;
  concerns: string[];
  scheduledAt: string;
  duration: string;
  createdAt: string;
}

// === 알림 (Notification) ===

export type NotificationType =
  | 'CHECKIN_REMINDER'
  | 'ESCALATION'
  | 'HEALTH_ALERT'
  | 'CARE_MATCH'
  | 'CARE_VISIT'
  | 'COMMUNITY_REPLY'
  | 'SYSTEM';

export interface NotificationResponse {
  id: number;
  type: NotificationType;
  title: string;
  message: string;
  relatedId: number | null;
  relatedType: string | null;
  read: boolean;
  createdAt: string;
}

// === 커뮤니티 댓글 ===

export interface CommentRequest {
  content: string;
  parentId?: number | null;
}

export interface CommentResponse {
  id: number;
  postId: number;
  userId: number;
  authorName: string;
  content: string;
  parentId: number | null;
  replies: CommentResponse[];
  createdAt: string;
  updatedAt: string;
}
