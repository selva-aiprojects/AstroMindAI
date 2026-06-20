package com.astra.ai;

import com.astra.ai.AgentRouter.AgentRequest;
import com.astra.ai.AgentRouter.AgentResponse;
import com.astra.astrology.SwissEphemerisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChartGeneratorAgent implements Agent {

    private final SwissEphemerisService swissEphemerisService;

    @Override
    public AgentResponse process(AgentRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("Chart Generator Agent processing request for user: {}", request.getUserId());
        
        try {
            // Extract birth data from context
            Map<String, Object> context = request.getContext();
            LocalDate birthDate = (LocalDate) context.get("birthDate");
            LocalTime birthTime = (LocalTime) context.get("birthTime");
            Double latitude = (Double) context.get("latitude");
            Double longitude = (Double) context.get("longitude");
            String timezone = (String) context.get("timezone");
            String ayanamsa = (String) context.getOrDefault("ayanamsa", "LAHIRI");
            
            // Calculate birth chart
            SwissEphemerisService.BirthChart chart = swissEphemerisService.calculateBirthChart(
                    birthDate, birthTime, latitude, longitude, timezone, ayanamsa
            );
            
            // Generate response
            String response = generateChartResponse(chart);
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("chart", chart);
            metadata.put("ayanamsa", ayanamsa);
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            return AgentResponse.builder()
                    .agentType(getAgentName())
                    .response(response)
                    .metadata(metadata)
                    .processingTimeMs(processingTime)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error in Chart Generator Agent", e);
            return AgentResponse.builder()
                    .agentType(getAgentName())
                    .response("I apologize, but I encountered an error generating your birth chart. Please ensure your birth details are correct.")
                    .metadata(new HashMap<>())
                    .processingTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    private String generateChartResponse(SwissEphemerisService.BirthChart chart) {
        StringBuilder response = new StringBuilder();
        
        response.append("🌟 **Your Birth Chart Analysis**\n\n");
        response.append("**Ascendant (Lagna):** ").append(calculateSign(chart.getAscendant())).append("\n\n");
        response.append("**Planetary Positions:**\n");
        
        for (Map.Entry<String, SwissEphemerisService.PlanetPosition> entry : chart.getPlanetaryPositions().entrySet()) {
            SwissEphemerisService.PlanetPosition position = entry.getValue();
            response.append(String.format("- **%s**: %s (%s) in House %d", 
                    entry.getKey(), 
                    position.getSign(), 
                    position.getNakshatra(),
                    chart.getHousePlacements().get(entry.getKey())));
            
            if (position.isRetrograde()) {
                response.append(" [Retrograde]");
            }
            if (position.isCombust()) {
                response.append(" [Combust]");
            }
            response.append("\n");
        }
        
        response.append("\n**Functional Nature:**\n");
        for (Map.Entry<String, String> entry : chart.getFunctionalNature().entrySet()) {
            response.append(String.format("- **%s**: %s\n", entry.getKey(), entry.getValue()));
        }
        
        response.append(String.format("\n**Ayanamsa:** %s\n", chart.getAyanamsa()));
        
        return response.toString();
    }

    private String calculateSign(double longitude) {
        int signIndex = (int)(longitude / 30);
        String[] signs = {
            "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
            "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
        };
        return signs[signIndex];
    }

    @Override
    public String getAgentName() {
        return "Chart Generator";
    }
}
