package org.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
public class ApplicationProperties {

    public static String BUCKET_NAME;

    @Value("${bucketName}")
    public void setBucketName( String bucketName ) {
        BUCKET_NAME = bucketName;
    }
}
