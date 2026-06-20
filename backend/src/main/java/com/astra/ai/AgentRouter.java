package com.astra.ai;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class AgentRouter {

    private final Map<String, Agent> agents;

    public AgentRouter() {
        this.agents = new HashMap<>();
        initializeAgents();
    }

    private void initializeAgents() {
        // Initialize all 10 agents
        agents.put("chart_generator", new ChartGeneratorAgent());
        agents.put("planet_analysis", new PlanetAnalysisAgent());
        agents.put("dasha_analysis", new DashaAnalysisAgent());
        agents.put("transit_analysis", new TransitAnalysisAgent());
        agents.put("career_guidance", new CareerGuidanceAgent());
        agents.put("marriage_guidance", new MarriageGuidanceAgent());
        agents.put("finance_guidance", new FinanceGuidanceAgent());
        agents.put("health_guidance", new HealthGuidanceAgent());
        agents.put("spiritual_guidance", new SpiritualGuidanceAgent());
        agents.put("memory_agent", new MemoryAgent());
        
        log.info("Initialized 10 AI agents");
    }

    public AgentResponse routeQuery(AgentRequest request) {
        log.info("Routing query: {}", request.getQuery());
        
        // Determine which agent(s) to invoke based on query analysis
        String agentType = determineAgentType(request.getQuery());
        
        // Invoke the appropriate agent
        Agent agent = agents.get(agentType);
        if (agent == null) {
            agent = agents.get("chart_generator"); // Default to chart generator
        }
        
        // Process the query
        AgentResponse response = agent.process(request);
        
        // Add memory context
        MemoryAgent memoryAgent = (MemoryAgent) agents.get("memory_agent");
        memoryAgent.addToMemory(request.getUserId(), request.getQuery(), response.getResponse());
        
        return response;
    }

    private String determineAgentType(String query) {
        String lowerQuery = query.toLowerCase();
        
        // Career-related queries
        if (lowerQuery.contains("career") || lowerQuery.contains("job") || lowerQuery.contains("work") || 
            lowerQuery.contains("profession") || lowerQuery.contains("business")) {
            return "career_guidance";
        }
        
        // Marriage/relationship queries
        if (lowerQuery.contains("marriage") || lowerQuery.contains("relationship") || lowerQuery.contains("love") || 
            lowerQuery.contains("spouse") || lowerQuery.contains("partner")) {
            return "marriage_guidance";
        }
        
        // Finance-related queries
        if (lowerQuery.contains("finance") || lowerQuery.contains("money") || lowerQuery.contains("wealth") || 
            lowerQuery.contains("investment") || lowerQuery.contains("income")) {
            return "finance_guidance";
        }
        
        // Health-related queries
        if (lowerQuery.contains("health") || lowerQuery.contains("wellness") || lowerQuery.contains("disease") || 
            lowerQuery.contains("illness")) {
            return "health_guidance";
        }
        
        // Spiritual queries
        if (lowerQuery.contains("spiritual") || lowerQuery.contains("meditation") || lowerQuery.contains("mantra") || 
            lowerQuery.contains("remedy") || lowerQuery.contains("puja")) {
            return "spiritual_guidance";
        }
        
        // Dasha queries
        if (lowerQuery.contains("dasha") || lowerQuery.contains("period") || lowerQuery.contains("mahadasha")) {
            return "dasha_analysis";
        }
        
        // Transit queries
        if (lowerQuery.contains("transit") || lowerQuery.contains("gochara") || lowerQuery.contains("current")) {
            return "transit_analysis";
        }
        
        // Planet analysis queries
        if (lowerQuery.contains("planet") || lowerQuery.contains("strength") || lowerQuery.contains("retrograde")) {
            return "planet_analysis";
        }
        
        // Default to chart generator
        return "chart_generator";
    }

    @Data
    public static class AgentRequest {
        private String userId;
        private String query;
        private Map<String, Object> context;
    }

    @Data
    public static class AgentResponse {
        private String agentType;
        private String response;
        private Map<String, Object> metadata;
        private long processingTimeMs;
    }
}
