package com.astra.ai.vector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeBaseService {

    private final ChromaService chromaService;

    public void initializeKnowledgeBase() {
        log.info("Initializing ASTRA knowledge base...");
        
        // Create collections
        createCollectionIfNotExists("vedic_texts");
        createCollectionIfNotExists("remedies");
        createCollectionIfNotExists("user_memory");
        createCollectionIfNotExists("knowledge_base");
        
        log.info("Knowledge base initialization completed");
    }

    private void createCollectionIfNotExists(String collectionName) {
        List<Map<String, Object>> collections = chromaService.getCollections();
        boolean exists = collections.stream()
                .anyMatch(col -> collectionName.equals(col.get("name")));
        
        if (!exists) {
            chromaService.createCollection(collectionName);
            log.info("Created collection: {}", collectionName);
        } else {
            log.info("Collection already exists: {}", collectionName);
        }
    }

    public void addVedicText(String text, String source, String category) {
        List<Map<String, Object>> metadatas = List.of(
                Map.of(
                        "source", source,
                        "category", category,
                        "type", "vedic_text"
                )
        );
        
        // Note: In production, you would generate embeddings using an embedding model
        // For now, we'll use placeholder embeddings
        List<List<Double>> embeddings = List.of(generatePlaceholderEmbedding());
        
        chromaService.addDocuments("vedic_texts", List.of(text), metadatas, embeddings);
    }

    public void addRemedy(String remedy, String type, String description) {
        List<Map<String, Object>> metadatas = List.of(
                Map.of(
                        "type", type,
                        "description", description,
                        "remedy_type", "remedy"
                )
        );
        
        List<List<Double>> embeddings = List.of(generatePlaceholderEmbedding());
        
        chromaService.addDocuments("remedies", List.of(remedy), metadatas, embeddings);
    }

    public void addUserMemory(String userId, String memory, String category) {
        List<Map<String, Object>> metadatas = List.of(
                Map.of(
                        "user_id", userId,
                        "category", category,
                        "type", "user_memory",
                        "timestamp", System.currentTimeMillis()
                )
        );
        
        List<List<Double>> embeddings = List.of(generatePlaceholderEmbedding());
        
        chromaService.addDocuments("user_memory", List.of(memory), metadatas, embeddings);
    }

    public List<String> searchVedicTexts(String query, int topK) {
        List<Double> queryEmbedding = generatePlaceholderEmbedding();
        List<Map<String, Object>> results = chromaService.queryCollection("vedic_texts", queryEmbedding, topK);
        
        List<String> texts = new ArrayList<>();
        for (Map<String, Object> result : results) {
            List<String> documents = (List<String>) result.get("documents");
            texts.addAll(documents);
        }
        
        return texts;
    }

    public List<String> searchRemedies(String query, int topK) {
        List<Double> queryEmbedding = generatePlaceholderEmbedding();
        List<Map<String, Object>> results = chromaService.queryCollection("remedies", queryEmbedding, topK);
        
        List<String> remedies = new ArrayList<>();
        for (Map<String, Object> result : results) {
            List<String> documents = (List<String>) result.get("documents");
            remedies.addAll(documents);
        }
        
        return remedies;
    }

    public List<String> getUserMemories(String userId, int topK) {
        // In production, you would filter by user_id in the metadata
        List<Double> queryEmbedding = generatePlaceholderEmbedding();
        List<Map<String, Object>> results = chromaService.queryCollection("user_memory", queryEmbedding, topK);
        
        List<String> memories = new ArrayList<>();
        for (Map<String, Object> result : results) {
            List<String> documents = (List<String>) result.get("documents");
            memories.addAll(documents);
        }
        
        return memories;
    }

    private List<Double> generatePlaceholderEmbedding() {
        // Placeholder embedding - in production, use actual embedding model
        // This is a 384-dimensional embedding (common for sentence-transformers)
        List<Double> embedding = new ArrayList<>();
        for (int i = 0; i < 384; i++) {
            embedding.add(Math.random());
        }
        return embedding;
    }
}
