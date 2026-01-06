package com.example.devso.repository;

import com.example.devso.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 전체 게시물 조회
    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.deletedAt IS NULL ORDER BY p.createdAt")
    List<Post> findAllWithUser();

    // 최신(전체) 게시물 조회
    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    Page<Post> findAllWithUser(Pageable pageable);

    // 최신(전체) 검색: 제목/내용/작성자(username/name)
    @Query(
            value = """
                    SELECT p FROM Post p
                    JOIN p.user u
                    WHERE p.deletedAt IS NULL
                      AND (
                        p.title LIKE CONCAT('%', :q, '%')
                        OR p.content LIKE CONCAT('%', :q, '%')
                        OR u.username LIKE CONCAT('%', :q, '%')
                        OR u.name LIKE CONCAT('%', :q, '%')
                      )
                    ORDER BY p.createdAt DESC
                    """,
            countQuery = """
                    SELECT COUNT(p) FROM Post p
                    JOIN p.user u
                    WHERE p.deletedAt IS NULL
                      AND (
                        p.title LIKE CONCAT('%', :q, '%')
                        OR p.content LIKE CONCAT('%', :q, '%')
                        OR u.username LIKE CONCAT('%', :q, '%')
                        OR u.name LIKE CONCAT('%', :q, '%')
                      )
                    """
    )
    Page<Post> searchAll(@Param("q") String q, Pageable pageable);

    // 단일 게시물 조회
    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<Post> findByIdWithUser(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id AND p.deletedAt IS NULL")
    int incrementViewCount(@Param("id") Long id);

    // 특정 사용자의 게시물 조회
    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.user.id = :userId AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<Post> findByUserIdWithUser(@Param("userId") Long userId);

    // 사용자별 게시물 수
    long countByUserId(Long userId);

    // 탐색
    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    Slice<Post> findAllWithUserPaging(Pageable pageable);

    // 피드 (내게시물 + 팔로잉게시물)
    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.user.id IN :userIds AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    Slice<Post> findByUserIdsWithUserPaging(@Param("userIds") List<Long> userIds, Pageable pageable);

    // 피드(페이지 응답용)
    @Query(
            value = "SELECT p FROM Post p JOIN p.user u WHERE p.user.id IN :userIds AND p.deletedAt IS NULL ORDER BY p.createdAt DESC",
            countQuery = "SELECT COUNT(p) FROM Post p WHERE p.user.id IN :userIds AND p.deletedAt IS NULL"
    )
    Page<Post> findByUserIdsPage(@Param("userIds") List<Long> userIds, Pageable pageable);

    // 피드 검색: 제목/내용/작성자(username/name)
    @Query(
            value = """
                    SELECT p FROM Post p
                    JOIN p.user u
                    WHERE p.user.id IN :userIds
                      AND p.deletedAt IS NULL
                      AND (
                        p.title LIKE CONCAT('%', :q, '%')
                        OR p.content LIKE CONCAT('%', :q, '%')
                        OR u.username LIKE CONCAT('%', :q, '%')
                        OR u.name LIKE CONCAT('%', :q, '%')
                      )
                    ORDER BY p.createdAt DESC
                    """,
            countQuery = """
                    SELECT COUNT(p) FROM Post p
                    JOIN p.user u
                    WHERE p.user.id IN :userIds
                      AND p.deletedAt IS NULL
                      AND (
                        p.title LIKE CONCAT('%', :q, '%')
                        OR p.content LIKE CONCAT('%', :q, '%')
                        OR u.username LIKE CONCAT('%', :q, '%')
                        OR u.name LIKE CONCAT('%', :q, '%')
                      )
                    """
    )
    Page<Post> searchFeed(@Param("userIds") List<Long> userIds, @Param("q") String q, Pageable pageable);

    // 트렌딩: 최근 24시간 + (좋아요*3 + 댓글*2 + 조회수) 기준 정렬
    @Query(
            value = """
                    SELECT p FROM Post p
                    LEFT JOIN p.likes l
                    LEFT JOIN p.comments c WITH c.deletedAt IS NULL
                    WHERE p.deletedAt IS NULL
                      AND p.createdAt >= :cutoff
                    GROUP BY p
                    ORDER BY (COUNT(DISTINCT l.id) * 3 + COUNT(DISTINCT c.id) * 2 + p.viewCount) DESC,
                             p.createdAt DESC
                    """,
            countQuery = """
                    SELECT COUNT(p) FROM Post p
                    WHERE p.deletedAt IS NULL
                      AND p.createdAt >= :cutoff
                    """
    )
    Page<Post> findTrending(@Param("cutoff") LocalDateTime cutoff, Pageable pageable);

    // 트렌딩(전체 기간): (좋아요*3 + 댓글*2 + 조회수) 기준 정렬
    @Query(
            value = """
                    SELECT p FROM Post p
                    LEFT JOIN p.likes l
                    LEFT JOIN p.comments c WITH c.deletedAt IS NULL
                    WHERE p.deletedAt IS NULL
                    GROUP BY p
                    ORDER BY (COUNT(DISTINCT l.id) * 3 + COUNT(DISTINCT c.id) * 2 + p.viewCount) DESC,
                             p.createdAt DESC
                    """,
            countQuery = """
                    SELECT COUNT(p) FROM Post p
                    WHERE p.deletedAt IS NULL
                    """
    )
    Page<Post> findTrendingAllTime(Pageable pageable);

    Optional<Post> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByIdAndDeletedAtIsNull(Long id);

}
