package com.ocean.piuda.user.dto.request;

import lombok.Builder;

@Builder
public record UserSearchRequest(
        String q,
        Integer page,
        Integer size
) {

    public int pageOrDefault() { return page == null ? 0 : Math.max(0, page); }
    public int sizeOrDefault() { return size == null ? 20 : Math.max(1, size); }
    public String qOrEmpty()    { return q == null ? "" : q; }
}