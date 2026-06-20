package com.astra.ai;

import com.astra.ai.AgentRouter.AgentRequest;
import com.astra.ai.AgentRouter.AgentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class PlanetAnalysisAgent implements Agent {

    @Override
    public AgentResponse process(AgentRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("Planet Analysis Agent processing request for user: {}", request.getUserId());
        
        try {
            // Extract chart data from context
            Map<String, Object> context = request.getContext();
            Map<String, Object> chartData = (Map<String, Object>) context.get("chart");
            
            // Generate planet analysis
            String response = generatePlanetAnalysis(chartData);
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("analysisType", "planet_strength");
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            return AgentResponse.builder()
                    .agentType(getAgentName())
                    .response(response)
                    .metadata(metadata)
                    .processingTimeMs(processingTime)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error in Planet Analysis Agent", e);
            return AgentResponse.builder()
                    .agentType(getAgentName())
                    .response("I apologize, but I encountered an error analyzing planetary positions. Please try again.")
                    .metadata(new HashMap<>())
                    .processingTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    private String generatePlanetAnalysis(Map<String, Object> chartData) {
        StringBuilder response = new StringBuilder();
        
        response.append("🪐 **Planetary Strength Analysis**\n\n");
        response.append("Based on your birth chart, here's an analysis of planetary strengths:\n\n");
        
        // Placeholder analysis - in production, use actual Shadbala calculations
        response.append("**Sun:** Represents your soul, vitality, and father figure. ");
        response.append("Strong Sun indicates leadership qualities and good health.\n\n");
        
        response.append("**Moon:** Represents your mind, emotions, and mother figure. ");
        response.append("Strong Moon indicates emotional stability and mental peace.\n\n");
        
        response.append("**Mars:** Represents energy, courage, and siblings. ");
        response.append("Strong Mars indicates determination and physical strength.\n\n");
        
        response.append("**Mercury:** Represents intelligence, communication, and analytical abilities. ");
        response.append("Strong Mercury indicates sharp intellect and good communication skills.\n\n");
        
        response.append("**Jupiter:** Represents wisdom, fortune, and teacher. ");
        response.append("Strong Jupiter indicates luck, spiritual growth, and prosperity.\n\n");
        
        response.append("**Venus:** Represents love, beauty, and relationships. ");
        response.append("Strong Venus indicates artistic abilities and harmonious relationships.\n\n");
        
        response.append("**Saturn:** Represents discipline, karma, and hard work. ");
        response.append("Strong Saturn indicates perseverance and long-term success through hard work.\n\n");
        
        response.append("**Rahu:** Represents desires, materialism, and foreign influences. ");
        response.append("Its placement indicates areas of intense focus and potential obsession.\n\n");
        
        response.append("**Ketu:** Represents spirituality, detachment, and past life karma. ");
        response.append("Its placement indicates areas of spiritual growth and liberation.\n\n");
        
        response.append("💡 *Note: For detailed Shadbala calculations and specific strength scores, ");
        response.append("consult with a professional Vedic astrologer.*\n");
        
        return response.toString();
    }

    @Override
    public String getAgentName() {
        return "Planet Analysis";
    }
}
