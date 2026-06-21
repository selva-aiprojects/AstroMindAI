package com.astra.ai;

import com.astra.ai.AgentRouter.AgentRequest;
import com.astra.ai.AgentRouter.AgentResponse;
import com.astra.astrology.SwissEphemerisService.BirthChart;
import com.astra.astrology.SwissEphemerisService.PlanetPosition;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class PlanetAnalysisAgent implements Agent {

    private final ChatLanguageModel chatLanguageModel;

    public PlanetAnalysisAgent(ChatLanguageModel chatLanguageModel) {
        this.chatLanguageModel = chatLanguageModel;
    }

    @Override
    public AgentResponse process(AgentRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("Planet Analysis Agent processing request for user: {}", request.getUserId());

        try {
            Map<String, Object> context = request.getContext();
            BirthChart chart = (BirthChart) context.get("chart");

            String response;
            if (chart != null) {
                response = generateLLMResponse(request.getQuery(), chart);
            } else {
                response = "I need your birth chart data to provide planetary analysis. Please ensure your birth profile is complete.";
            }

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("analysisType", "planet_strength");

            long processingTime = System.currentTimeMillis() - startTime;

            return AgentResponse.builder()
                    .agentType(getAgentName())
                    .response(response)
                    .metadata(metadata)
                    .processingTimeMs(processingTime)
                    .build();

        } catch (Exception e) {
            log.error("Error in Planet Analysis Agent", e);
            return AgentResponse.builder()
                    .agentType(getAgentName())
                    .response("I apologize, but I encountered an error analyzing planetary positions. Please try again.")
                    .metadata(new HashMap<>())
                    .processingTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    private String generateLLMResponse(String query, BirthChart chart) {
        String chartSummary = summarizeChart(chart);
        try {
            String prompt = """
                    You are a Vedic astrology expert analyzing planetary strengths and placements.
                    Provide detailed analysis of each planet: its sign, house, nakshatra, dignity, and functional nature.
                    Focus on: Shadbala concepts, planetary dignity (exaltation, debilitation, mulatrikona), and yogas.
                    Keep response to 3-4 paragraphs.

                    Birth Chart:
                    %s

                    User Query: "%s"
                    """.formatted(chartSummary, query);
            return chatLanguageModel.generate(prompt);
        } catch (Exception e) {
            log.warn("LLM fallback for planet analysis: {}", e.getMessage());
            return templateResponse(chart);
        }
    }

    private String templateResponse(BirthChart chart) {
        StringBuilder sb = new StringBuilder("Planetary Strength Analysis\n\n");
        for (Map.Entry<String, PlanetPosition> e : chart.getPlanetaryPositions().entrySet()) {
            PlanetPosition p = e.getValue();
            sb.append(e.getKey()).append(" in ").append(p.getSign())
              .append(" (").append(p.getNakshatra()).append(")")
              .append(" | House ").append(chart.getHousePlacements().get(e.getKey()))
              .append(" | ").append(chart.getFunctionalNature().getOrDefault(e.getKey(), "Neutral"));
            if (p.isRetrograde()) sb.append(" | Retrograde");
            if (p.isCombust()) sb.append(" | Combust");
            sb.append("\n");
        }
        sb.append("\nThe planetary positions show a unique distribution of energies across your chart. ")
          .append("Each planet's house placement and sign influence its expression in your life. ")
          .append("Functional benefic planets support growth while malefic planets present challenges to overcome.");
        return sb.toString();
    }

    private String summarizeChart(BirthChart chart) {
        StringBuilder sb = new StringBuilder();
        sb.append("Ascendant: ").append(signName(chart.getAscendant())).append("\n");
        for (Map.Entry<String, PlanetPosition> e : chart.getPlanetaryPositions().entrySet()) {
            PlanetPosition p = e.getValue();
            sb.append(e.getKey()).append(": ").append(p.getSign()).append(" House ").append(chart.getHousePlacements().get(e.getKey())).append("\n");
        }
        return sb.toString();
    }

    private String signName(double lon) {
        String[] s = {"Aries","Taurus","Gemini","Cancer","Leo","Virgo","Libra","Scorpio","Sagittarius","Capricorn","Aquarius","Pisces"};
        return s[(int)(lon/30)];
    }

    @Override
    public String getAgentName() {
        return "Planet Analysis";
    }
}
