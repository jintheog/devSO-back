package com.example.devso.repository;

import com.example.devso.entity.PostView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface PostViewRepository extends JpaRepository<PostView, Long> {
    @Modifying
    @Query(value = """
            INSERT IGNORE INTO post_view_trackers (post_id, viewer_key, last_viewed_at)
            VALUES (:postId, :viewerKey, :now)
            """, nativeQuery = true)
    int insertIgnore(
            @Param("postId") Long postId,
            @Param("viewerKey") String viewerKey,
            @Param("now") LocalDateTime now
    );

    @Modifying
    @Query("""
            UPDATE PostView v
            SET v.lastViewedAt = :now
            WHERE v.post.id = :postId
              AND v.viewerKey = :viewerKey
              AND v.lastViewedAt < :cutoff
            """)
    int touchIfExpired(
            @Param("postId") Long postId,
            @Param("viewerKey") String viewerKey,
            @Param("now") LocalDateTime now,
            @Param("cutoff") LocalDateTime cutoff
    );
}


