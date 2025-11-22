package com.ocean.piuda.admin.export.dto.request;

import com.ocean.piuda.admin.common.enums.ExportFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportRequest {
    
    @NotNull(message = "포맷은 필수입니다")
    private ExportFormat format;
    
    private ExportFilters filters;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExportFilters {
        private LocalDate dateFrom;
        private LocalDate dateTo;
    }
}
