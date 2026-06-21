package com.astra.ai;

import com.astra.ai.AgentRouter.AgentRequest;
import com.astra.ai.AgentRouter.AgentResponse;
import com.astra.astrology.DashaCalculator;
import com.astra.astrology.SwissEphemerisService.BirthChart;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class DashaAnalysisAgent implements Agent {

    private final ChatLanguageModel chatLanguageModel;

    public DashaAnalysisAgent(ChatLanguageModel chatLanguageModel) {
        this.chatLanguageModel = chatLanguageModel;
    }

    @Override
    public AgentResponse process(AgentRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("Dasha Analysis Agent processing request for user: {}", request.getUserId());

        try {
            Map<String, Object> context = request.getContext();
            BirthChart chart = (BirthChart) context.get("chart");
            LocalDate birthDate = (LocalDate) context.get("birthDate");
            String moonNakshatra = (String) context.get("moonNakshatra");

            String response;
            if (birthDate != null) {
                DashaCalculator dashaCalc = new DashaCalculator();
                DashaCalculator.DashaTimeline timeline;
                
                if (chart != null && chart.getPlanetaryPositions() != null && chart.getPlanetaryPositions().containsKey("Moon")) {
                    double moonLongitude = chart.getPlanetaryPositions().get("Moon").getLongitude();
                    timeline = dashaCalc.calculateDashaTimeline(birthDate, moonLongitude);
                } else if (moonNakshatra != null) {
                    timeline = dashaCalc.calculateDashaTimeline(birthDate, moonNakshatra);
                } else {
                    timeline = null;
                }

                if (timeline != null) {
                    response = generateLLMResponse(request.getQuery(), timeline);
                } else {
                    response = "I need your birth chart and birth date to provide Dasha analysis. Please ensure your birth profile is complete.";
                }
            } else {
                response = "I need your birth chart and birth date to provide Dasha analysis. Please ensure your birth profile is complete.";
            }

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("category", "dasha");

            long processingTime = System.currentTimeMillis() - startTime;

            return AgentResponse.builder()
                    .agentType(getAgentName())
                    .response(response)
                    .metadata(metadata)
                    .processingTimeMs(processingTime)
                    .build();

        } catch (Exception e) {
            log.error("Error in Dasha Analysis Agent", e);
            return AgentResponse.builder()
                    .agentType(getAgentName())
                    .response("I apologize, but I encountered an error analyzing your Dasha periods. Please try again.")
                    .metadata(new HashMap<>())
                    .processingTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    private String generateLLMResponse(String query, DashaCalculator.DashaTimeline timeline) {
        StringBuilder timelineSummary = new StringBuilder();
        if (timeline.getCurrentMahadasha() != null) {
            timelineSummary.append("Current Mahadasha: ").append(timeline.getCurrentMahadasha().getLord())
                    .append(" (").append(timeline.getCurrentMahadasha().getStartDate())
                    .append(" to ").append(timeline.getCurrentMahadasha().getEndDate()).append(")\n");
        }
        if (timeline.getCurrentAntardasha() != null) {
            timelineSummary.append("Current Antardasha: ").append(timeline.getCurrentAntardasha().getLord())
                    .append(" (").append(timeline.getCurrentAntardasha().getStartDate())
                    .append(" to ").append(timeline.getCurrentAntardasha().getEndDate()).append(")\n");
        }

        try {
            String prompt = """
                    You are a Vedic astrology Dasha expert. Interpret this Vimshottari Dasha timeline.
                    Explain the current Mahadasha and Antardasha influences, and what themes they activate.
                    Keep response to 3-4 paragraphs.

                    Dasha Timeline:
                    %s

                    User Query: "%s"
                    """.formatted(timelineSummary.toString(), query);
            return chatLanguageModel.generate(prompt);
        } catch (Exception e) {
            log.warn("LLM fallback for dasha: {}", e.getMessage());
            return templateResponse(timeline);
        }
    }

    private String templateResponse(DashaCalculator.DashaTimeline timeline) {
        StringBuilder sb = new StringBuilder("Vimshottari Dasha Analysis\n\n");
        if (timeline.getCurrentMahadasha() != null) {
            var md = timeline.getCurrentMahadasha();
            sb.append("Current Mahadasha: ").append(md.getLord()).append("\n");
            sb.append("Period: ").append(md.getStartDate()).append(" to ").append(md.getEndDate()).append("\n\n");
            sb.append("The ").append(md.getLord()).append(" Mahadasha brings themes of ")
              .append(getLordTheme(md.getLord())).append("\n\n");
        }
        if (timeline.getCurrentAntardasha() != null) {
            var ad = timeline.getCurrentAntardasha();
            sb.append("Current Antardasha: ").append(ad.getLord()).append("\n");
            sb.append("This sub-period modifies the Mahadasha with ").append(getLordTheme(ad.getLord())).append("\n\n");
        }
        return sb.toString();
    }

    private String getLordTheme(String lord) {
        return switch (lord) {
            case "Ketu" -> "spiritual growth, detachment, and liberation.";
            case "Venus" -> "love, beauty, comfort, and material prosperity.";
            case "Sun" -> "authority, recognition, and leadership.";
            case "Moon" -> "emotional experiences, mental peace, and domestic happiness.";
            case "Mars" -> "energy, courage, and potential conflicts.";
            case "Rahu" -> "intense desires, material gains, and transformation.";
            case "Jupiter" -> "wisdom, fortune, spiritual growth, and expansion.";
            case "Saturn" -> "discipline, hard work, delays, and karmic lessons.";
            case "Mercury" -> "intellectual growth, communication, and versatility.";
            default -> lord + " influences.";
        };
    }

    @Override
    public String getAgentName() {
        return "Dasha Analysis";
    }
}
