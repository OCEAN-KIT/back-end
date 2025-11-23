package com.ocean.piuda.admin.submission.repository;

import com.ocean.piuda.admin.submission.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findBySubmissionSubmissionIdOrderByCreatedAtDesc(Long submissionId);
}
