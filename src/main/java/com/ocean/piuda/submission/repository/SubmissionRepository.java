package com.ocean.piuda.submission.repository;

import com.ocean.piuda.submission.enums.ActivityType;
import com.ocean.piuda.submission.enums.SubmissionStatus;
import com.ocean.piuda.submission.entity.Submission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    long countByStatus(SubmissionStatus status);

    @Query("""
        SELECT DISTINCT s FROM Submission s
        LEFT JOIN FETCH s.basicEnv
        LEFT JOIN FETCH s.attachments
        LEFT JOIN FETCH s.rejectReason
        LEFT JOIN FETCH s.activityTransplant
        LEFT JOIN FETCH s.activityGrazerRemoval
        LEFT JOIN FETCH s.activitySubstrateImprovement
        LEFT JOIN FETCH s.activityMonitoring
        LEFT JOIN FETCH s.activityMarineCleanup
        WHERE s.submissionId = :id
        """)
    Optional<Submission> findByIdWithDetails(@Param("id") Long id);

    @Query("""
        SELECT DISTINCT s FROM Submission s
        LEFT JOIN FETCH s.auditLogs
        WHERE s.submissionId = :id
        """)
    Optional<Submission> findByIdWithAuditLogs(@Param("id") Long id);

    @Query(
            value = """
            SELECT DISTINCT s FROM Submission s
            LEFT JOIN FETCH s.basicEnv
            WHERE s.user.id = :userId
            """,
            countQuery = """
            SELECT COUNT(s) FROM Submission s
            WHERE s.user.id = :userId
            """
    )
    Page<Submission> findAllByUserIdWithBasicEnv(
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query("""
        SELECT DISTINCT s FROM Submission s
        LEFT JOIN FETCH s.basicEnv
        LEFT JOIN FETCH s.attachments
        LEFT JOIN FETCH s.rejectReason
        LEFT JOIN FETCH s.activityTransplant
        LEFT JOIN FETCH s.activityGrazerRemoval
        LEFT JOIN FETCH s.activitySubstrateImprovement
        LEFT JOIN FETCH s.activityMonitoring
        LEFT JOIN FETCH s.activityMarineCleanup
        WHERE s.submissionId = :id
          AND s.user.id = :userId
        """)
    Optional<Submission> findByIdAndUserIdWithDetails(
            @Param("id") Long id,
            @Param("userId") Long userId
    );

    @Query("""
    SELECT DISTINCT s FROM Submission s
    LEFT JOIN FETCH s.basicEnv
    WHERE (:keyword IS NULL OR :keyword = '' OR
           s.siteName LIKE %:keyword% OR
           s.authorName LIKE %:keyword%)
      AND (:status IS NULL OR s.status = :status)
      AND (:activityType IS NULL OR s.activityType = :activityType)
      AND (cast(:startDate as timestamp) IS NULL OR s.submittedAt >= :startDate)
      AND (cast(:endDate as timestamp) IS NULL OR s.submittedAt <= :endDate)
    """)
    Page<Submission> findWithFilters(
            @Param("keyword") String keyword,
            @Param("status") SubmissionStatus status,
            @Param("activityType") ActivityType activityType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query("""
        SELECT DISTINCT s FROM Submission s
        LEFT JOIN FETCH s.basicEnv
        LEFT JOIN FETCH s.attachments
        LEFT JOIN FETCH s.activityTransplant
        LEFT JOIN FETCH s.activityGrazerRemoval
        LEFT JOIN FETCH s.activitySubstrateImprovement
        LEFT JOIN FETCH s.activityMonitoring
        LEFT JOIN FETCH s.activityMarineCleanup
        WHERE s.status = :status
          AND s.submissionId IN :ids
        """)
    List<Submission> findAllByIdsAndStatusWithDetails(
            @Param("ids") List<Long> ids,
            @Param("status") SubmissionStatus status
    );

    @Query("""
    SELECT DISTINCT s FROM Submission s
    LEFT JOIN FETCH s.basicEnv
    LEFT JOIN FETCH s.attachments
    LEFT JOIN FETCH s.activityTransplant
    LEFT JOIN FETCH s.activityGrazerRemoval
    LEFT JOIN FETCH s.activitySubstrateImprovement
    LEFT JOIN FETCH s.activityMonitoring
    LEFT JOIN FETCH s.activityMarineCleanup
    WHERE s.status = :status
      AND s.submittedAt BETWEEN :start AND :end
    ORDER BY s.submittedAt DESC
    """)
    List<Submission> findAllByStatusAndSubmittedAtBetweenWithDetails(
            @Param("status") SubmissionStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}