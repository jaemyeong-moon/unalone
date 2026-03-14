package com.project.api.service;

import com.project.api.domain.Profile;
import com.project.api.domain.User;
import com.project.api.dto.auth.LoginRequest;
import com.project.api.dto.auth.LoginResponse;
import com.project.api.dto.auth.SignupRequest;
import com.project.api.exception.BusinessException;
import com.project.api.repository.ProfileRepository;
import com.project.api.repository.UserRepository;
import com.project.api.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public LoginResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw BusinessException.conflict("이미 등록된 이메일입니다: " + request.email());
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .phone(request.phone())
                .role(User.Role.ROLE_USER)
                .build();
        userRepository.save(user);

        Profile profile = Profile.builder()
                .user(user)
                .checkIntervalHours(24)
                .activeHoursStart("08:00")
                .activeHoursEnd("22:00")
                .build();
        profileRepository.save(profile);

        return buildLoginResponse(user);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> BusinessException.unauthorized("이메일 또는 비밀번호가 올바르지 않습니다"));

        if (!user.hasPassword()) {
            throw BusinessException.unauthorized("소셜 로그인으로 가입된 계정입니다. 소셜 로그인을 이용해주세요.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw BusinessException.unauthorized("이메일 또는 비밀번호가 올바르지 않습니다");
        }

        return buildLoginResponse(user);
    }

    private LoginResponse buildLoginResponse(User user) {
        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return LoginResponse.of(token, user);
    }
}
