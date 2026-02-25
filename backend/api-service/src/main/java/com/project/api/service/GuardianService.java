package com.project.api.service;

import com.project.api.domain.Guardian;
import com.project.api.domain.User;
import com.project.api.dto.guardian.GuardianRequest;
import com.project.api.dto.guardian.GuardianResponse;
import com.project.api.exception.BusinessException;
import com.project.api.repository.GuardianRepository;
import com.project.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GuardianService {

    private final GuardianRepository guardianRepository;
    private final UserRepository userRepository;

    @Transactional
    public GuardianResponse addGuardian(Long userId, GuardianRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("사용자를 찾을 수 없습니다"));

        if (guardianRepository.countByUserId(userId) >= 5) {
            throw BusinessException.badRequest("보호자는 최대 5명까지 등록할 수 있습니다");
        }

        Guardian guardian = Guardian.builder()
                .user(user)
                .name(request.getName())
                .phone(request.getPhone())
                .relationship(request.getRelationship())
                .build();

        guardianRepository.save(guardian);
        return GuardianResponse.from(guardian);
    }

    @Transactional(readOnly = true)
    public List<GuardianResponse> getGuardians(Long userId) {
        return guardianRepository.findByUserId(userId).stream()
                .map(GuardianResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeGuardian(Long userId, Long guardianId) {
        Guardian guardian = guardianRepository.findById(guardianId)
                .orElseThrow(() -> BusinessException.notFound("보호자를 찾을 수 없습니다"));

        if (!guardian.getUser().getId().equals(userId)) {
            throw BusinessException.unauthorized("본인의 보호자만 삭제할 수 있습니다");
        }

        guardianRepository.delete(guardian);
    }
}
