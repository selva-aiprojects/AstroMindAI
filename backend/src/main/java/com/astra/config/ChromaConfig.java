package com.astra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class ChromaConfig {

    @Value("${chroma.db.host:localhost}")
    private String chromaHost;

    @Value("${chroma.db.port:8000}")
    private int chromaPort;

    @Bean
    public String chromaBaseUrl() {
        return String.format("http://%s:%d", chromaHost, chromaPort);
    }
}
