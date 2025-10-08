package com.ocean.piuda.aws.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "aws")
@Component
@Getter
@Setter
public class AwsConfigProperties {

    private String accessKey;
    private String secretKey;
    private String region;

    private S3Properties s3;

    @Data
    @Validated
    public static class S3Properties {
        /**
         * AWS S3 Bucket Name
         */
        @NotBlank(message = "AWS S3 Bucket Name must not be blank")
        private String bucketName;

        /**
         * AWS S3 Project Folder Name
         * <p>
         * All files uploaded to S3 on this project will be stored in this folder.
         */
        @NotBlank(message = "AWS S3 Project Folder Name must not be blank")
        private String projectFolderName;

        /**
         * AWS S3 Presigned URL Expiration Time. (in seconds) Default: 10 minutes
         */
        @Min(value = 1, message = "AWS S3 Presigned URL Expiration Time must be greater than 0")
        private Long expirationTime = 600L;


        /**
         * AWS S3 User Objects Directory Name
         * <p>
         * All files uploaded to S3 by users will be stored in this folder.
         */
        private String userObjectsDirectory = "user_objects";
    }
}
