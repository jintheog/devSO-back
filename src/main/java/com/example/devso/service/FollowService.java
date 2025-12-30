package com.example.devso.service;

import com.example.devso.dto.response.FollowResponse;
import com.example.devso.dto.response.UserResponse;
import com.example.devso.entity.Follow;
import com.example.devso.entity.User;
import com.example.devso.exception.CustomException;
import com.example.devso.exception.ErrorCode;
import com.example.devso.repository.FollowRepository;
import com.example.devso.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    /**
     * 팔로우/팔로잉 카운트를 조회하여 FollowResponse로 반환합니다.
     */
    private FollowResponse getFollowCounts(Long userId, boolean isFollowing){
        long followerCount = followRepository.countByFollowingId(userId);
        long followingCount = followRepository.countByFollowerId(userId);

        return FollowResponse.of(isFollowing, followerCount, followingCount);
    }

    /**
     * 사용자를 팔로우합니다.
     */
    @Transactional
    public FollowResponse follow(String username, Long followerId) {
        User following = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 자기 자신 팔로우 방지
        if (following.getId().equals(follower.getId())) {
            throw new CustomException(ErrorCode.CANNOT_FOLLOW_SELF);
        }

        // 이미 팔로우 중인지 체크
        if (followRepository.existsByFollowerIdAndFollowingId(follower.getId(), following.getId())) {
            throw new CustomException(ErrorCode.ALREADY_FOLLOWING);
        }

        Follow follow = Follow.builder()
                .follower(follower)
                .following(following)
                .build();

        followRepository.save(follow);
        followRepository.flush(); // 즉시 DB 반영하여 카운트 정합성 확보

        return getFollowCounts(following.getId(), true);
    }

    /**
     * 사용자를 언팔로우합니다.
     */
    @Transactional
    public FollowResponse unfollow(String username, Long followerId) {
        // 대상 유저 조회
        User following = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 팔로우 관계 존재 확인
        Follow follow = followRepository
                .findByFollowerIdAndFollowingId(followerId, following.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOLLOWING));

        followRepository.delete(follow);
        followRepository.flush(); // 삭제 내용을 즉시 반영해야 getFollowCounts에서 정확한 숫자가 나옵니다.

        return getFollowCounts(following.getId(), false);
    }

    /**
     * 특정 사용자의 팔로워 목록을 조회합니다.
     */
    public List<UserResponse> getFollowers(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return followRepository.findFollowersByFollowingId(user.getId()).stream()
                .map(follow -> UserResponse.from(follow.getFollower()))
                .toList();
    }

    /**
     * 특정 사용자가 팔로잉하는 목록을 조회합니다.
     */
    public List<UserResponse> getFollowings(String username) {
        User user  = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return followRepository.findFollowingsByFollowerId(user.getId()).stream()
                .map(follow -> UserResponse.from(follow.getFollowing()))
                .toList();
    }
}