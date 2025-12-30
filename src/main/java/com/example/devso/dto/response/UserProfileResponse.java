package com.example.devso.dto.response;

import com.example.devso.entity.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private Long id;
    private String username;
    private String name;
    private String email;
    private String phone;
    private String bio;
    private String profileImageUrl;
    private String portfolio;
    private Role role;
    private long followerCount;
    private long followingCount;

    @JsonProperty("isFollowing")
    private boolean isFollowing;

    // 하위 리스트 DTO
    private List<CareerDto> careers;
    private List<EducationDto> educations;
    private List<CertiDto> certis;
    private List<ActivityDto> activities;
    private List<SkillDto> skills; // 1. SkillDto 리스트 필드 추가

    public static UserProfileResponse from(User user, long followerCount, long followingCount, boolean isFollowing) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .bio(user.getBio())
                .profileImageUrl(user.getProfileImageUrl())
                .portfolio(user.getPortfolio())
                .role(user.getRole())
                .careers(user.getCareers().stream().map(CareerDto::from).collect(Collectors.toList()))
                .educations(user.getEducations().stream().map(EducationDto::from).collect(Collectors.toList()))
                .certis(user.getCertis().stream().map(CertiDto::from).collect(Collectors.toList()))
                .activities(user.getActivities().stream().map(ActivityDto::from).collect(Collectors.toList()))
                // 2. Skill 엔티티 리스트를 DTO로 변환하여 매핑
                .skills(user.getSkills() != null ?
                        user.getSkills().stream().map(SkillDto::from).collect(Collectors.toList()) : null)
                .followerCount(followerCount)
                .followingCount(followingCount)
                .isFollowing(isFollowing)
                .build();
    }

    // --- 내부 DTO 클래스들 (조회용) ---

    // 3. Skill 조회를 위한 내부 DTO 클래스 추가
    @Getter
    @Builder
    public static class SkillDto {
        private String name;
        private String level;

        public static SkillDto from(Skill skill) {
            return SkillDto.builder()
                    .name(skill.getName())
                    .level(skill.getLevel())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class CareerDto {
        private String companyName;
        private String department;
        private String position;
        private String startDate;
        private String endDate;
        private String task;

        public static CareerDto from(Career career) {
            return CareerDto.builder()
                    .companyName(career.getCompanyName())
                    .department(career.getDepartment())
                    .position(career.getPosition())
                    .startDate(career.getStartDate())
                    .endDate(career.getEndDate())
                    .task(career.getTask())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class EducationDto {
        private String schoolName;
        private String major;
        private String startDate;
        private String endDate;

        public static EducationDto from(Education education) {
            return EducationDto.builder()
                    .schoolName(education.getSchoolName())
                    .major(education.getMajor())
                    .startDate(education.getStartDate())
                    .endDate(education.getEndDate())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class CertiDto {
        private String certiName;
        private String issuer;
        private String acquisitionDate;

        public static CertiDto from(Certi certi) {
            return CertiDto.builder()
                    .certiName(certi.getCertiName())
                    .issuer(certi.getIssuer())
                    .acquisitionDate(certi.getAcquisitionDate())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class ActivityDto {
        private String category;
        private String projectName;
        private String duration;
        private String content;

        public static ActivityDto from(Activity activity) {
            return ActivityDto.builder()
                    .category(activity.getCategory())
                    .projectName(activity.getProjectName())
                    .duration(activity.getDuration())
                    .content(activity.getContent())
                    .build();
        }
    }
}