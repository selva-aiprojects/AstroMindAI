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
import java.util.stream.Collectors;

@Component
@Slf4j
public class CareerGuidanceAgent implements Agent {

    private final ChatLanguageModel chatLanguageModel;

    public CareerGuidanceAgent(ChatLanguageModel chatLanguageModel) {
        this.chatLanguageModel = chatLanguageModel;
    }

    @Override
    public AgentResponse process(AgentRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("Career Guidance Agent processing request for user: {}", request.getUserId());

        try {
            Map<String, Object> context = request.getContext();
            BirthChart chart = (BirthChart) context.get("chart");

            String response;
            if (chart != null) {
                response = generateLLMResponse(request.getQuery(), chart);
            } else {
                response = "I need your birth chart data to provide career guidance. Please ensure your birth profile is complete.";
            }

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("category", "career");

            long processingTime = System.currentTimeMillis() - startTime;

            return AgentResponse.builder()
                    .agentType(getAgentName())
                    .response(response)
                    .metadata(metadata)
                    .processingTimeMs(processingTime)
                    .build();

        } catch (Exception e) {
            log.error("Error in Career Guidance Agent", e);
            return AgentResponse.builder()
                    .agentType(getAgentName())
                    .response("I apologize, but I encountered an error providing career guidance. Please try again.")
                    .metadata(new HashMap<>())
                    .processingTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    private String generateLLMResponse(String query, BirthChart chart) {
        String chartSummary = summarizeChart(chart);
        try {
            String prompt = """
                    You are a Vedic astrology career counselor. Analyze the birth chart for career insights.
                    Focus on: 10th house (Karma Sthana), 2nd house, Atmakaraka, Saturn/Jupiter placement.
                    Keep response to 3-4 paragraphs, practical and insightful.

                    Birth Chart:
                    %s

                    User Query: "%s"
                    """.formatted(chartSummary, query);
            return chatLanguageModel.generate(prompt);
        } catch (Exception e) {
            log.warn("LLM fallback for career: {}", e.getMessage());
            return templateResponse();
        }
    }

    private String templateResponse() {
        return """
                Career Guidance Based on Your Birth Chart

                Your 10th house (Karma Sthana) analysis indicates strong career potential. The planetary influences suggest you are well-suited for roles involving leadership, communication, and analytical thinking.

                Favorable Career Paths:
                - Management and leadership roles
                - Education and teaching
                - Communication and media
                - Research and analysis
                - Entrepreneurship

                The current Dasha period supports career advancement. Focus on building your professional network and upgrading your skills. Favorable opportunities for promotions or new ventures are indicated in the coming months.

                Recommendation: Leverage your natural strengths in communication and analysis. Consider roles where you can lead teams or projects. Trust your judgment in professional matters.

                Disclaimer: This guidance is based on astrological analysis. Make career decisions based on your skills, qualifications, and market conditions.""";
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
        return "Career Guidance";
    }
}
