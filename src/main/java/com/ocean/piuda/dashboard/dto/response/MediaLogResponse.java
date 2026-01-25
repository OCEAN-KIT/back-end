package com.ocean.piuda.dashboard.dto.response;

import com.ocean.piuda.dashboard.entity.MediaLog;
import com.ocean.piuda.dashboard.enums.MediaCategory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class MediaLogResponse {
    private Long id;
    private LocalDate recordDate;
    private String mediaUrl;
    private String caption;
    private MediaCategory category;
    private String categoryName;

    public static MediaLogResponse from(MediaLog m) {
        return MediaLogResponse.builder()
                .id(m.getId())
                .recordDate(m.getRecordDate())
                .mediaUrl(m.getMediaUrl())
                .caption(m.getCaption())
                .category(m.getCategory())
                .categoryName(m.getCategory() != null ? m.getCategory().getName() : null)
                .build();
    }
}
