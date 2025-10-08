package com.ocean.piuda.store.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoreSearchRequest extends StorePageRequest {
    private String query;
}
