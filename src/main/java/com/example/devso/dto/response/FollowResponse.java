package com.example.devso.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FollowResponse {

    private boolean following;
    private long followerCount;
    private long followingCount;

    public static FollowResponse of (boolean following, long followerCount, long followingCount) {
        return FollowResponse.builder()
                .following(following)
                .followerCount(followerCount)
                .followingCount(followingCount)
                .build();
    }
}
