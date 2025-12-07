package com.ocean.piuda.admin.dashboard.dto;

public record AdminDashboardSummaryResponse(
        Long totalSubmissions,
        Long pending,
        Long approved,
        Long rejected,
        Long deleted
) {
    public static AdminDashboardSummaryResponse of(
            long total,
            long pending,
            long approved,
            long rejected,
            long deleted
    ) {
        return new AdminDashboardSummaryResponse(total, pending, approved, rejected, deleted);
    }
}
