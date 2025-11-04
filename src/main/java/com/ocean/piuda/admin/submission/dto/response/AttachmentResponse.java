package com.ocean.piuda.admin.submission.dto.response;

import com.ocean.piuda.admin.submission.entity.Attachment;

import java.time.LocalDateTime;

public record AttachmentResponse(
        Long attachmentId,
        String fileName,
        String fileUrl,
        String mimeType,
        Integer fileSize,
        LocalDateTime uploadedAt
) {
    public static AttachmentResponse from(Attachment attachment) {
        if (attachment == null) return null;
        return new AttachmentResponse(
                attachment.getAttachmentId(),
                attachment.getFileName(),
                attachment.getFileUrl(),
                attachment.getMimeType(),
                attachment.getFileSize(),
                attachment.getUploadedAt()
        );
    }
}
