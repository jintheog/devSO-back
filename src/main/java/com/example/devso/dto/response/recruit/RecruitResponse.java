package com.example.devso.dto.response.recruit;

import com.example.devso.entity.recruit.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class RecruitResponse {
    private Long id;
    private String title;
    private String content;
    private String imageUrl;
    private int totalCount;
    private int currentCount;
    private RecruitType type;
    private RecruitStatus status;
    private List<RecruitPosition> positions; // 다중 선택 가능
    private RecruitProgressType progressType;
    private List<StackResponse> stacks;
    private String username;
    private String name;
    private String profileImageUrl;
    private RecruitDuration duration;          // 진행 기간
    private RecruitContactMethod contactMethod; // 연락 방법
    private String contactInfo;                // 실제 이메일/링크/전화번호

    private boolean bookmarked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDate deadLine;
    private long viewCount;
    private long commentCount;
    private long bookmarkCount;

    // 기본 from 메서드 (북마크 false)
    public static RecruitResponse from(Recruit recruit){
        return from(recruit, false);
    }

    // 북마크 여부 포함 from 메서드
    public static RecruitResponse from(Recruit recruit, boolean bookmarked){
        return RecruitResponse.builder()
                .id(recruit.getId())
                .title(recruit.getTitle())
                .content(recruit.getContent())
                .imageUrl(recruit.getImageUrl())
                .totalCount(recruit.getTotalCount())
                .currentCount(recruit.getCurrentCount())
                .type(recruit.getType())
                .status(recruit.getStatus())
                .positions(recruit.getPositions())
                .progressType(recruit.getProgressType())
                // Enum 리스트를 StackResponse DTO 리스트로 변환하는 스트림 로직
                .stacks(recruit.getStacks().stream()
                        .map(StackResponse::from)
                        .collect(Collectors.toList()))
                .duration(recruit.getDuration())
                .contactMethod(recruit.getContactMethod())
                .contactInfo(recruit.getContactInfo())
                .createdAt(recruit.getCreatedAt())
                .updatedAt(recruit.getUpdatedAt())
                .username(recruit.getUser().getUsername())
                .name(recruit.getUser().getName())
                .profileImageUrl(recruit.getUser().getProfileImageUrl())
                .deadLine(recruit.getDeadLine())
                .bookmarked(bookmarked)
                .viewCount(recruit.getViewCount())
                .commentCount(recruit.getCommentCount())
                .bookmarkCount(recruit.getRecruitBookMarks().size())
                .build();
    }
}