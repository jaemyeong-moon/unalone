export interface OAuthConnection {
  provider: string;
  email: string | null;
  nickname: string | null;
  profileImageUrl: string | null;
  connectedAt: string;
}

export interface OAuthLoginRequest {
  provider: string;
  authorizationCode: string;
  redirectUri: string;
}

export interface OAuthLinkRequest {
  provider: string;
  authorizationCode: string;
  redirectUri: string;
}
