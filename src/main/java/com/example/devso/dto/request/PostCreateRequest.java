package com.example.devso.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostCreateRequest {
    @NotBlank(message = "제목은 필수 입니다.")
    @Size(max = 255, message = "제목은 255자 까지")
    private String title;

    @NotBlank(message = "내용은 필수 입니다.")
    @Size(max = 2000000, message = "내용은 2000000자 까지")
    private String content;

    private String imageUrl;
}
