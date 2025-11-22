package com.ocean.piuda.admin.submission.dto.response;

import com.ocean.piuda.admin.common.enums.AuditAction;
import com.ocean.piuda.admin.submission.entity.AuditLog;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long logId,
        AuditAction action,
        String performedBy,
        String comment,
        LocalDateTime createdAt
) {
    public static AuditLogResponse from(AuditLog auditLog) {
        if (auditLog == null) return null;
        return new AuditLogResponse(
                auditLog.getLogId(),
                auditLog.getAction(),
                auditLog.getPerformedBy(),
                auditLog.getComment(),
                auditLog.getCreatedAt()
        );
    }
}
