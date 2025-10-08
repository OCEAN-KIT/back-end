package com.ocean.piuda.aws.controller;

import com.ocean.piuda.aws.dto.response.GeneratePresignedPutUrlResponse;
import com.ocean.piuda.aws.service.S3Service;
import com.ocean.piuda.global.api.dto.ApiData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/image")
@RestController
@RequiredArgsConstructor
@Tag(
        name = "Image",
        description = "이미지 업로드 관련 API입니다. 클라이언트가 S3에 직접 PUT 업로드할 수 있도록 Presigned URL을 발급합니다."
)
public class ImageController {

    private final S3Service s3Service;

    @GetMapping("/presigned-put-url")
    @Operation(
            summary = "S3 Presigned PUT URL 발급",
            description = """
                확장자(예: jpg, png, webp 등)를 입력하면 S3에 직접 업로드 가능한 Presigned PUT URL을 발급합니다.
                응답의 key는 업로드된 객체의 S3 Key이며, uploadUrl로 해당 파일을 PUT 업로드하면 됩니다.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공적으로 Presigned URL 발급됨",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GeneratePresignedPutUrlResponse.class),
                            examples = @ExampleObject(
                                    name = "성공 예시",
                                    value = """
                    {
                      "success": true,
                      "data": {
                        "uploadUrl": "https://pre-piuda.s3.ap-northeast-2.amazonaws.com/public/user_objects/2025-09-24/1f535333d9b5_1758707009522.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20250924T094329Z&X-Amz-SignedHeaders=host&X-Amz-Credential=...&X-Amz-Expires=1800&X-Amz-Signature=...",
                        "key": "public/user_objects/2025-09-24/1f535333d9b5_1758707009522.jpg",
                        "expirationSeconds": 1800
                      }
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 파라미터 오류(허용되지 않는 확장자 등)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "잘못된 확장자 예시",
                                    value = """
                    {
                      "success": false,
                      "error": {
                        "code": "INVALID_EXTENSION",
                        "message": "허용되지 않는 확장자입니다."
                      }
                    }
                    """
                            )
                    )
            )
    })
    public ApiData<GeneratePresignedPutUrlResponse> generatePresignedPutUrl(
            @Parameter(
                    description = "파일 확장자입니다. 점(.) 없이 입력합니다. 예: jpg, png, webp",
                    example = "jpg",
                    required = true
            )
            @RequestParam String extension
    ) {
        return ApiData.ok(s3Service.generatePresignedPutUrl(extension));
    }
}
