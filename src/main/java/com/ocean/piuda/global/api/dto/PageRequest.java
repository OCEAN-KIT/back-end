package com.ocean.piuda.global.api.dto;

import com.ocean.piuda.global.enums.SortEnum;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Data
public class PageRequest<T extends Enum<T> & SortEnum> {
    
    @Min(value = 1, message = "페이지는 1부터 시작합니다.")
    private int page = 1;
    
    @Min(value = 1, message = "사이즈는 1 이상이어야 합니다.")
    private int size = 10;
    
    @NotNull
    private T sort;
    
    public PageRequest() {}
    
    public PageRequest(int page, int size, T sort) {
        this.page = page;
        this.size = size;
        this.sort = sort;
    }
    
    public Pageable toPageable() {
        // 1-based page를 0-based로 변환
        Sort springSort = sort.toSort();
        return org.springframework.data.domain.PageRequest.of(page - 1, size, springSort);
    }

    public Pageable toPageableWithoutSort() {
        return org.springframework.data.domain.PageRequest.of(page - 1, size);
    }
}
