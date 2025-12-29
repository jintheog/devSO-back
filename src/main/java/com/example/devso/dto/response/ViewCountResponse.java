package com.example.devso.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ViewCountResponse {
    private long viewCount;

    public static ViewCountResponse of(long viewCount) {
        return ViewCountResponse.builder().viewCount(viewCount).build();
    }
}


