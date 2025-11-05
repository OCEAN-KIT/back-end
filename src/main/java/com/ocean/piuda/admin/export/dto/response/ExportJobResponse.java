package com.ocean.piuda.admin.export.dto.response;

import com.ocean.piuda.admin.common.enums.ExportFormat;
import com.ocean.piuda.admin.common.enums.ExportStatus;
import com.ocean.piuda.admin.export.entity.ExportJob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportJobResponse {
    private Long jobId;
    private String requestedBy;
    private ExportFormat format;
    private ExportStatus status;
    private String downloadUrl;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String filtersJson;

    public static ExportJobResponse from(ExportJob exportJob) {
        return ExportJobResponse.builder()
                .jobId(exportJob.getJobId())
                .requestedBy(exportJob.getRequestedBy())
                .format(exportJob.getFormat())
                .status(exportJob.getStatus())
                .downloadUrl(exportJob.getDownloadUrl())
                .createdAt(exportJob.getCreatedAt())
                .completedAt(exportJob.getCompletedAt())
                .filtersJson(exportJob.getFiltersJson())
                .build();
    }
}
