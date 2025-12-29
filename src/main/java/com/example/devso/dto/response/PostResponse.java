package com.example.devso.dto.response;


import com.example.devso.entity.Post;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostResponse {

    private Long id;
    private String title;
    private String content;
    private String imageUrl;
    private long viewCount;
    private LocalDateTime createdAt;
    private UserResponse author;

    private boolean liked;
    private long likeCount;
    private long commentCount;

    public static PostResponse from(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .imageUrl(post.getImageUrl())
                .viewCount(post.getViewCount())
                .author(UserResponse.from(post.getUser()))
                .createdAt(post.getCreatedAt())
                .liked(false)
                .likeCount(0)
                .commentCount(0)
                .build();
    }

    public static PostResponse from(Post post, boolean liked, long likeCount, long commentCount) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .imageUrl(post.getImageUrl())
                .viewCount(post.getViewCount())
                .author(UserResponse.from(post.getUser()))
                .createdAt(post.getCreatedAt())
                .liked(liked)
                .likeCount(likeCount)
                .commentCount(commentCount)
                .build();
    }
}
