package com.ocean.piuda.dashboard.dto.request;

import com.ocean.piuda.dashboard.enums.MediaCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateMediaLogRequest(
        @NotNull LocalDate recordDate,
        @NotBlank String mediaUrl,
        String caption,
        @NotNull MediaCategory category
) {}
