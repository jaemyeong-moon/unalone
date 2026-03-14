package com.project.api.service;

import com.project.api.domain.Profile;
import com.project.api.dto.profile.ProfileRequest;
import com.project.api.dto.profile.ProfileResponse;
import com.project.api.exception.BusinessException;
import com.project.api.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(Long userId) {
        return profileRepository.findByUserId(userId)
                .map(ProfileResponse::from)
                .orElseThrow(() -> BusinessException.notFound("프로필을 찾을 수 없습니다"));
    }

    @Transactional
    public ProfileResponse updateProfile(Long userId, ProfileRequest request) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> BusinessException.notFound("프로필을 찾을 수 없습니다"));

        profile.update(
                request.checkIntervalHours(),
                request.activeHoursStart(),
                request.activeHoursEnd(),
                request.address(),
                request.emergencyNote()
        );

        return ProfileResponse.from(profile);
    }
}
