package com.ocean.piuda.admin.export.dto.request;

import com.ocean.piuda.admin.common.enums.ExportFormat;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportByIdsRequest {

    @NotNull(message = "포맷은 필수입니다")
    private ExportFormat format;

    @NotEmpty(message = "ids는 1개 이상이어야 합니다")
    private List<Long> ids;

}
