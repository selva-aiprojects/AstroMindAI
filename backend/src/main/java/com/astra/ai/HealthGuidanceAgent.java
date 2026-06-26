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
public class HealthGuidanceAgent implements Agent {

    private final ChatLanguageModel chatLanguageModel;

    public HealthGuidanceAgent(ChatLanguageModel chatLanguageModel) {
        this.chatLanguageModel = chatLanguageModel;
    }

    @Override
    public AgentResponse process(AgentRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("Health Guidance Agent processing request for user: {}", request.getUserId());

        try {
            Map<String, Object> context = request.getContext();
            BirthChart chart = (BirthChart) context.get("chart");

            String response;
            if (chart != null) {
                response = generateLLMResponse(request.getQuery(), chart, context);
            } else {
                response = "I need your birth chart data to provide health guidance. Please ensure your birth profile is complete.";
            }

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("category", "health");

            long processingTime = System.currentTimeMillis() - startTime;

            return AgentResponse.builder()
                    .agentType(getAgentName())
                    .response(response)
                    .metadata(metadata)
                    .processingTimeMs(processingTime)
                    .build();

        } catch (Exception e) {
            log.error("Error in Health Guidance Agent", e);
            return AgentResponse.builder()
                    .agentType(getAgentName())
                    .response("I apologize, but I encountered an error providing health guidance. Please try again.")
                    .metadata(new HashMap<>())
                    .processingTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    private String generateLLMResponse(String query, BirthChart chart, Map<String, Object> context) {
        String chartSummary = summarizeChart(chart);
        String languageInstruction = buildLanguageInstruction(context);
        try {
            String prompt = """
                    You are a Vedic astrology health counselor. Analyze the birth chart for health insights.
                    Focus on: 6th house (Roga Sthana), 8th house (Ayur Sthana), Lagna strength, Sun/Moon placement.
                    Keep response to 3-4 paragraphs, caring and practical. Include wellness recommendations.
                    %s

                    Birth Chart:
                    %s

                    User Query: "%s"
                    """.formatted(languageInstruction, chartSummary, query);
            return chatLanguageModel.generate(prompt);
        } catch (Exception e) {
            log.warn("LLM fallback for health: {}", e.getMessage());
            return templateResponse();
        }
    }

    private String templateResponse() {
        return """
                Health & Wellness Guidance

                Your Lagna is strong, indicating good overall health and vitality. The chart shows good recovery potential and a generally strong constitution.

                Areas to Monitor: Pay attention to digestive health. Regular exercise is important for maintaining vitality. Stress management through meditation or yoga is recommended.

                The current Dasha period supports good health. This is a favorable time for starting fitness routines and health checkups.

                Recommendations: Maintain a balanced diet, practice stress-reduction techniques, ensure adequate sleep, stay hydrated. Consider yoga and pranayama for overall wellness.

                Disclaimer: This guidance is based on astrological analysis. Consult licensed healthcare professionals for medical concerns.""";
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

    private String buildLanguageInstruction(Map<String, Object> context) {
        if (context == null || !context.containsKey("language")) return "";
        String lang = context.get("language").toString();
        return switch (lang) {
            case "hi" -> "IMPORTANT: You MUST respond entirely in Hindi (हिन्दी). Every word of your response must be in Hindi.";
            case "ta" -> "IMPORTANT: You MUST respond entirely in Tamil (தமிழ்). Every word of your response must be in Tamil.";
            case "te" -> "IMPORTANT: You MUST respond entirely in Telugu (తెలుగు). Every word of your response must be in Telugu.";
            case "mr" -> "IMPORTANT: You MUST respond entirely in Marathi (मराठी). Every word of your response must be in Marathi.";
            case "bn" -> "IMPORTANT: You MUST respond entirely in Bengali (বাংলা). Every word of your response must be in Bengali.";
            case "gu" -> "IMPORTANT: You MUST respond entirely in Gujarati (ગુજરાતી). Every word of your response must be in Gujarati.";
            case "kn" -> "IMPORTANT: You MUST respond entirely in Kannada (ಕನ್ನಡ). Every word of your response must be in Kannada.";
            case "ml" -> "IMPORTANT: You MUST respond entirely in Malayalam (മലയാളം). Every word of your response must be in Malayalam.";
            case "pa" -> "IMPORTANT: You MUST respond entirely in Punjabi (ਪੰਜਾਬੀ). Every word of your response must be in Punjabi.";
            case "or" -> "IMPORTANT: You MUST respond entirely in Odia (ଓଡ଼ିଆ). Every word of your response must be in Odia.";
            case "as" -> "IMPORTANT: You MUST respond entirely in Assamese (অসমীয়া). Every word of your response must be in Assamese.";
            case "ur" -> "IMPORTANT: You MUST respond entirely in Urdu (اردو). Every word of your response must be in Urdu.";
            case "sa" -> "IMPORTANT: You MUST respond entirely in Sanskrit (संस्कृत). Every word of your response must be in Sanskrit.";
            default -> "";
        };
    }

    private String signName(double lon) {
        String[] s = {"Aries","Taurus","Gemini","Cancer","Leo","Virgo","Libra","Scorpio","Sagittarius","Capricorn","Aquarius","Pisces"};
        return s[(int)(lon/30)];
    }

    @Override
    public String getAgentName() {
        return "Health Guidance";
    }
}
