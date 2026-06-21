package com.astra.ai;

import com.astra.astrology.SwissEphemerisService.BirthChart;
import com.astra.astrology.SwissEphemerisService.PlanetPosition;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class AgentRouter {

    private final Map<String, Agent> agents;
    private final ChatLanguageModel chatLanguageModel;

    public AgentRouter(ChatLanguageModel chatLanguageModel) {
        this.chatLanguageModel = chatLanguageModel;
        this.agents = new HashMap<>();
        initializeAgents();
    }

    private void initializeAgents() {
        agents.put("chart_generator", new ChartGeneratorAgent(chatLanguageModel));
        agents.put("planet_analysis", new PlanetAnalysisAgent(chatLanguageModel));
        agents.put("dasha_analysis", new DashaAnalysisAgent(chatLanguageModel));
        agents.put("transit_analysis", new TransitAnalysisAgent(chatLanguageModel));
        agents.put("career_guidance", new CareerGuidanceAgent(chatLanguageModel));
        agents.put("marriage_guidance", new MarriageGuidanceAgent(chatLanguageModel));
        agents.put("finance_guidance", new FinanceGuidanceAgent(chatLanguageModel));
        agents.put("health_guidance", new HealthGuidanceAgent(chatLanguageModel));
        agents.put("spiritual_guidance", new SpiritualGuidanceAgent(chatLanguageModel));
        agents.put("memory_agent", new MemoryAgent(chatLanguageModel));

        log.info("Initialized 10 AI agents");
    }

    public AgentResponse routeQuery(AgentRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("Routing query: {} for user: {}", request.getQuery(), request.getUserId());

        String agentType = determineAgentType(request.getQuery());
        log.info("Determined agent type: {}", agentType);

        Agent agent = agents.get(agentType);
        if (agent == null) {
            agent = agents.get("chart_generator");
        }

        AgentResponse response = agent.process(request);

        Agent memoryAgent = agents.get("memory_agent");
        if (memoryAgent instanceof MemoryAgent memAgent) {
            memAgent.addToMemory(request.getUserId(), request.getQuery(), response.getResponse());
        }

        long processingTime = System.currentTimeMillis() - startTime;
        log.info("Query processed by {} in {}ms", agentType, processingTime);
        return response;
    }

    public AgentResponse routeToAgent(String agentType, AgentRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("Directly routing to agent: {} for user: {}", agentType, request.getUserId());

        Agent agent = agents.get(agentType);
        if (agent == null) {
            agent = agents.get("chart_generator");
        }

        AgentResponse response = agent.process(request);

        Agent memoryAgent = agents.get("memory_agent");
        if (memoryAgent instanceof MemoryAgent memAgent) {
            memAgent.addToMemory(request.getUserId(), request.getQuery(), response.getResponse());
        }

        long processingTime = System.currentTimeMillis() - startTime;
        log.info("Query processed by {} in {}ms", agentType, processingTime);
        return response;
    }

    private String determineAgentType(String query) {
        String lowerQuery = query.toLowerCase();

        try {
            String prompt = """
                    Classify the following user query about Vedic astrology into exactly ONE category.
                    Categories: career, marriage, finance, health, spiritual, dasha, transit, planet, chart
                    - career: jobs, career, work, profession, business, leadership
                    - marriage: marriage, relationship, love, spouse, partner, romance
                    - finance: money, wealth, finance, investment, income, financial
                    - health: health, wellness, disease, illness, fitness, medical
                    - spiritual: spiritual, meditation, mantra, remedy, puja, yoga, soul
                    - dasha: dasha, period, mahadasha, antardasha, timing, planetary period
                    - transit: transit, gochara, current movement, today, this week
                    - planet: planet, graha, strength, retrograde, combustion, nakshatra
                    - chart: chart, birth chart, kundli, horoscope, lagna, ascendant

                    Query: "%s"

                    Reply with only the category name, nothing else.
                    """.formatted(query);

            String result = chatLanguageModel.generate(prompt);
            if (result != null) {
                String cleaned = result.trim().toLowerCase();
                if (agents.containsKey(cleaned + "_guidance")) return cleaned + "_guidance";
                if (agents.containsKey(cleaned + "_analysis")) return cleaned + "_analysis";
                if (cleaned.equals("chart")) return "chart_generator";
                if (cleaned.equals("planet")) return "planet_analysis";
            }
        } catch (Exception e) {
            log.warn("LLM classification failed, using keyword routing: {}", e.getMessage());
        }

        if (lowerQuery.contains("career") || lowerQuery.contains("job") || lowerQuery.contains("work") ||
            lowerQuery.contains("profession") || lowerQuery.contains("business")) {
            return "career_guidance";
        }
        if (lowerQuery.contains("marriage") || lowerQuery.contains("relationship") || lowerQuery.contains("love") ||
            lowerQuery.contains("spouse") || lowerQuery.contains("partner")) {
            return "marriage_guidance";
        }
        if (lowerQuery.contains("finance") || lowerQuery.contains("money") || lowerQuery.contains("wealth") ||
            lowerQuery.contains("investment") || lowerQuery.contains("income")) {
            return "finance_guidance";
        }
        if (lowerQuery.contains("health") || lowerQuery.contains("wellness") || lowerQuery.contains("disease") ||
            lowerQuery.contains("illness")) {
            return "health_guidance";
        }
        if (lowerQuery.contains("spiritual") || lowerQuery.contains("meditation") || lowerQuery.contains("mantra") ||
            lowerQuery.contains("remedy") || lowerQuery.contains("puja")) {
            return "spiritual_guidance";
        }
        if (lowerQuery.contains("dasha") || lowerQuery.contains("period") || lowerQuery.contains("mahadasha")) {
            return "dasha_analysis";
        }
        if (lowerQuery.contains("transit") || lowerQuery.contains("gochara") || lowerQuery.contains("current")) {
            return "transit_analysis";
        }
        if (lowerQuery.contains("planet") || lowerQuery.contains("strength") || lowerQuery.contains("retrograde")) {
            return "planet_analysis";
        }

        return "chart_generator";
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgentRequest {
        private String userId;
        private String query;
        private Map<String, Object> context;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgentResponse {
        private String agentType;
        private String response;
        private Map<String, Object> metadata;
        private long processingTimeMs;
    }
}
