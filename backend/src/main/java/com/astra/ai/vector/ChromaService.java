package com.astra.ai.vector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ChromaService {

    @Value("${chroma.db.host:localhost}")
    private String chromaHost;

    @Value("${chroma.db.port:8000}")
    private int chromaPort;

    private final RestTemplate restTemplate = new RestTemplate();

    private String getBaseUrl() {
        return String.format("http://%s:%d", chromaHost, chromaPort);
    }

    public void createCollection(String collectionName) {
        String url = getBaseUrl() + "/api/v1/collections";
        
        Map<String, Object> request = new HashMap<>();
        request.put("name", collectionName);
        request.put("metadata", Map.of("description", "ASTRA knowledge base collection"));
        
        try {
            restTemplate.postForObject(url, request, String.class);
            log.info("Created Chroma collection: {}", collectionName);
        } catch (Exception e) {
            log.error("Failed to create collection: {}", collectionName, e);
        }
    }

    public void addDocuments(String collectionName, List<String> documents, List<Map<String, Object>> metadatas, List<List<Double>> embeddings) {
        String url = getBaseUrl() + "/api/v1/collections/" + collectionName + "/add";
        
        Map<String, Object> request = new HashMap<>();
        request.put("documents", documents);
        request.put("metadatas", metadatas);
        request.put("embeddings", embeddings);
        
        try {
            restTemplate.postForObject(url, request, String.class);
            log.info("Added {} documents to collection: {}", documents.size(), collectionName);
        } catch (Exception e) {
            log.error("Failed to add documents to collection: {}", collectionName, e);
        }
    }

    public List<Map<String, Object>> queryCollection(String collectionName, List<Double> queryEmbedding, int nResults) {
        String url = getBaseUrl() + "/api/v1/collections/" + collectionName + "/query";
        
        Map<String, Object> request = new HashMap<>();
        request.put("query_embeddings", List.of(queryEmbedding));
        request.put("n_results", nResults);
        
        try {
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);
            return (List<Map<String, Object>>) response.get("results");
        } catch (Exception e) {
            log.error("Failed to query collection: {}", collectionName, e);
            return List.of();
        }
    }

    public List<Map<String, Object>> getCollections() {
        String url = getBaseUrl() + "/api/v1/collections";
        
        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            return (List<Map<String, Object>>) response.get("collections");
        } catch (Exception e) {
            log.error("Failed to get collections", e);
            return List.of();
        }
    }

    public void deleteCollection(String collectionName) {
        String url = getBaseUrl() + "/api/v1/collections/" + collectionName;
        
        try {
            restTemplate.delete(url);
            log.info("Deleted collection: {}", collectionName);
        } catch (Exception e) {
            log.error("Failed to delete collection: {}", collectionName, e);
        }
    }
}
