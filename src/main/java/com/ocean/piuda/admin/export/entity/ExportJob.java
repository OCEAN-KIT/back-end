package com.ocean.piuda.admin.export.entity;

import com.ocean.piuda.admin.common.enums.ExportFormat;
import com.ocean.piuda.admin.common.enums.ExportStatus;
import com.ocean.piuda.global.api.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "export_job")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ExportJob extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "requested_by", nullable = false, length = 100)
    private String requestedBy;

    @Column(name = "filters_json", columnDefinition = "JSON")
    private String filtersJson;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ExportFormat format = ExportFormat.CSV;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ExportStatus status = ExportStatus.PROCESSING;

    @Column(name = "download_url", length = 500)
    private String downloadUrl;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
