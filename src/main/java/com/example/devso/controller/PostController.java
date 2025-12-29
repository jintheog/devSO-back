package com.example.devso.controller;

import com.example.devso.dto.request.CommentCreateRequest;
import com.example.devso.dto.request.PostCreateRequest;
import com.example.devso.dto.request.PostUpdateRequest;
import com.example.devso.dto.response.ApiResponse;
import com.example.devso.dto.response.CommentResponse;
import com.example.devso.dto.response.LikeResponse;
import com.example.devso.dto.response.PostResponse;
import com.example.devso.dto.response.ViewCountResponse;
import com.example.devso.repository.CommentRepository;
import com.example.devso.repository.PostRepository;
import com.example.devso.security.CustomUserDetails;
import com.example.devso.service.CommentService;
import com.example.devso.service.PostLikeService;
import com.example.devso.service.PostService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final CommentService commentService;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeService postLikeService;

    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PostCreateRequest request
    ) {

        PostResponse response = postService.create(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));

    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PostResponse>>> findAll(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        Long userId = userDetails != null ? userDetails.getId() : null;
        Page<PostResponse> posts = postService.findAll(userId, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(posts));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> findById(@PathVariable Long id,
                                                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails != null ? userDetails.getId() : null;
        PostResponse response = postService.findById(id, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/view")
    public ResponseEntity<ApiResponse<ViewCountResponse>> recordView(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String effectiveViewerKey = userDetails != null
                ? "u:" + userDetails.getId()
                : buildAnonymousViewerKey(request, response);

        ViewCountResponse viewCountResponse = postService.recordView(id, effectiveViewerKey);
        return ResponseEntity.ok(ApiResponse.success(viewCountResponse));
    }

    private static final String VIEW_COOKIE_NAME = "devso_vid";

    private String buildAnonymousViewerKey(HttpServletRequest request, HttpServletResponse response) {
        String ip = getClientIp(request);
        String vid = getOrSetViewerCookie(request, response);
        return sha256Hex(ip + ":" + vid);
    }

    private String getOrSetViewerCookie(HttpServletRequest request, HttpServletResponse response) {
        String existing = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (VIEW_COOKIE_NAME.equals(c.getName())) {
                    existing = c.getValue();
                    break;
                }
            }
        }

        if (existing != null && !existing.isBlank()) {
            return existing;
        }

        String vid = UUID.randomUUID().toString();
        Cookie cookie = new Cookie(VIEW_COOKIE_NAME, vid);
        cookie.setPath("/");
        cookie.setHttpOnly(false); // 프론트에서 생성/확인도 가능하게
        cookie.setMaxAge(60 * 60 * 24 * 365); // 1년
        response.addCookie(cookie);
        return vid;
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            // 첫 번째 IP가 원본
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(dig.length * 2);
            for (byte b : dig) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> update(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PostUpdateRequest request
    ) {
        PostResponse response = postService.update(id, userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        postService.delete(id, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CommentCreateRequest request
    ) {
        CommentResponse response = commentService.create(id, userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(
            @PathVariable Long id
    ) {
        List<CommentResponse> response = commentService.findByPostId(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        commentService.delete(commentId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<ApiResponse<LikeResponse>> like(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        LikeResponse response = postLikeService.like( userDetails.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}/like")
    public ResponseEntity<ApiResponse<LikeResponse>> unlike(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        LikeResponse response = postLikeService.unlike( userDetails.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));

    }

    @GetMapping("/{id}/like")
    public ResponseEntity<ApiResponse<LikeResponse>> getLikeStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        long userId = userDetails != null ? userDetails.getId() : null;
        LikeResponse response = postLikeService.getLikeStatus(id, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }


}
