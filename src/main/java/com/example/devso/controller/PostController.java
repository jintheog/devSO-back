package com.example.devso.controller;

import com.example.devso.dto.request.CommentCreateRequest;
import com.example.devso.dto.request.PostCreateRequest;
import com.example.devso.dto.response.ApiResponse;
import com.example.devso.dto.response.CommentResponse;
import com.example.devso.dto.response.LikeResponse;
import com.example.devso.dto.response.PostResponse;
import com.example.devso.repository.CommentRepository;
import com.example.devso.repository.PostRepository;
import com.example.devso.security.CustomUserDetails;
import com.example.devso.service.CommentService;
import com.example.devso.service.PostLikeService;
import com.example.devso.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

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
        PostResponse response = postService.findById(id, userDetails.getId());
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
