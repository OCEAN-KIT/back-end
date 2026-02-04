package com.ocean.piuda.admin.site.dto.response;

import com.ocean.piuda.admin.site.entity.SiteNameOption;

public record SiteNameOptionResponse(
        Long id,
        String name
) {
    public static SiteNameOptionResponse from(SiteNameOption entity) {
        return new SiteNameOptionResponse(entity.getId(), entity.getName());
    }
}