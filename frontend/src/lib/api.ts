import axios from 'axios';

const apiClient = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || '',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

const adminClient = axios.create({
  baseURL: process.env.NEXT_PUBLIC_ADMIN_URL || '',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

export { apiClient, adminClient };
export default apiClient;
