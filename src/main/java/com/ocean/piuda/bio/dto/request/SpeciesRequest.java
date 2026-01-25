package com.ocean.piuda.bio.dto.request;

import com.ocean.piuda.bio.enums.BioGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SpeciesRequest {

    public record Create(
            @NotBlank(message = "종 이름은 필수입니다")
            String name,

            @NotNull(message = "생물 그룹 분류는 필수입니다")
            BioGroup category
    ) {}

    public record Update(
            String name,
            BioGroup category
    ) {}
}