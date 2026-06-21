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
public class SpiritualGuidanceAgent implements Agent {

    private final ChatLanguageModel chatLanguageModel;

    public SpiritualGuidanceAgent(ChatLanguageModel chatLanguageModel) {
        this.chatLanguageModel = chatLanguageModel;
    }

    @Override
    public AgentResponse process(AgentRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("Spiritual Guidance Agent processing request for user: {}", request.getUserId());

        try {
            Map<String, Object> context = request.getContext();
            BirthChart chart = (BirthChart) context.get("chart");

            String response;
            if (chart != null) {
                response = generateLLMResponse(request.getQuery(), chart);
            } else {
                response = "I need your birth chart data to provide spiritual guidance. Please ensure your birth profile is complete.";
            }

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("category", "spiritual");

            long processingTime = System.currentTimeMillis() - startTime;

            return AgentResponse.builder()
                    .agentType(getAgentName())
                    .response(response)
                    .metadata(metadata)
                    .processingTimeMs(processingTime)
                    .build();

        } catch (Exception e) {
            log.error("Error in Spiritual Guidance Agent", e);
            return AgentResponse.builder()
                    .agentType(getAgentName())
                    .response("I apologize, but I encountered an error providing spiritual guidance. Please try again.")
                    .metadata(new HashMap<>())
                    .processingTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    private String generateLLMResponse(String query, BirthChart chart) {
        String chartSummary = summarizeChart(chart);
        try {
            String prompt = """
                    You are a Vedic astrology spiritual guide. Analyze the birth chart for spiritual insights.
                    Focus on: 5th house (Punya Sthana), 9th house (Dharma Sthana), 12th house (Moksha Sthana), Atmakaraka, Ketu.
                    Keep response to 3-4 paragraphs, warm and inspiring.

                    Birth Chart:
                    %s

                    User Query: "%s"
                    """.formatted(chartSummary, query);
            return chatLanguageModel.generate(prompt);
        } catch (Exception e) {
            log.warn("LLM fallback for spiritual: {}", e.getMessage());
            return templateResponse();
        }
    }

    private String templateResponse() {
        return """
                Spiritual Guidance

                Your chart shows strong spiritual potential. The 5th and 9th houses indicate favorable conditions for spiritual growth. Natural interest in philosophical subjects and good intuition are indicated.

                Recommended Practices: Daily meditation for inner peace and clarity. Chant mantras for spiritual growth - Gayatri Mantra for wisdom, Om Namah Shivaya for transformation. Practice yoga for physical and spiritual well-being.

                The current Dasha period supports spiritual practices. This is a favorable time for initiating new spiritual practices, pilgrimages, or retreats.

                Your chart suggests a life purpose involving guiding others. Your journey involves balancing material and spiritual life. Follow practices that resonate with your heart and intuition.""";
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
        return "Spiritual Guidance";
    }
}
