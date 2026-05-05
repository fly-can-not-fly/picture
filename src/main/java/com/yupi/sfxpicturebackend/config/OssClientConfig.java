package com.yupi.sfxpicturebackend.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "oss")
@Data
public class OssClientConfig {

    private String accessKeyId;

    private String secretAccessKey;

    private String endpoint;

    private String region;

    private String bucketName;

    private String urlPrefix;

    @Bean
    public OSS ossClient() {
        DefaultCredentialProvider provider = CredentialsProviderFactory
                .newDefaultCredentialProvider(accessKeyId, secretAccessKey);
        return OSSClientBuilder
                .create()
                .credentialsProvider(provider)
                .endpoint(endpoint)
                .region(region).build();
    }
}