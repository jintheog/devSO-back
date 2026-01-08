package com.example.devso.repository.recruit;

import com.example.devso.entity.recruit.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecruitRepository extends JpaRepository<Recruit, Long> {

    // 전체 조회 시 Fetch Join 추가
    @Query("SELECT DISTINCT r FROM Recruit r JOIN FETCH r.user ORDER BY r.createdAt DESC")
    List<Recruit> findAllWithUser();

    // 모집글 상세 조회
    @Query("SELECT r FROM Recruit r JOIN FETCH r.user WHERE r.id = :id")
    Optional<Recruit> findByIdWithDetails(@Param("id") Long id);

    // 모집글 필터링 조회 (페이징 지원 및 내 글/북마크 필터 통합)
    @Query(value = """
        SELECT DISTINCT r FROM Recruit r
        JOIN FETCH r.user u
        LEFT JOIN r.recruitBookMarks rb ON rb.user.id = :currentUserId
        WHERE (:type IS NULL OR r.type = :type)
          AND (
                :search IS NULL OR :search = ''
                OR r.title LIKE %:search%
                OR r.content LIKE %:search%
                OR r.user.name LIKE %:search%
              )
          AND (:stacks IS NULL OR EXISTS (SELECT 1 FROM r.stacks s WHERE s IN :stacks))
          AND (:position IS NULL OR :position MEMBER OF r.positions)
          AND (:progressType IS NULL OR r.progressType = :progressType)
          AND (:onlyOpen = false OR r.deadLine >= CURRENT_DATE)
          AND (:onlyMyRecruits = false OR u.username = :currentUsername)
          AND (:onlyBookmarked = false OR rb.user.id = :currentUserId)
        """,
            countQuery = """
        SELECT COUNT(DISTINCT r) FROM Recruit r
        LEFT JOIN r.recruitBookMarks rb ON rb.user.id = :currentUserId
        WHERE (:type IS NULL OR r.type = :type)
          AND (
                :search IS NULL OR :search = ''
                OR r.title LIKE %:search%
                OR r.content LIKE %:search%
                OR r.user.name LIKE %:search%
              )
          AND (:progressType IS NULL OR r.progressType = :progressType)
          AND (:onlyOpen = false OR r.deadLine >= CURRENT_DATE)
          AND (:onlyMyRecruits = false OR r.user.username = :currentUsername)
          AND (:onlyBookmarked = false OR rb.user.id = :currentUserId)
        """)
    Page<Recruit> findRecruitsByFilters(
            @Param("type") RecruitType type,
            @Param("search") String search,
            @Param("stacks") List<TechStack> stacks,
            @Param("position") RecruitPosition position,
            @Param("progressType") RecruitProgressType progressType,
            @Param("onlyOpen") boolean onlyOpen,
            @Param("onlyBookmarked") boolean onlyBookmarked,
            @Param("onlyMyRecruits") boolean onlyMyRecruits,
            @Param("currentUserId") Long currentUserId,
            @Param("currentUsername") String currentUsername,
            Pageable pageable);
}
