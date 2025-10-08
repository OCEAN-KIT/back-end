package com.ocean.piuda.aws.config;

import com.ocean.piuda.aws.properties.AwsConfigProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

@Configuration
@RequiredArgsConstructor
public class AwsCredentialConfig {

    private final AwsConfigProperties awsConfigProperties;

    @Bean
    public AwsCredentialsProvider awsCredentialProvider() {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.builder()
                        .accessKeyId(this.awsConfigProperties.getAccessKey())
                        .secretAccessKey(this.awsConfigProperties.getSecretKey())
                        .build()
        );
    }
}
