package com.astra.ai;

import com.astra.ai.AgentRouter.AgentRequest;
import com.astra.ai.AgentRouter.AgentResponse;
import com.astra.astrology.TransitCalculator;
import com.astra.astrology.SwissEphemerisService.BirthChart;
import com.astra.astrology.SwissEphemerisService.PlanetPosition;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class TransitAnalysisAgent implements Agent {

    private final ChatLanguageModel chatLanguageModel;

    public TransitAnalysisAgent(ChatLanguageModel chatLanguageModel) {
        this.chatLanguageModel = chatLanguageModel;
    }

    @Override
    public AgentResponse process(AgentRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("Transit Analysis Agent processing request for user: {}", request.getUserId());

        try {
            Map<String, Object> context = request.getContext();
            BirthChart chart = (BirthChart) context.get("chart");

            String response;
            if (chart != null) {
                TransitCalculator transitCalc = new TransitCalculator();
                TransitCalculator.TransitAnalysis analysis = transitCalc.calculateTransits(
                        LocalDate.now(), chart.getPlanetaryPositions());
                response = generateLLMResponse(request.getQuery(), analysis, chart);
            } else {
                response = "I need your birth chart data to provide transit analysis. Please ensure your birth profile is complete.";
            }

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("category", "transit");

            long processingTime = System.currentTimeMillis() - startTime;

            return AgentResponse.builder()
                    .agentType(getAgentName())
                    .response(response)
                    .metadata(metadata)
                    .processingTimeMs(processingTime)
                    .build();

        } catch (Exception e) {
            log.error("Error in Transit Analysis Agent", e);
            return AgentResponse.builder()
                    .agentType(getAgentName())
                    .response("I apologize, but I encountered an error analyzing current transits. Please try again.")
                    .metadata(new HashMap<>())
                    .processingTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    private String generateLLMResponse(String query, TransitCalculator.TransitAnalysis analysis, BirthChart chart) {
        StringBuilder transitSummary = new StringBuilder();
        transitSummary.append("Current Date: ").append(analysis.getTransitDate()).append("\n");
        if (!analysis.getMajorTransits().isEmpty()) {
            transitSummary.append("Major Transits:\n");
            for (Map.Entry<String, String> e : analysis.getMajorTransits().entrySet()) {
                transitSummary.append("- ").append(e.getKey()).append(": ").append(e.getValue()).append("\n");
            }
        }

        try {
            String prompt = """
                    You are a Vedic astrology transit (Gochara) expert. Interpret how current planetary transits affect the birth chart.
                    Focus on: Saturn (Sade Sati), Jupiter transits, Rahu/Ketu transits, and major aspects.
                    Keep response to 3-4 paragraphs, practical and timely.

                    Transit Data:
                    %s

                    User Query: "%s"
                    """.formatted(transitSummary.toString(), query);
            return chatLanguageModel.generate(prompt);
        } catch (Exception e) {
            log.warn("LLM fallback for transit: {}", e.getMessage());
            return templateResponse(analysis);
        }
    }

    private String templateResponse(TransitCalculator.TransitAnalysis analysis) {
        StringBuilder sb = new StringBuilder("Current Transit Analysis (Gochara)\n\n");
        sb.append("Date: ").append(analysis.getTransitDate()).append("\n\n");

        if (!analysis.getMajorTransits().isEmpty()) {
            sb.append("Active Major Transits:\n");
            for (Map.Entry<String, String> e : analysis.getMajorTransits().entrySet()) {
                sb.append("- ").append(e.getKey()).append(": ").append(e.getValue()).append("\n");
            }
            sb.append("\n");
        }

        sb.append("Current planetary movements interact with your natal chart to create favorable ");
        sb.append("and challenging periods for different life areas. Pay special attention to major ");
        sb.append("transits involving Saturn, Jupiter, and the lunar nodes (Rahu/Ketu).\n\n");

        sb.append("Tip: Transit analysis shows how current planetary movements affect your natal placements. ");
        sb.append("Use this information to plan major activities and decisions during favorable periods.");

        return sb.toString();
    }

    @Override
    public String getAgentName() {
        return "Transit Analysis";
    }
}
