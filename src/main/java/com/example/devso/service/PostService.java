package com.example.devso.service;

import com.example.devso.dto.request.PostCreateRequest;
import com.example.devso.dto.request.PostUpdateRequest;
import com.example.devso.dto.response.PostResponse;
import com.example.devso.dto.response.ViewCountResponse;
import com.example.devso.entity.Post;
import com.example.devso.entity.User;
import com.example.devso.exception.CustomException;
import com.example.devso.exception.ErrorCode;
import com.example.devso.repository.CommentRepository;
import com.example.devso.repository.PostLikeRepository;
import com.example.devso.repository.PostRepository;
import com.example.devso.repository.PostViewRepository;
import com.example.devso.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;
    private final PostViewRepository postViewRepository;

    @Transactional
    public PostResponse create(Long userId, PostCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .user(user)
                .build();

        Post saved = postRepository.save(post);
        return PostResponse.from(saved);

    }

    // 전체 게시물
    public Page<PostResponse> findAll(Long currentUserId, Pageable pageable) {
        Page<Post> posts = postRepository.findAllWithUser(pageable);
        return posts.map(post -> toPostResponseWithStats(post, currentUserId));
    }

    // 단일 게시물
    public PostResponse findById(Long postId, Long currentUserId) {
        Post post = postRepository.findByIdWithUser(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        return toPostResponseWithStats(post, currentUserId);
    }

    /**
     * "정식" 조회수 기록: GET에서 분리된 POST endpoint로 조회수 증가.
     *
     * 정책:
     * - 로그인: viewerKey = "u:{userId}"
     * - 비로그인: viewerKey = SHA-256(ip + ":" + cookieValue)
     * - 동일 글 24시간 1회만 증가
     */
    @Transactional
    public ViewCountResponse recordView(Long postId, String viewerKey) {
        if (viewerKey == null || viewerKey.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        // post 존재/삭제 여부 확인
        if (!postRepository.existsByIdAndDeletedAtIsNull(postId)) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoff = now.minusHours(24);

        // 1) 최초 조회: tracker insert 성공 시 +1
        // 2) 이미 tracker 존재: lastViewedAt < cutoff 인 경우에만 touch +1
        String key = viewerKey.trim();
        boolean shouldIncrement = false;

        // 이미 존재하면 24시간 경과 시에만 갱신
        int touched = postViewRepository.touchIfExpired(postId, key, now, cutoff);
        if (touched > 0) {
            shouldIncrement = true;
        } else {
            // 없으면 insert (중복이어도 IGNORE라 예외 없음)
            int inserted = postViewRepository.insertIgnore(postId, key, now);
            shouldIncrement = inserted > 0;
        }

        if (shouldIncrement) {
            postRepository.incrementViewCount(postId);
        }

        // 최신 값 반환 (bulk update 사용했으므로 다시 조회)
        long latest = postRepository.findByIdAndDeletedAtIsNull(postId)
                .map(Post::getViewCount)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        return ViewCountResponse.of(latest);
    }

    // 특정 사용자 게시물
    public List<PostResponse> findByUsername(String username, Long currentUserId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<Post> posts = postRepository.findByUserIdWithUser(user.getId());

        return posts.stream()
                .map(post -> toPostResponseWithStats(post, currentUserId))
                .toList();
    }

    @Transactional
    public PostResponse update(Long postId, Long userId, PostUpdateRequest request) {
        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        if(!post.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.NOT_POST_OWNER);
        }

        // 제목, 내용, 이미지 URL 업데이트
        post.updateTitle(request.getTitle());
        post.updateContent(request.getContent());
        post.updateImageUrl(request.getImageUrl());

        Post updated = postRepository.save(post);
        return toPostResponseWithStats(updated, userId);
    }


    @Transactional
    public void delete(Long postId, Long userId) {
        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        if(!post.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.NOT_POST_OWNER);
        }

        post.markDeleted();
        postRepository.save(post);

    }

    private PostResponse toPostResponseWithStats(Post post, Long currentUserId) {
        boolean liked = currentUserId != null
                && postLikeRepository.existsByUserIdAndPostId(currentUserId, post.getId());
        long likeCount = postLikeRepository.countByPostId(post.getId());
        long commentCount = commentRepository.countByPostIdAndDeletedAtIsNull(post.getId());

        return PostResponse.from(post, liked, likeCount, commentCount);

    }
}
