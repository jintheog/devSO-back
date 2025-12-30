package com.example.devso.controller;

import com.example.devso.dto.request.ProfileUpdateRequest;
import com.example.devso.dto.response.UserResponse;
import com.example.devso.security.CustomUserDetails;
import com.example.devso.dto.request.PasswordChangeRequest;
import com.example.devso.dto.response.ApiResponse;
import com.example.devso.dto.response.UserProfileResponse;
import com.example.devso.service.FollowService;
import com.example.devso.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "User", description = "사용자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final FollowService followService;


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


    /**
     * 프로필 조회 (username 기준)
     * GET /api/users/{username}
     */
    @Operation(summary = "프로필 검색")
    @GetMapping("/{username}")
    public ResponseEntity<UserProfileResponse> getProfile(@PathVariable String username) {
        // ID가 아닌 username으로 조회하는 서비스 로직 호출
        UserProfileResponse response = userService.getUserProfileByUsername(username);
        return ResponseEntity.ok(response);
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

    /**
     * 팔로우 실행
     * POST /api/users/{username}/follow
     */
    @Operation(summary = "사용자 팔로우")
    @PostMapping("/{username}/follow") // 👈 api.js의 follow 함수와 매핑됨
    public ResponseEntity<ApiResponse<com.example.devso.dto.response.FollowResponse>> follow(
            @PathVariable String username,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 현재 로그인한 유저의 ID를 followerId로 전달
        var response = followService.follow(username, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 팔로우 취소
     * DELETE /api/users/{username}/follow
     */
    @Operation(summary = "사용자 언팔로우")
    @DeleteMapping("/{username}/follow") // 👈 api.js의 unfollow 함수와 매핑됨
    public ResponseEntity<ApiResponse<com.example.devso.dto.response.FollowResponse>> unfollow(
            @PathVariable String username,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var response = followService.unfollow(username, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }



}