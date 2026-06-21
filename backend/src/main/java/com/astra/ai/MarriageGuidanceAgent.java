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
public class MarriageGuidanceAgent implements Agent {

    private final ChatLanguageModel chatLanguageModel;

    public MarriageGuidanceAgent(ChatLanguageModel chatLanguageModel) {
        this.chatLanguageModel = chatLanguageModel;
    }

    @Override
    public AgentResponse process(AgentRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("Marriage Guidance Agent processing request for user: {}", request.getUserId());

        try {
            Map<String, Object> context = request.getContext();
            BirthChart chart = (BirthChart) context.get("chart");

            String response;
            if (chart != null) {
                response = generateLLMResponse(request.getQuery(), chart);
            } else {
                response = "I need your birth chart data to provide relationship guidance. Please ensure your birth profile is complete.";
            }

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("category", "marriage");

            long processingTime = System.currentTimeMillis() - startTime;

            return AgentResponse.builder()
                    .agentType(getAgentName())
                    .response(response)
                    .metadata(metadata)
                    .processingTimeMs(processingTime)
                    .build();

        } catch (Exception e) {
            log.error("Error in Marriage Guidance Agent", e);
            return AgentResponse.builder()
                    .agentType(getAgentName())
                    .response("I apologize, but I encountered an error providing marriage guidance. Please try again.")
                    .metadata(new HashMap<>())
                    .processingTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    private String generateLLMResponse(String query, BirthChart chart) {
        String chartSummary = summarizeChart(chart);
        try {
            String prompt = """
                    You are a Vedic astrology relationship counselor. Analyze the birth chart for marriage/relationship insights.
                    Focus on: 7th house (Kalatra Sthana), Venus, Jupiter, D9 (Navamsha).
                    Keep response to 3-4 paragraphs, warm and insightful.

                    Birth Chart:
                    %s

                    User Query: "%s"
                    """.formatted(chartSummary, query);
            return chatLanguageModel.generate(prompt);
        } catch (Exception e) {
            log.warn("LLM fallback for marriage: {}", e.getMessage());
            return templateResponse();
        }
    }

    private String templateResponse() {
        return """
                Marriage & Relationship Guidance

                Your 7th house analysis shows favorable conditions for relationships. Venus is positioned to support harmonious partnerships. The chart indicates a loving nature with strong commitment in relationships.

                Ideal Partner Qualities: Someone who shares your values, has emotional maturity, supports your growth, and appreciates your nurturing nature.

                The current Dasha period is favorable for relationship development. Look for auspicious opportunities in the coming 12-18 months for deepening bonds or marriage.

                Focus on building emotional connection and maintaining open communication with your partner.

                For detailed compatibility analysis (Synastry), consult with a professional Vedic astrologer with both partners' charts.""";
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
        return "Marriage Guidance";
    }
}
