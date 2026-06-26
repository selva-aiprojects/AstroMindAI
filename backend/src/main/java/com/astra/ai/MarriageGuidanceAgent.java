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
                response = generateLLMResponse(request.getQuery(), chart, context);
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

    private String generateLLMResponse(String query, BirthChart chart, Map<String, Object> context) {
        String chartSummary = summarizeChart(chart);
        String languageInstruction = buildLanguageInstruction(context);
        try {
            String prompt = """
                    You are a Vedic astrology relationship counselor. Analyze the birth chart for marriage/relationship insights.
                    Focus on: 7th house (Kalatra Sthana), Venus, Jupiter, D9 (Navamsha).
                    Keep response to 3-4 paragraphs, warm and insightful.
                    %s

                    Birth Chart:
                    %s

                    User Query: "%s"
                    """.formatted(languageInstruction, chartSummary, query);
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
        return "Marriage Guidance";
    }
}
