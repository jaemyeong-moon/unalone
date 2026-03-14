import apiClient from '@/lib/api';
import { ApiResponse, LoginResponse } from '@/types';
import { OAuthConnection } from '@/types/oauth';

/** OAuth 소셜 로그인 */
export async function oauthLogin(
  provider: string,
  authorizationCode: string,
  redirectUri: string
): Promise<LoginResponse> {
  const res = await apiClient.post<ApiResponse<LoginResponse>>(
    `/api/auth/oauth/${provider}`,
    { code: authorizationCode, redirectUri }
  );
  return res.data.data;
}

/** OAuth 계정 연결 */
export async function linkOAuthAccount(
  provider: string,
  authorizationCode: string,
  redirectUri: string
): Promise<void> {
  await apiClient.post(`/api/auth/oauth/${provider}/link`, {
    code: authorizationCode,
    redirectUri,
  });
}

/** OAuth 계정 연결 해제 */
export async function unlinkOAuthAccount(provider: string): Promise<void> {
  await apiClient.delete(`/api/auth/oauth/${provider}/link`);
}

/** 연결된 소셜 계정 목록 조회 */
export async function getOAuthConnections(): Promise<OAuthConnection[]> {
  const res = await apiClient.get<ApiResponse<OAuthConnection[]>>(
    '/api/auth/oauth/connections'
  );
  return res.data.data;
}
