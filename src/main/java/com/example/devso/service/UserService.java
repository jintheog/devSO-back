package com.example.devso.service;

import com.example.devso.dto.request.PasswordChangeRequest;
import com.example.devso.dto.request.ProfileUpdateRequest;
import com.example.devso.dto.request.UserUpdateRequest;
import com.example.devso.dto.response.UserProfileResponse;
import com.example.devso.dto.response.UserResponse;
import com.example.devso.entity.*;
import com.example.devso.repository.FollowRepository;
import com.example.devso.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects; // ✅ NullPointerException 방지를 위해 추가
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FollowRepository followRepository;

    /**
     * 프로필 조회 (팔로우 카운트 및 팔로우 여부 포함)
     */
    public UserProfileResponse getUserProfileByUsername(String targetUsername, Long currentUserId) {
        User targetUser = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        long followerCount = followRepository.countByFollowingId(targetUser.getId());
        long followingCount = followRepository.countByFollowerId(targetUser.getId());

        boolean isFollowing = false;
        if (currentUserId != null) {
            isFollowing = followRepository.existsByFollowerIdAndFollowingId(currentUserId, targetUser.getId());
        }

        return UserProfileResponse.from(targetUser, followerCount, followingCount, isFollowing);
    }

    /**
     * 프로필 및 이력 정보 통합 수정
     */
    @Transactional
    public void updateFullProfileByUsername(String username, ProfileUpdateRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        // ✅ [수정] Objects.equals를 사용하여 user.getEmail()이 null이어도 NPE가 발생하지 않도록 함
        if (request.getEmail() != null && !Objects.equals(user.getEmail(), request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalStateException("이미 사용 중인 이메일입니다: " + request.getEmail());
            }
        }

        user.updateProfile(
                request.getName(),
                request.getBio(),
                request.getProfileImageUrl(),
                request.getPortfolio(),
                request.getPhone(),
                request.getEmail()
        );

        // 하위 리스트 업데이트 로직
        if (request.getCareers() != null) {
            List<Career> newCareers = request.getCareers().stream()
                    .map(dto -> dto.toEntity(user))
                    .toList();
            user.updateCareers(newCareers);
        }

        if (request.getEducations() != null) {
            List<Education> newEducations = request.getEducations().stream()
                    .map(dto -> dto.toEntity(user))
                    .toList();
            user.updateEducations(newEducations);
        }

        if (request.getActivities() != null) {
            List<Activity> newActivities = request.getActivities().stream()
                    .map(dto -> dto.toEntity(user))
                    .toList();
            user.updateActivities(newActivities);
        }

        if (request.getCertis() != null) {
            List<Certi> newCerti = request.getCertis().stream()
                    .map(dto -> dto.toEntity(user))
                    .toList();
            user.updateCertis(newCerti);
        }

        if (request.getSkills() != null) {
            List<Skill> newSkill = request.getSkills().stream()
                    .map(dto -> dto.toEntity(user))
                    .toList();
            user.updateSkills(newSkill);
        }
    }

    /**
     * 기본 프로필 정보 수정
     */
    @Transactional
    public UserProfileResponse updateProfile(String username, Long currentUserId, UserUpdateRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        if (!user.getId().equals(currentUserId)) {
            throw new IllegalArgumentException("사용자가 아닙니다.");
        }

        // ✅ [수정] 여기도 동일하게 Objects.equals 적용
        if (request.getEmail() != null && !Objects.equals(user.getEmail(), request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalStateException("이미 사용 중인 이메일입니다: " + request.getEmail());
            }
        }

        user.updateProfile(
                request.getName(),
                request.getBio(),
                request.getProfileImageUrl(),
                request.getPortfolio(),
                request.getPhone(),
                request.getEmail()
        );

        long followerCount = followRepository.countByFollowingId(user.getId());
        long followingCount = followRepository.countByFollowerId(user.getId());

        return UserProfileResponse.from(user, followerCount, followingCount, false);
    }

    @Transactional
    public void changePassword(String username, PasswordChangeRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        String newPassword = request.getNewPassword();
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("새로운 비밀번호를 입력해야 합니다.");
        }

        String newEncodedPassword = passwordEncoder.encode(newPassword);
        user.updatePassword(newEncodedPassword);
    }

    public List<UserResponse> searchUsers(String query, Long excludeUserId) {
        List<User> users = userRepository.searchUsers(query, excludeUserId);
        return users.stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 이메일 중복 여부 확인
     * @param email 체크할 이메일
     * @return 존재하면 true, 없으면 false
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}