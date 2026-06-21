package com.astra.ai.vector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
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
        request.put("metadata", Map.of("hnsw:space", "cosine"));

        try {
            restTemplate.postForObject(url, request, String.class);
            log.info("Created Chroma collection: {}", collectionName);
        } catch (Exception e) {
            log.warn("Failed to create Chroma collection {} (may already exist): {}", collectionName, e.getMessage());
        }
    }

    public void addDocuments(String collectionName, List<String> documents, List<Map<String, Object>> metadatas) {
        if (documents.isEmpty()) return;

        String url = getBaseUrl() + "/api/v1/collections/" + collectionName + "/add";

        List<List<Double>> embeddingsList = new ArrayList<>();
        for (int i = 0; i < documents.size(); i++) {
            List<Double> zeroVec = new ArrayList<>();
            for (int j = 0; j < 384; j++) zeroVec.add(0.0);
            embeddingsList.add(zeroVec);
        }

        Map<String, Object> request = new HashMap<>();
        request.put("documents", documents);
        request.put("metadatas", metadatas);
        request.put("embeddings", embeddingsList);

        try {
            restTemplate.postForObject(url, request, String.class);
            log.info("Added {} documents to collection: {}", documents.size(), collectionName);
        } catch (Exception e) {
            log.warn("Failed to add documents to Chroma: {}", e.getMessage());
        }
    }

    public List<String> queryCollection(String collectionName, String queryText, int nResults) {
        String url = getBaseUrl() + "/api/v1/collections/" + collectionName + "/query";

        List<Double> queryList = new ArrayList<>();
        for (int j = 0; j < 384; j++) queryList.add(0.0);

        Map<String, Object> request = new HashMap<>();
        request.put("query_embeddings", List.of(queryList));
        request.put("n_results", nResults);

        try {
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);
            if (response != null && response.containsKey("documents")) {
                Object docs = response.get("documents");
                if (docs instanceof List) {
                    List<?> docList = (List<?>) docs;
                    if (!docList.isEmpty() && docList.get(0) instanceof List) {
                        return (List<String>) ((List<?>) docList.get(0));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to query Chroma: {}", e.getMessage());
        }

        return List.of();
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
