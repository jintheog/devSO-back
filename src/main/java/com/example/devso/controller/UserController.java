package com.example.devso.controller;

import com.example.devso.dto.request.ProfileUpdateRequest;
import com.example.devso.dto.response.FollowResponse;
import com.example.devso.dto.response.UserResponse;
import com.example.devso.repository.UserRepository;
import com.example.devso.security.CustomUserDetails;
import com.example.devso.dto.request.PasswordChangeRequest;
import com.example.devso.dto.response.ApiResponse;
import com.example.devso.dto.response.UserProfileResponse;
import com.example.devso.service.FollowService;
import com.example.devso.service.UserService;
import com.example.devso.service.recruit.GeminiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "User", description = "사용자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final FollowService followService;
    private final GeminiService geminiService;
    private final UserRepository userRepository;


    private boolean isAdmin(CustomUserDetails userDetails) {
        if (userDetails == null) {
            return false;
        }

        return userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN") || a.getAuthority().equals("ROLE_ADMIN"));
    }

    @Operation(summary = "사용자 검색")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserResponse>>> searchUsers(
            @Parameter(description = "검색어") @RequestParam("q") String query,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<UserResponse> response = userService.searchUsers(query, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }



    @Operation(summary = "비밀번호 변경")
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changeMyPassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PasswordChangeRequest request
    ) {
        String username = userDetails.getUsername();
        userService.changePassword(username, request);

        return ResponseEntity.noContent().build();
    }


    @Operation(summary = "프로필 조회")
    @GetMapping("/{username}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @PathVariable String username,
            @AuthenticationPrincipal CustomUserDetails userDetails // ✅ 현재 로그인 정보 가져오기
    ) {
        Long currentUserId = (userDetails != null) ? userDetails.getId() : null;
        UserProfileResponse response = userService.getUserProfileByUsername(username, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    /**
     * 프로필 업데이트 (username 기준)
     * PUT /api/users/{username}
     */
    @Operation(summary = "프로필 업데이트")
    @PutMapping("/{username}")
    public ResponseEntity<String> updateProfile(
            @PathVariable String username,
            @RequestBody ProfileUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // 1. 로그인 여부 확인 (시큐리티 설정으로 대체 가능)
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 2. 경로의 username과 로그인한 유저의 username이 일치하는지 검증 (보안)
        if (!userDetails.getUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("자신의 프로필만 수정할 수 있습니다.");
        }

        userService.updateFullProfileByUsername(username, request);
        return ResponseEntity.ok("프로필이 성공적으로 수정되었습니다.");
    }

    @PostMapping("/{username}/follow")
    public ResponseEntity<ApiResponse<FollowResponse>> follow(
            @PathVariable String username,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        FollowResponse response = followService.follow(username,  userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{username}/follow")
    public ResponseEntity<ApiResponse<FollowResponse>> unfollow(
            @PathVariable String username,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        FollowResponse response = followService.unfollow(username, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }



    // TODO: 팔로워 목록 조회 API 추가
    // GET /api/users/{username}/followers
    @GetMapping("/{username}/followers")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getFollowers(
            @PathVariable String username
    ) {
        List<UserResponse> response = followService.getFollowers(username);
        return ResponseEntity.ok(ApiResponse.success(response));
    }


    // TODO: 팔로잉 목록 조회 API 추가
    // GET /api/users/{username}/following
    @GetMapping("/{username}/following")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getFollowings(
            @PathVariable String username
    )  {
        List<UserResponse> response = followService.getFollowings(username);
        return  ResponseEntity.ok(ApiResponse.success(response));
    }


    @Operation(summary = "AI 자기소개 생성", description = "유저의 기술스택, 경력 등을 바탕으로 AI가 자기소개를 생성합니다.")
    @PostMapping("/{username}/ai-bio")
    public ResponseEntity<ApiResponse<String>> generateAiBio(
            @PathVariable String username,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // 1. 보안 검증: 로그인한 유저 본인인지 확인
        if (userDetails == null || !userDetails.getUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // 2. AI 서비스 호출 (이전에 만든 메서드 호출)
        // userDetails.getId()를 바로 사용할 수 있어 DB 조회를 줄일 수 있습니다.
        String aiGeneratedBio = geminiService.generatePersonalStatement(userDetails.getId());

        return ResponseEntity.ok(ApiResponse.success(aiGeneratedBio));
    }


    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        boolean isAvailable = !userService.existsByEmail(email);

        // 구조를 단순화해서 보냅니다.
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", isAvailable);

        // 반환값: { "available": true } 또는 { "available": false }
        return ResponseEntity.ok(response);
    }
}