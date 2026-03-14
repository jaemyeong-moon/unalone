import { AuthUser, LoginResponse } from '@/types';

export function saveAuth(data: LoginResponse): void {
  localStorage.setItem('token', data.token);
  localStorage.setItem('user', JSON.stringify({
    userId: data.userId,
    email: data.email,
    name: data.name,
    role: data.role,
  } satisfies AuthUser));
}

export function getUser(): AuthUser | null {
  if (typeof window === 'undefined') return null;
  const user = localStorage.getItem('user');
  return user ? (JSON.parse(user) as AuthUser) : null;
}

export function getToken(): string | null {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem('token');
}

export function logout(): void {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
}

export function isLoggedIn(): boolean {
  return !!getToken();
}
