package com.astra.ai;

import com.astra.ai.AgentRouter.AgentRequest;
import com.astra.ai.AgentRouter.AgentResponse;
import com.astra.astrology.SwissEphemerisService;
import com.astra.astrology.SwissEphemerisService.BirthChart;
import com.astra.astrology.SwissEphemerisService.PlanetPosition;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class ChartGeneratorAgent implements Agent {

    private final ChatLanguageModel chatLanguageModel;

    public ChartGeneratorAgent(ChatLanguageModel chatLanguageModel) {
        this.chatLanguageModel = chatLanguageModel;
    }

    @Override
    public AgentResponse process(AgentRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("Chart Generator Agent processing request for user: {}", request.getUserId());

        try {
            Map<String, Object> context = request.getContext();
            SwissEphemerisService swissEphemerisService = (SwissEphemerisService) context.get("swissEphemerisService");

            if (swissEphemerisService == null) {
                return AgentResponse.builder()
                        .agentType(getAgentName())
                        .response("I need your birth details to generate your birth chart. Please set up your birth profile first.")
                        .metadata(new HashMap<>())
                        .processingTimeMs(System.currentTimeMillis() - startTime)
                        .build();
            }

            BirthChart chart = swissEphemerisService.calculateBirthChart(
                    (LocalDate) context.get("birthDate"),
                    (LocalTime) context.get("birthTime"),
                    (Double) context.get("latitude"),
                    (Double) context.get("longitude"),
                    (String) context.get("timezone"),
                    (String) context.getOrDefault("ayanamsa", "LAHIRI")
            );

            String response = generateLLMResponse(request.getQuery(), chart);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("chart", chart);
            metadata.put("ayanamsa", chart.getAyanamsa());

            long processingTime = System.currentTimeMillis() - startTime;

            return AgentResponse.builder()
                    .agentType(getAgentName())
                    .response(response)
                    .metadata(metadata)
                    .processingTimeMs(processingTime)
                    .build();

        } catch (Exception e) {
            log.error("Error in Chart Generator Agent", e);
            return AgentResponse.builder()
                    .agentType(getAgentName())
                    .response("I apologize, but I encountered an error generating your birth chart. Please ensure your birth details are correct.")
                    .metadata(new HashMap<>())
                    .processingTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    private String generateLLMResponse(String query, BirthChart chart) {
        StringBuilder chartSummary = new StringBuilder();
        chartSummary.append("Ascendant (Lagna): ").append(calculateSign(chart.getAscendant())).append("\n");
        for (Map.Entry<String, PlanetPosition> entry : chart.getPlanetaryPositions().entrySet()) {
            PlanetPosition p = entry.getValue();
            chartSummary.append(entry.getKey()).append(": ")
                    .append(p.getSign()).append(" (").append(p.getNakshatra()).append(")")
                    .append(" House ").append(chart.getHousePlacements().get(entry.getKey()))
                    .append(p.isRetrograde() ? " [Retrograde]" : "")
                    .append(p.isCombust() ? " [Combust]" : "")
                    .append("\n");
        }
        chartSummary.append("Functional Natures:\n");
        for (Map.Entry<String, String> e : chart.getFunctionalNature().entrySet()) {
            chartSummary.append(e.getKey()).append(": ").append(e.getValue()).append("\n");
        }

        try {
            String prompt = """
                    You are a Vedic astrology expert. Interpret this birth chart in a clear, insightful way.
                    Keep the response to 3-4 paragraphs, conversational but authoritative.

                    Chart Data:
                    %s

                    User Query: "%s"
                    """.formatted(chartSummary.toString(), query);

            return chatLanguageModel.generate(prompt);
        } catch (Exception e) {
            log.warn("LLM call failed, using template response: {}", e.getMessage());
            return generateTemplateResponse(chart);
        }
    }

    private String generateTemplateResponse(BirthChart chart) {
        StringBuilder response = new StringBuilder();
        response.append("Your Birth Chart Analysis\n\n");
        response.append("Ascendant (Lagna): ").append(calculateSign(chart.getAscendant())).append("\n\n");
        response.append("Planetary Positions:\n");

        for (Map.Entry<String, PlanetPosition> entry : chart.getPlanetaryPositions().entrySet()) {
            PlanetPosition position = entry.getValue();
            response.append("- ").append(entry.getKey()).append(": ").append(position.getSign())
                    .append(" (").append(position.getNakshatra()).append(") in House ")
                    .append(chart.getHousePlacements().get(entry.getKey()));
            if (position.isRetrograde()) response.append(" [Retrograde]");
            if (position.isCombust()) response.append(" [Combust]");
            response.append("\n");
        }

        response.append("\nYour chart shows a well-distributed planetary pattern. ");
        response.append("The strength of your houses and planetary placements suggest strong potential in areas highlighted by your functional benefic planets. ");
        response.append("For personalized guidance, ask me about career, relationships, finance, or specific life areas.");

        return response.toString();
    }

    private String calculateSign(double longitude) {
        String[] signs = {
            "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
            "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
        };
        return signs[(int)(longitude / 30)];
    }

    @Override
    public String getAgentName() {
        return "Chart Generator";
    }
}
