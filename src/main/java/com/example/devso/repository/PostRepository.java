package com.example.devso.repository;

import com.example.devso.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 전체 게시물 조회
    @Query("SELECT p FROM Post p JOIN FETCH p.user ORDER BY p.createdAt")
    List<Post> findAllWithUser();

    // 최신(전체) 게시물 조회
    @Query("SELECT p FROM Post p JOIN FETCH p.user ORDER BY p.createdAt DESC")
    Page<Post> findAllWithUser(Pageable pageable);

    // 단일 게시물 조회
    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.id = :id")
    Optional<Post> findByIdWithUser(@Param("id") Long id);

    // 특정 사용자의 게시물 조회
    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.user.id = :userId")
    List<Post> findByUserIdWithUser(@Param("userId") Long userId);

    // 사용자별 게시물 수
    long countByUserId(Long userId);

    // 탐색
    @Query("SELECT p FROM Post p JOIN FETCH p.user ORDER BY p.createdAt DESC")
    Slice<Post> findAllWithUserPaging(Pageable pageable);

    // 피드 (내게시물 + 팔로잉게시물)
    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.user.id IN :userIds ORDER BY p.createdAt")
    Slice<Post> findByUserIdsWithUserPaging(@Param("userIds") List<Long> userIds, Pageable pageable);


}
