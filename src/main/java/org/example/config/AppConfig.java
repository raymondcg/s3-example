package org.example.config;

import org.example.dao.DataDao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Value("${endpoint}")
    private String endpoint;
    @Value("accessKey")
    private String accessKey;
    @Value("secretKey")
    private String secretKey;

    @Bean
    public DataDao dataDao() {
        return new DataDao( endpoint, accessKey, secretKey );
    }
}
