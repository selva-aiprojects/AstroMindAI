package com.astra.ai;

import com.astra.ai.AgentRouter.AgentRequest;
import com.astra.ai.AgentRouter.AgentResponse;
import com.astra.astrology.DashaCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DashaAnalysisAgent implements Agent {

    private final DashaCalculator dashaCalculator;

    @Override
    public AgentResponse process(AgentRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("Dasha Analysis Agent processing request for user: {}", request.getUserId());
        
        try {
            // Extract birth data from context
            Map<String, Object> context = request.getContext();
            LocalDate birthDate = (LocalDate) context.get("birthDate");
            String moonNakshatra = (String) context.get("moonNakshatra");
            
            // Calculate Dasha timeline
            DashaCalculator.DashaTimeline timeline = dashaCalculator.calculateDashaTimeline(birthDate, moonNakshatra);
            
            // Generate response
            String response = generateDashaResponse(timeline);
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("timeline", timeline);
            
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

    private String generateDashaResponse(DashaCalculator.DashaTimeline timeline) {
        StringBuilder response = new StringBuilder();
        
        response.append("⏳ **Vimshottari Dasha Analysis**\n\n");
        
        if (timeline.getCurrentMahadasha() != null) {
            DashaCalculator.DashaPeriod currentMahadasha = timeline.getCurrentMahadasha();
            response.append("**Current Mahadasha:** ").append(currentMahadasha.getLord()).append("\n");
            response.append("Period: ").append(currentMahadasha.getStartDate()).append(" to ").append(currentMahadasha.getEndDate()).append("\n");
            response.append("Duration: ").append(currentMahadasha.getYears()).append(" years\n\n");
            
            response.append(generateMahadashaInterpretation(currentMahadasha.getLord()));
        }
        
        if (timeline.getCurrentAntardasha() != null) {
            DashaCalculator.DashaPeriod currentAntardasha = timeline.getCurrentAntardasha();
            response.append("\n**Current Antardasha:** ").append(currentAntardasha.getLord()).append("\n");
            response.append("Period: ").append(currentAntardasha.getStartDate()).append(" to ").append(currentAntardasha.getEndDate()).append("\n\n");
            
            response.append(generateAntardashaInterpretation(currentAntardasha.getLord()));
        }
        
        response.append("\n📊 **Upcoming Mahadashas:**\n");
        for (DashaCalculator.DashaPeriod dasha : timeline.getMahadashas()) {
            if (dasha.getStartDate().isAfter(LocalDate.now())) {
                response.append(String.format("- **%s**: %s to %s (%d years)\n", 
                        dasha.getLord(), 
                        dasha.getStartDate(), 
                        dasha.getEndDate(), 
                        dasha.getYears()));
            }
        }
        
        return response.toString();
    }

    private String generateMahadashaInterpretation(String lord) {
        return switch (lord) {
            case "Ketu" -> "Ketu Mahadasha brings spiritual growth, detachment, and liberation. ";
            case "Venus" -> "Venus Mahadasha brings love, beauty, comfort, and material prosperity. ";
            case "Sun" -> "Sun Mahadasha brings authority, recognition, and leadership opportunities. ";
            case "Moon" -> "Moon Mahadasha brings emotional experiences, mental peace, and domestic happiness. ";
            case "Mars" -> "Mars Mahadasha brings energy, courage, and potential conflicts. ";
            case "Rahu" -> "Rahu Mahadasha brings intense desires, material gains, and transformative experiences. ";
            case "Jupiter" -> "Jupiter Mahadasha brings wisdom, fortune, spiritual growth, and expansion. ";
            case "Saturn" -> "Saturn Mahadasha brings discipline, hard work, delays, and karmic lessons. ";
            case "Mercury" -> "Mercury Mahadasha brings intellectual growth, communication skills, and versatility. ";
            default -> "This period will be influenced by the characteristics of " + lord + ". ";
        };
    }

    private String generateAntardashaInterpretation(String lord) {
        return "The " + lord + " Antardasha modifies the themes of your current Mahadasha with its specific qualities. ";
    }

    @Override
    public String getAgentName() {
        return "Dasha Analysis";
    }
}
