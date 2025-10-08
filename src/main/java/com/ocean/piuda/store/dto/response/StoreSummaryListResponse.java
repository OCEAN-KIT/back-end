package com.ocean.piuda.store.dto.response;

import java.util.List;

public record StoreSummaryListResponse(
        List<StoreSummaryResponse> stores
) {

    public static StoreSummaryListResponse of(List<StoreSummaryResponse> stores) {
        return new StoreSummaryListResponse(stores);
    }
}
