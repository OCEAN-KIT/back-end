package com.ocean.piuda.dashboard.entity;

import com.ocean.piuda.dashboard.enums.MediaCategory;
import com.ocean.piuda.global.api.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "media_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MediaLog extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable=false)
    @Setter
    private ProjectArea projectArea;

    @Column(nullable = false)
    private LocalDate recordDate;
    @Column(nullable = false)
    private String mediaUrl;
    private String caption; // 설명 (선택)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaCategory category;

    public void update(
            LocalDate recordDate,
            String mediaUrl,
            String caption,
            MediaCategory category
    ) {
        if (recordDate != null) this.recordDate = recordDate;
        if (mediaUrl != null) this.mediaUrl = mediaUrl;
        if (caption != null) this.caption = caption;
        if (category != null) this.category = category;
    }

}
