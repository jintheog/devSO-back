package com.example.devso.service;


import com.example.devso.dto.request.CommentCreateRequest;
import com.example.devso.dto.request.CommentUpdateRequest;
import com.example.devso.dto.response.CommentResponse;
import com.example.devso.entity.Comment;
import com.example.devso.entity.CommentMention;
import com.example.devso.entity.Post;
import com.example.devso.entity.User;
import com.example.devso.exception.CustomException;
import com.example.devso.exception.ErrorCode;
import com.example.devso.repository.CommentMentionRepository;
import com.example.devso.repository.CommentRepository;
import com.example.devso.repository.PostRepository;
import com.example.devso.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentMentionRepository commentMentionRepository;

    //update + insert
    private void upsertMentions(Comment comment, List<Long> mentionedUserIds, boolean replaceIfProvided) {
        if (mentionedUserIds == null) {
            // update 요청에서 null이면 기존 유지
            if (replaceIfProvided) return;
            // create에서는 null -> 처리 없음
            return;
        }

        // replace mode: 기존 멘션 전체 삭제 후 재생성
        commentMentionRepository.deleteByCommentId(comment.getId());

        List<Long> deduped = mentionedUserIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (deduped.isEmpty()) return;

        List<User> users = userRepository.findAllById(deduped).stream()
                .filter(u -> u.getDeletedAt() == null)
                .toList();

        if (users.size() != deduped.size()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        List<CommentMention> mentions = users.stream()
                .map(u -> new CommentMention(comment, u))
                .toList();

        commentMentionRepository.saveAll(mentions);
    }

    @Transactional
    public CommentResponse create(
            Long postId,
            Long userId,
            CommentCreateRequest request
    ) {

        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Comment parent = null;
        if (request.getParentCommentId() != null) {
            parent = commentRepository.findByIdAndDeletedAtIsNull(request.getParentCommentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

            // 같은 게시물의 댓글만 대댓글로 허용
            if (parent.getPost() == null || !parent.getPost().getId().equals(postId)) {
                throw new CustomException(ErrorCode.COMMENT_NOT_FOUND);
            }

            // depth 2까지만 허용 (대댓글의 대댓글 금지)
            if (parent.getParentComment() != null) {
                throw new CustomException(ErrorCode.INVALID_INPUT);
            }
        }

        Comment comment = Comment.builder()
                .content(request.getContent())
                .post(post)
                .user(user)
                .parentComment(parent)
                .build();

        Comment saved = commentRepository.save(comment);
        // 멘션 저장(유저 참조 형태)
        upsertMentions(saved, request.getMentionedUserIds(), false);
        return CommentResponse.from(saved);
    }

    public List<CommentResponse> findByPostId(Long postId) {
        if (!postRepository.existsByIdAndDeletedAtIsNull(postId)) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }

        List<Comment> comments = commentRepository.findByPostIdWithUser(postId);
        return comments.stream()
                .map(CommentResponse::from)
                .toList();
    }

    @Transactional
    public void delete(Long commentId, Long userId) {
        Comment comment = commentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(()-> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if(!comment.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.NOT_COMMENT_OWNER);
        }

        // 부모 댓글 삭제 시, 대댓글도 함께 soft delete (요구사항: deletedAt is null만 조회되므로 thread 깨짐 방지)
        if (comment.getParentComment() == null) {
            commentRepository.softDeleteReplies(commentId, LocalDateTime.now());
        }

        comment.markDeleted();
        commentRepository.save(comment);

        // 멘션 레코드는 FK로 묶여있지만, 깔끔하게 정리
        commentMentionRepository.deleteByCommentId(commentId);
    }

    @Transactional
    public CommentResponse update(Long postId, Long commentId, Long userId, CommentUpdateRequest request) {
        Comment comment = commentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if (comment.getPost() == null || comment.getPost().getDeletedAt() != null) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }

        if (!comment.getPost().getId().equals(postId)) {
            throw new CustomException(ErrorCode.COMMENT_NOT_FOUND);
        }

        if (!comment.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.NOT_COMMENT_OWNER);
        }

        comment.updateContent(request.getContent());
        Comment saved = commentRepository.save(comment);

        // null이면 유지, 빈 배열이면 제거
        upsertMentions(saved, request.getMentionedUserIds(), true);
        return CommentResponse.from(saved);
    }



}
