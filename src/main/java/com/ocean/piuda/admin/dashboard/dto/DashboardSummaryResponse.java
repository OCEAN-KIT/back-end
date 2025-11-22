package com.ocean.piuda.admin.dashboard.dto;

public record DashboardSummaryResponse(
        Long totalSubmissions,
        Long pending,
        Long approved,
        Long rejected,
        Long deleted
) {
    public static DashboardSummaryResponse of(
            long total,
            long pending,
            long approved,
            long rejected,
            long deleted
    ) {
        return new DashboardSummaryResponse(total, pending, approved, rejected, deleted);
    }
}
