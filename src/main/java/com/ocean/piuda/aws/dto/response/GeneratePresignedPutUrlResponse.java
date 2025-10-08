package com.ocean.piuda.aws.dto.response;

import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Instant;

public record GeneratePresignedPutUrlResponse(
    String presignedUrl,
    Instant expiresAt,
    String key
) {

  public static GeneratePresignedPutUrlResponse from(PresignedPutObjectRequest presignedPutObjectRequest, String key) {
    return new GeneratePresignedPutUrlResponse(
        presignedPutObjectRequest.url().toString(),
        presignedPutObjectRequest.expiration(),
        key
    );
  }
}
