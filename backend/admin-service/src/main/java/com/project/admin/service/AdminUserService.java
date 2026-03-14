package com.project.admin.service;

import com.project.admin.domain.CheckIn;
import com.project.admin.domain.User;
import com.project.admin.dto.UserDetailResponse;
import com.project.admin.exception.InvalidRequestException;
import com.project.admin.exception.ResourceNotFoundException;
import com.project.admin.repository.CheckInRepository;
import com.project.admin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final CheckInRepository checkInRepository;

    public Page<UserDetailResponse> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(user -> {
            var lastCheckIn = checkInRepository.findLatestByUserId(user.getId())
                    .map(CheckIn::getCheckedAt)
                    .orElse(null);
            return UserDetailResponse.from(user, lastCheckIn);
        });
    }

    @Transactional
    public void updateUserStatus(Long userId, String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        User.UserStatus userStatus;
        try {
            userStatus = User.UserStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException("유효하지 않은 사용자 상태: " + status);
        }

        user.updateStatus(userStatus);
    }
}
