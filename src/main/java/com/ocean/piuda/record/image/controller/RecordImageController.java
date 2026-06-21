package com.ocean.piuda.record.image.controller;

import com.ocean.piuda.aws.dto.response.GeneratePresignedPutUrlResponse;
import com.ocean.piuda.aws.service.S3Service;
import com.ocean.piuda.global.api.dto.ApiData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/record/images")
@RequiredArgsConstructor
@Tag(
        name = "Record Image",
        description = "record 앱에서 사용하는 이미지 업로드 Presigned URL 발급 API입니다."
)
public class RecordImageController {

    private final S3Service s3Service;

    @GetMapping("/presigned-put-url")
    @Operation(
            summary = "S3 Presigned PUT URL 발급",
            description = "확장자를 입력하면 S3 직접 업로드용 Presigned PUT URL을 발급합니다."
    )
    public ApiData<GeneratePresignedPutUrlResponse> generatePresignedPutUrl(
            @RequestParam String extension
    ) {
        return ApiData.ok(s3Service.generatePresignedPutUrl(extension));
    }
}
