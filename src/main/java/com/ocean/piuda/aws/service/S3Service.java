package com.ocean.piuda.aws.service;

import com.ocean.piuda.aws.dto.response.GeneratePresignedPutUrlResponse;
import com.ocean.piuda.aws.properties.AwsConfigProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final S3Presigner s3Presigner;
    private final AwsConfigProperties aws;

    /** 외부에서 extension만 받아 presign 생성 */
    public GeneratePresignedPutUrlResponse generatePresignedPutUrl(String extension) {
        String key = buildObjectKey(
                aws.getS3().getProjectFolderName(),
                aws.getS3().getUserObjectsDirectory(),
                extension
        );
        return generatePresignedPutUrl(
                aws.getS3().getBucketName(),
                key,
                Duration.ofSeconds(aws.getS3().getExpirationTime())
        );
    }

    /** 버킷/키/만료 지정 presign */
    public GeneratePresignedPutUrlResponse generatePresignedPutUrl(
            String bucketName,
            String key,
            Duration expirationTime
    ) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                // 퍼블릭/CORS를 그대로 쓰므로 추가 헤더 제약 없이 단순 PUT presign
                .build();

        PutObjectPresignRequest putObjectPresignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(expirationTime)
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(putObjectPresignRequest);
        return GeneratePresignedPutUrlResponse.from(presigned, key);
    }

    /** 키 생성 버그 수정: 폴더/확장자 점/일자 경로 반영 */
    public String buildObjectKey(String projectFolder, String userDir, String extension) {
        String ext = normalizeExt(extension); // ".jpg" 형태로 보정
        String date = LocalDate.now().toString(); // YYYY-MM-DD
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String ts = String.valueOf(Instant.now().toEpochMilli());

        // {projectFolder}/{userDir}/{YYYY-MM-DD}/{uuid}_{ts}{ext}
        return String.format("%s/%s/%s/%s_%s%s",
                trimSlashes(projectFolder),
                trimSlashes(userDir),
                date,
                uuid,
                ts,
                ext
        );
    }

    private String normalizeExt(String extension) {
        if (extension == null || extension.isBlank()) return "";
        String e = extension.trim();
        if (!e.startsWith(".")) e = "." + e;
        return e.toLowerCase();
    }

    private String trimSlashes(String s) {
        if (s == null) return "";
        return s.replaceAll("^/+", "").replaceAll("/+$", "");
    }}
