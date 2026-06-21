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
public class FinanceGuidanceAgent implements Agent {

    private final ChatLanguageModel chatLanguageModel;

    public FinanceGuidanceAgent(ChatLanguageModel chatLanguageModel) {
        this.chatLanguageModel = chatLanguageModel;
    }

    @Override
    public AgentResponse process(AgentRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("Finance Guidance Agent processing request for user: {}", request.getUserId());

        try {
            Map<String, Object> context = request.getContext();
            BirthChart chart = (BirthChart) context.get("chart");

            String response;
            if (chart != null) {
                response = generateLLMResponse(request.getQuery(), chart);
            } else {
                response = "I need your birth chart data to provide financial guidance. Please ensure your birth profile is complete.";
            }

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("category", "finance");

            long processingTime = System.currentTimeMillis() - startTime;

            return AgentResponse.builder()
                    .agentType(getAgentName())
                    .response(response)
                    .metadata(metadata)
                    .processingTimeMs(processingTime)
                    .build();

        } catch (Exception e) {
            log.error("Error in Finance Guidance Agent", e);
            return AgentResponse.builder()
                    .agentType(getAgentName())
                    .response("I apologize, but I encountered an error providing finance guidance. Please try again.")
                    .metadata(new HashMap<>())
                    .processingTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    private String generateLLMResponse(String query, BirthChart chart) {
        String chartSummary = summarizeChart(chart);
        try {
            String prompt = """
                    You are a Vedic astrology financial advisor. Analyze the birth chart for financial insights.
                    Focus on: 2nd house (Dhana Sthana), 11th house (Labha Sthana), 9th house (Bhagya Sthana), Jupiter, Venus.
                    Keep response to 3-4 paragraphs, practical and actionable.

                    Birth Chart:
                    %s

                    User Query: "%s"
                    """.formatted(chartSummary, query);
            return chatLanguageModel.generate(prompt);
        } catch (Exception e) {
            log.warn("LLM fallback for finance: {}", e.getMessage());
            return templateResponse();
        }
    }

    private String templateResponse() {
        return """
                Finance & Wealth Guidance

                Your 2nd and 11th houses show good financial potential. Jupiter's influence suggests growth in wealth through consistent effort and wise investments. The chart indicates steady income accumulation and good money management skills.

                Favorable Investment Areas: Real estate shows long-term potential. Stock market investments may be favorable during specific planetary periods. Fixed income instruments provide stability.

                The current Dasha period supports financial growth. This is a favorable time for investments and financial planning. Focus on long-term wealth creation over short-term gains.

                Maintain a balanced portfolio and build emergency funds. Consider consulting a financial advisor for personalized investment strategies.

                Disclaimer: This guidance is based on astrological analysis and should NOT be considered financial advice.""";
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
        return "Finance Guidance";
    }
}
