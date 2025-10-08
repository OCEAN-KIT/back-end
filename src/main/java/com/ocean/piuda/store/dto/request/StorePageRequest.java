package com.ocean.piuda.store.dto.request;

import com.ocean.piuda.global.api.dto.PageRequest;
import com.ocean.piuda.store.enums.StoreCategory;
import com.ocean.piuda.store.enums.StoreSortType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StorePageRequest extends PageRequest<StoreSortType> {

    private StoreCategory category = StoreCategory.한식;

    public StorePageRequest() {
        this.setPage(1);
        this.setSize(10);
        this.setSort(StoreSortType.LATEST);
    }
}
