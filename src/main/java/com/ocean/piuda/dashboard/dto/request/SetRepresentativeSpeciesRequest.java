package com.ocean.piuda.dashboard.dto.request;

import jakarta.validation.constraints.NotNull;

public record SetRepresentativeSpeciesRequest(
        @NotNull(message = "종 ID는 필수입니다.")
        Long speciesId
) {}