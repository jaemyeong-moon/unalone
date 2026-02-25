package com.project.api.service;

import com.project.api.domain.Profile;
import com.project.api.domain.User;
import com.project.api.dto.profile.ProfileRequest;
import com.project.api.dto.profile.ProfileResponse;
import com.project.api.exception.BusinessException;
import com.project.api.repository.ProfileRepository;
import com.project.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(Long userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> BusinessException.notFound("프로필을 찾을 수 없습니다"));
        return ProfileResponse.from(profile);
    }

    @Transactional
    public ProfileResponse updateProfile(Long userId, ProfileRequest request) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> BusinessException.notFound("프로필을 찾을 수 없습니다"));

        profile.update(
                request.getCheckIntervalHours(),
                request.getActiveHoursStart(),
                request.getActiveHoursEnd(),
                request.getAddress(),
                request.getEmergencyNote()
        );

        User user = profile.getUser();
        profileRepository.save(profile);

        return ProfileResponse.from(profile);
    }
}
