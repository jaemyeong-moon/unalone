package com.project.api.service;

import com.project.api.config.OAuthProviderConfig;
import com.project.api.domain.Profile;
import com.project.api.domain.User;
import com.project.api.domain.UserOAuthConnection;
import com.project.api.dto.auth.LoginResponse;
import com.project.api.dto.auth.OAuthConnectionResponse;
import com.project.api.dto.auth.OAuthLinkResponse;
import com.project.api.dto.auth.OAuthLoginRequest;
import com.project.api.dto.auth.OAuthUserInfo;
import com.project.api.exception.BusinessException;
import com.project.api.repository.ProfileRepository;
import com.project.api.repository.UserOAuthConnectionRepository;
import com.project.api.repository.UserRepository;
import com.project.api.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthService {

    private final OAuthProviderConfig oAuthProviderConfig;
    private final OAuthProviderFactory oAuthProviderFactory;
    private final KakaoOAuthClient kakaoOAuthClient;
    private final GoogleOAuthClient googleOAuthClient;
    private final UserRepository userRepository;
    private final UserOAuthConnectionRepository oauthConnectionRepository;
    private final ProfileRepository profileRepository;
    private final JwtTokenProvider jwtTokenProvider;

    // state 토큰 저장소 (실제 운영에서는 Redis 등 사용 권장)
    private final Map<String, StateInfo> stateStore = new ConcurrentHashMap<>();

    /**
     * OAuth 인증 URL 생성
     */
    public String getAuthorizationUrl(String provider, String frontendRedirectUri) {
        validateProvider(provider);

        String state = generateState();
        stateStore.put(state, new StateInfo(provider, frontendRedirectUri));

        return switch (provider.toLowerCase()) {
            case "kakao" -> kakaoOAuthClient.buildAuthorizationUrl(state);
            case "google" -> googleOAuthClient.buildAuthorizationUrl(state);
            default -> throw BusinessException.badRequest("지원하지 않는 OAuth 제공자입니다: " + provider);
        };
    }

    /**
     * OAuth 콜백 처리 - 인가 코드를 JWT로 교환
     */
    @Transactional
    public CallbackResult handleCallback(String provider, String code, String state) {
        validateProvider(provider);
        validateState(state, provider);

        // state에서 frontendRedirectUri 추출 후 삭제
        StateInfo stateInfo = stateStore.remove(state);
        String frontendRedirectUri = stateInfo != null ? stateInfo.frontendRedirectUri() : null;

        // 1. Authorization Code -> Access Token
        String accessToken = exchangeToken(provider, code);

        // 2. Access Token -> 사용자 정보
        OAuthUserInfo userInfo = getUserInfo(provider, accessToken);

        // 3. OAuth 연동 조회
        Optional<UserOAuthConnection> existingConnection =
                oauthConnectionRepository.findByOauthProviderAndOauthId(provider.toLowerCase(), userInfo.oauthId());

        if (existingConnection.isPresent()) {
            // 기존 연동 사용자 - 재로그인
            User user = existingConnection.get().getUser();
            LoginResponse loginResponse = buildLoginResponse(user);
            return new CallbackResult(loginResponse, false, null, frontendRedirectUri);
        }

        // 4. 이메일로 기존 계정 조회
        if (userInfo.email() != null) {
            Optional<User> existingUser = userRepository.findByEmail(userInfo.email());
            if (existingUser.isPresent()) {
                // BR-002: 이메일 중복 시 자동 연동 금지
                return new CallbackResult(null, false, userInfo.email(), frontendRedirectUri);
            }
        }

        // 5. 신규 사용자 - 자동 회원가입
        User newUser = createOAuthUser(userInfo);
        createOAuthConnection(newUser, provider, userInfo);
        createDefaultProfile(newUser);

        LoginResponse loginResponse = buildLoginResponse(newUser);
        return new CallbackResult(loginResponse, true, null, frontendRedirectUri);
    }

    /**
     * 기존 계정에 소셜 계정 연동
     */
    @Transactional
    public OAuthLinkResponse linkOAuthAccount(Long userId, String provider, String authorizationCode, String state) {
        validateProvider(provider);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("사용자를 찾을 수 없습니다"));

        // Authorization Code -> Access Token -> 사용자 정보
        String accessToken = exchangeToken(provider, authorizationCode);
        OAuthUserInfo userInfo = getUserInfo(provider, accessToken);

        // 이미 다른 계정에 연동된 소셜 계정인지 확인 (BR-003)
        Optional<UserOAuthConnection> existingConnection =
                oauthConnectionRepository.findByOauthProviderAndOauthId(provider.toLowerCase(), userInfo.oauthId());
        if (existingConnection.isPresent()) {
            throw BusinessException.conflict("이 소셜 계정은 이미 다른 계정에 연동되어 있습니다");
        }

        // 이미 같은 provider로 연동된 것이 있는지 확인
        Optional<UserOAuthConnection> userExisting =
                oauthConnectionRepository.findByUserAndOauthProvider(user, provider.toLowerCase());
        if (userExisting.isPresent()) {
            throw BusinessException.conflict("이미 " + provider + " 계정이 연동되어 있습니다");
        }

        UserOAuthConnection connection = createOAuthConnection(user, provider, userInfo);
        return OAuthLinkResponse.of(provider, connection.getConnectedAt());
    }

    /**
     * OAuth 직접 로그인 - 프론트엔드에서 Authorization Code를 직접 전달하는 방식
     * POST /api/auth/oauth/login
     */
    @Transactional
    public LoginResult login(OAuthLoginRequest request) {
        String provider = request.provider().toLowerCase();
        validateProvider(provider);

        // 1. Authorization Code -> Access Token
        OAuthProviderClient client = oAuthProviderFactory.getClient(provider);
        String accessToken = client.exchangeToken(request.authorizationCode());

        // 2. Access Token -> 사용자 정보
        OAuthUserInfo userInfo = client.getUserInfo(accessToken);

        // 3. OAuth 연동 조회
        Optional<UserOAuthConnection> existingConnection =
                oauthConnectionRepository.findByOauthProviderAndOauthId(provider, userInfo.oauthId());

        if (existingConnection.isPresent()) {
            // 기존 연동 사용자 - 재로그인
            User user = existingConnection.get().getUser();
            LoginResponse loginResponse = buildLoginResponse(user);
            return new LoginResult(loginResponse, false);
        }

        // 4. 이메일로 기존 계정 조회
        if (userInfo.email() != null) {
            Optional<User> existingUser = userRepository.findByEmail(userInfo.email());
            if (existingUser.isPresent()) {
                // BR-002: 이메일 중복 시 자동 연동 금지
                throw BusinessException.conflict(
                        "이미 해당 이메일로 가입된 계정이 있습니다. 기존 계정에 로그인 후 소셜 계정을 연동해주세요.");
            }
        }

        // 5. 신규 사용자 - 자동 회원가입
        User newUser = createOAuthUser(userInfo);
        createOAuthConnection(newUser, provider, userInfo);
        createDefaultProfile(newUser);

        LoginResponse loginResponse = buildLoginResponse(newUser);
        return new LoginResult(loginResponse, true);
    }

    /**
     * 현재 사용자의 연동된 OAuth 제공자 목록 조회
     */
    @Transactional(readOnly = true)
    public List<OAuthConnectionResponse> getConnectedProviders(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("사용자를 찾을 수 없습니다"));

        return oauthConnectionRepository.findByUser(user).stream()
                .map(OAuthConnectionResponse::from)
                .toList();
    }

    /**
     * 소셜 계정 연동 해제
     */
    @Transactional
    public void unlinkOAuthAccount(Long userId, String provider) {
        validateProvider(provider);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("사용자를 찾을 수 없습니다"));

        UserOAuthConnection connection = oauthConnectionRepository.findByUserAndOauthProvider(user, provider.toLowerCase())
                .orElseThrow(() -> BusinessException.notFound("해당 소셜 계정 연동을 찾을 수 없습니다"));

        // BR-006: 최소 1개 로그인 수단 유지
        long oauthCount = oauthConnectionRepository.countByUser(user);
        if (!user.hasPassword() && oauthCount <= 1) {
            throw BusinessException.badRequest("최소 하나의 로그인 수단이 필요합니다. 비밀번호를 먼저 설정해주세요.");
        }

        oauthConnectionRepository.delete(connection);
    }

    // === Private helper methods ===

    private void validateProvider(String provider) {
        if (!oAuthProviderConfig.isSupported(provider)) {
            throw BusinessException.badRequest("지원하지 않는 OAuth 제공자입니다: " + provider);
        }
    }

    private void validateState(String state, String provider) {
        if (state == null || state.isBlank()) {
            throw BusinessException.badRequest("유효하지 않은 인증 요청입니다");
        }
        StateInfo stateInfo = stateStore.get(state);
        if (stateInfo == null) {
            throw BusinessException.badRequest("유효하지 않은 인증 요청입니다");
        }
        if (!stateInfo.provider().equalsIgnoreCase(provider)) {
            throw BusinessException.badRequest("유효하지 않은 인증 요청입니다");
        }
    }

    private String exchangeToken(String provider, String code) {
        return switch (provider.toLowerCase()) {
            case "kakao" -> kakaoOAuthClient.exchangeToken(code);
            case "google" -> googleOAuthClient.exchangeToken(code);
            default -> throw BusinessException.badRequest("지원하지 않는 OAuth 제공자입니다: " + provider);
        };
    }

    private OAuthUserInfo getUserInfo(String provider, String accessToken) {
        return switch (provider.toLowerCase()) {
            case "kakao" -> kakaoOAuthClient.getUserInfo(accessToken);
            case "google" -> googleOAuthClient.getUserInfo(accessToken);
            default -> throw BusinessException.badRequest("지원하지 않는 OAuth 제공자입니다: " + provider);
        };
    }

    private User createOAuthUser(OAuthUserInfo userInfo) {
        String name = userInfo.name() != null ? userInfo.name() : "사용자";
        String email = userInfo.email();

        User user = User.builder()
                .email(email)
                .password(null)  // BR-005: 소셜 가입 사용자의 password는 null
                .name(name)
                .role(User.Role.ROLE_USER)  // BR-004: 자동 가입 사용자 역할은 ROLE_USER
                .build();

        return userRepository.save(user);
    }

    private UserOAuthConnection createOAuthConnection(User user, String provider, OAuthUserInfo userInfo) {
        UserOAuthConnection connection = UserOAuthConnection.builder()
                .user(user)
                .oauthProvider(provider.toLowerCase())
                .oauthId(userInfo.oauthId())
                .oauthEmail(userInfo.email())
                .oauthNickname(userInfo.name())
                .profileImageUrl(userInfo.profileImageUrl())
                .build();

        return oauthConnectionRepository.save(connection);
    }

    private void createDefaultProfile(User user) {
        Profile profile = Profile.builder()
                .user(user)
                .checkIntervalHours(24)
                .activeHoursStart("08:00")
                .activeHoursEnd("22:00")
                .build();
        profileRepository.save(profile);
    }

    private LoginResponse buildLoginResponse(User user) {
        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return LoginResponse.of(token, user);
    }

    private String generateState() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // 콜백 결과를 담는 record
    public record CallbackResult(
            LoginResponse loginResponse,
            boolean isNewUser,
            String existingEmail,  // 이메일 중복 시 해당 이메일
            String frontendRedirectUri
    ) {
        public boolean isEmailConflict() {
            return existingEmail != null;
        }
    }

    /**
     * 직접 로그인 결과를 담는 record
     */
    public record LoginResult(
            LoginResponse loginResponse,
            boolean isNewUser
    ) {
    }

    private record StateInfo(String provider, String frontendRedirectUri) {
    }
}
