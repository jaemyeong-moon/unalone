package com.project.admin.service;

import com.project.admin.domain.CheckIn;
import com.project.admin.domain.User;
import com.project.admin.dto.UserDetailResponse;
import com.project.admin.repository.CheckInRepository;
import com.project.admin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserService {
    private final UserRepository userRepository;
    private final CheckInRepository checkInRepository;

    @Transactional(readOnly = true)
    public Page<UserDetailResponse> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(user -> {
            java.time.LocalDateTime lastCheckIn = checkInRepository.findLatestByUserId(user.getId())
                    .map(CheckIn::getCheckedAt).orElse(null);
            return UserDetailResponse.from(user, lastCheckIn);
        });
    }

    @Transactional
    public void updateUserStatus(Long userId, String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));
        user.updateStatus(User.UserStatus.valueOf(status));
        userRepository.save(user);
    }
}
