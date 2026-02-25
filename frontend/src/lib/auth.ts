import { LoginResponse } from '@/types';

export function saveAuth(data: LoginResponse) {
  localStorage.setItem('token', data.token);
  localStorage.setItem('user', JSON.stringify({
    userId: data.userId,
    email: data.email,
    name: data.name,
    role: data.role,
  }));
}

export function getUser() {
  if (typeof window === 'undefined') return null;
  const user = localStorage.getItem('user');
  return user ? JSON.parse(user) : null;
}

export function getToken() {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem('token');
}

export function logout() {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
}

export function isLoggedIn() {
  return !!getToken();
}
