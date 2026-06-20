package com.astra.ai;

import com.astra.ai.AgentRouter.AgentRequest;
import com.astra.ai.AgentRouter.AgentResponse;
import com.astra.ai.vector.KnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class MemoryAgent implements Agent {

    private final KnowledgeBaseService knowledgeBaseService;

    @Override
    public AgentResponse process(AgentRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("Memory Agent processing request for user: {}", request.getUserId());
        
        try {
            // Retrieve user's past interactions from memory
            String userId = request.getUserId();
            var memories = knowledgeBaseService.getUserMemories(userId, 5);
            
            // Generate response with memory context
            String response = generateMemoryResponse(memories);
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("memoriesRetrieved", memories.size());
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            return AgentResponse.builder()
                    .agentType(getAgentName())
                    .response(response)
                    .metadata(metadata)
                    .processingTimeMs(processingTime)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error in Memory Agent", e);
            return AgentResponse.builder()
                    .agentType(getAgentName())
                    .response("I apologize, but I encountered an error accessing your memory. Please try again.")
                    .metadata(new HashMap<>())
                    .processingTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    public void addToMemory(String userId, String query, String response) {
        try {
            // Store interaction in vector database for future retrieval
            String memory = String.format("Query: %s\nResponse: %s", query, response);
            knowledgeBaseService.addUserMemory(userId, memory, "interaction");
            log.info("Added interaction to memory for user: {}", userId);
        } catch (Exception e) {
            log.error("Error adding to memory", e);
        }
    }

    private String generateMemoryResponse(java.util.List<String> memories) {
        if (memories.isEmpty()) {
            return "I don't have any previous interactions stored in your memory yet. ";
        }
        
        StringBuilder response = new StringBuilder();
        response.append("📝 **Memory Context**\n\n");
        response.append("Based on your previous interactions, here's what I remember:\n\n");
        
        for (String memory : memories) {
            response.append("- ").append(memory).append("\n\n");
        }
        
        response.append("This context helps me provide more personalized responses based on your history.\n");
        
        return response.toString();
    }

    @Override
    public String getAgentName() {
        return "Memory Agent";
    }
}
