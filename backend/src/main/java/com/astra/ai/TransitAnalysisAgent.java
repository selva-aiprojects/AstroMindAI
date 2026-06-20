package com.astra.ai;

import com.astra.ai.AgentRouter.AgentRequest;
import com.astra.ai.AgentRouter.AgentResponse;
import com.astra.astrology.TransitCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransitAnalysisAgent implements Agent {

    private final TransitCalculator transitCalculator;

    @Override
    public AgentResponse process(AgentRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("Transit Analysis Agent processing request for user: {}", request.getUserId());
        
        try {
            // Extract birth data from context
            Map<String, Object> context = request.getContext();
            Map<String, Object> natalPositions = (Map<String, Object>) context.get("planetaryPositions");
            
            // Calculate transits for current date
            TransitCalculator.TransitAnalysis analysis = transitCalculator.calculateTransits(
                    LocalDate.now(), 
                    (Map) natalPositions
            );
            
            // Generate response
            String response = generateTransitResponse(analysis);
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("analysis", analysis);
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            return AgentResponse.builder()
                    .agentType(getAgentName())
                    .response(response)
                    .metadata(metadata)
                    .processingTimeMs(processingTime)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error in Transit Analysis Agent", e);
            return AgentResponse.builder()
                    .agentType(getAgentName())
                    .response("I apologize, but I encountered an error analyzing current transits. Please try again.")
                    .metadata(new HashMap<>())
                    .processingTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    private String generateTransitResponse(TransitCalculator.TransitAnalysis analysis) {
        StringBuilder response = new StringBuilder();
        
        response.append("🌍 **Current Transit Analysis (Gochara)**\n\n");
        response.append("Date: ").append(analysis.getTransitDate()).append("\n\n");
        
        // Major transits
        if (!analysis.getMajorTransits().isEmpty()) {
            response.append("**Major Transits Active:**\n");
            for (Map.Entry<String, String> entry : analysis.getMajorTransits().entrySet()) {
                response.append(String.format("- **%s**: %s\n", entry.getKey(), entry.getValue()));
            }
            response.append("\n");
        }
        
        // Current planetary positions
        response.append("**Current Planetary Positions:**\n");
        for (Map.Entry<String, com.astra.astrology.SwissEphemerisService.PlanetPosition> entry : 
                analysis.getTransitPositions().entrySet()) {
            com.astra.astrology.SwissEphemerisService.PlanetPosition position = entry.getValue();
            response.append(String.format("- **%s**: %s in %s\n", 
                    entry.getKey(), 
                    position.getSign(), 
                    position.getNakshatra()));
        }
        
        response.append("\n**Key Aspects:**\n");
        for (Map.Entry<String, TransitCalculator.TransitAspect> entry : analysis.getAspects().entrySet()) {
            TransitCalculator.TransitAspect aspect = entry.getValue();
            if (aspect.getOrb() < 10) { // Only show close aspects
                response.append(String.format("- **%s**: %s aspect (orb: %.1f°)\n", 
                        entry.getKey(), 
                        aspect.getAspectType(), 
                        aspect.getOrb()));
            }
        }
        
        response.append("\n💡 *Transit analysis shows how current planetary movements affect your natal chart. ");
        response.append("Pay special attention to major transits involving Saturn, Jupiter, and the nodes (Rahu/Ketu).*\n");
        
        return response.toString();
    }

    @Override
    public String getAgentName() {
        return "Transit Analysis";
    }
}
