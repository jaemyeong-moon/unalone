import axios, { AxiosError } from 'axios';

const apiClient = axios.create({
  baseURL: '',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use((config) => {
  if (typeof window !== 'undefined') {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
  }
  return config;
});

const adminClient = axios.create({
  baseURL: '',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

adminClient.interceptors.request.use((config) => {
  if (typeof window !== 'undefined') {
    config.headers.Authorization = `Basic ${btoa('admin:admin')}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401 && typeof window !== 'undefined') {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

adminClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401 && typeof window !== 'undefined') {
      localStorage.removeItem('adminAuth');
    }
    return Promise.reject(error);
  }
);

/** axios 에러에서 서버 메시지를 추출하는 헬퍼 함수 */
export function getErrorMessage(error: unknown, fallback: string): string {
  const axiosErr = error as AxiosError<{ message?: string }>;
  return axiosErr.response?.data?.message ?? fallback;
}

export { apiClient, adminClient };
export default apiClient;
