package com.ocean.piuda.bio.dto.request;

import jakarta.validation.constraints.NotBlank;

public class SpeciesRequest {

    public record Create(
            @NotBlank(message = "종 이름은 필수입니다")
            String name
    ) {}

    public record Update(
            String name
    ) {}
}