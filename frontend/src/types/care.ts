export interface Volunteer {
  id: number;
  name: string;
  phone: string;
  address: string;
  availableDays: string[];
  availableTimeStart: string;
  availableTimeEnd: string;
  trustScore: number;
  totalVisits: number;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
}

export interface CareMatch {
  id: number;
  volunteer: Volunteer;
  user: { id: number; name: string; address: string };
  status: 'PENDING' | 'ACCEPTED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
  scheduledDate: string;
  visitReport?: string;
  rating?: number;
}

export interface VisitReport {
  matchId: number;
  status: 'NORMAL' | 'CONCERN' | 'EMERGENCY';
  notes: string;
  photoUrl?: string;
}

export interface VisitRating {
  matchId: number;
  rating: number;
  review: string;
}
