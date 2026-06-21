package com.astra.ai;

import com.astra.ai.AgentRouter.AgentRequest;
import com.astra.ai.AgentRouter.AgentResponse;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class MemoryAgent implements Agent {

    private final ChatLanguageModel chatLanguageModel;
    private final Map<String, List<String>> userMemories = new ConcurrentHashMap<>();

    public MemoryAgent(ChatLanguageModel chatLanguageModel) {
        this.chatLanguageModel = chatLanguageModel;
    }

    @Override
    public AgentResponse process(AgentRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("Memory Agent processing request for user: {}", request.getUserId());

        try {
            List<String> memories = userMemories.getOrDefault(request.getUserId(), new ArrayList<>());
            List<String> recent = memories.size() > 5 ? memories.subList(Math.max(0, memories.size() - 5), memories.size()) : memories;

            String response = generateMemoryResponse(recent);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("memoriesRetrieved", recent.size());

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
        userMemories.computeIfAbsent(userId, k -> new ArrayList<>())
                .add(String.format("Q: %s\nA: %s", query, response.length() > 200 ? response.substring(0, 200) + "..." : response));
        log.info("Added interaction to memory for user: {}", userId);
    }

    private String generateMemoryResponse(List<String> memories) {
        if (memories.isEmpty()) {
            return "I don't have any previous interactions stored in your memory yet. As you ask questions, I'll remember our conversations to provide more personalized responses.";
        }

        StringBuilder response = new StringBuilder();
        response.append("Memory Context\n\n");
        response.append("Based on our previous conversations, here's what I remember:\n\n");

        for (String memory : memories) {
            response.append("- ").append(memory).append("\n\n");
        }

        response.append("This context helps me provide more personalized responses. Feel free to ask follow-up questions or explore new topics related to your chart.");
        return response.toString();
    }

    @Override
    public String getAgentName() {
        return "Memory Agent";
    }
}
