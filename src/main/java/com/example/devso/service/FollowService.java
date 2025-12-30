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

    private FollowResponse getFollowCounts(Long userId, boolean isFollowing){
        long followerCount = followRepository.countByFollowingId(userId);
        long followingCount = followRepository.countByFollowerId(userId);

        return FollowResponse.of(isFollowing, followerCount, followingCount);
    }

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

        return getFollowCounts(following.getId(), true);

    }

    @Transactional
    public FollowResponse unfollow(String username, Long followerId) {
        // 대상 조회
        User following = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Follow follow = followRepository
                .findByFollowerIdAndFollowingId(followerId, following.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOLLOWING));

        followRepository.delete(follow);

        return getFollowCounts(following.getId(), false);
    }


    // 팔로워 목록
    public List<UserResponse> getFollowers(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return followRepository.findFollowersByFollowingId(user.getId()).stream()
                .map(follow -> UserResponse.from(follow.getFollower()))
                .toList();
    }

    // 팔로잉 목록
    public List<UserResponse> getFollowings(String username) {
        User user  = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return followRepository.findFollowingsByFollowerId(user.getId()).stream()
                .map(follow -> UserResponse.from(follow.getFollowing()))
                .toList();
    }






}

