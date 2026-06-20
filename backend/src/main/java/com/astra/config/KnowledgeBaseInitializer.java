package com.astra.config;

import com.astra.ai.vector.KnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class KnowledgeBaseInitializer implements CommandLineRunner {

    private final KnowledgeBaseService knowledgeBaseService;

    @Override
    public void run(String... args) {
        try {
            log.info("Starting knowledge base initialization...");
            knowledgeBaseService.initializeKnowledgeBase();
            log.info("Knowledge base initialization completed successfully");
        } catch (Exception e) {
            log.error("Failed to initialize knowledge base", e);
        }
    }
}
