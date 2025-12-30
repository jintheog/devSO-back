package com.example.devso.dto.response;

import com.example.devso.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String name;
    private String profileImageUrl;


    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }
}
