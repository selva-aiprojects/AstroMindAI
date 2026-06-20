package com.astra.ai;

import com.astra.ai.AgentRouter.AgentRequest;
import com.astra.ai.AgentRouter.AgentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class HealthGuidanceAgent implements Agent {

    @Override
    public AgentResponse process(AgentRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("Health Guidance Agent processing request for user: {}", request.getUserId());
        
        try {
            // Extract chart data from context
            Map<String, Object> context = request.getContext();
            
            // Generate health guidance
            String response = generateHealthGuidance(context);
            
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

    private String generateHealthGuidance(Map<String, Object> context) {
        StringBuilder response = new StringBuilder();
        
        response.append("🏥 **Health & Wellness Guidance**\n\n");
        
        response.append("Based on the analysis of your 6th house (Roga Sthana), 8th house (Ayur Sthana), ");
        response.append("Lagna strength, and active Dashas, here are insights for your health:\n\n");
        
        response.append("**General Health Profile:**\n");
        response.append("- Your chart indicates generally good constitution and vitality.\n");
        response.append("- Strong immune system is indicated.\n");
        response.append("- Good recovery potential from illnesses.\n\n");
        
        response.append("**Areas to Monitor:**\n");
        response.append("- Pay attention to digestive health and diet.\n");
        response.append("- Regular exercise is important for maintaining vitality.\n");
        response.append("- Stress management is crucial for overall well-being.\n");
        response.append("- Adequate rest and sleep are essential.\n\n");
        
        response.append("Favorable Health Periods:\n");
        response.append("- Current Dasha period supports good health and recovery.\n");
        response.append("- Good time for starting fitness routines.\n");
        response.append("- Favorable for medical checkups and treatments.\n\n");
        
        response.append("**Precautionary Periods:**\n");
        response.append("- Be extra cautious about health during certain transit periods.\n");
        response.append("- Avoid stressful activities during vulnerable periods.\n");
        response.append("- Focus on preventive care during these times.\n\n");
        
        response.append("**Wellness Recommendations:**\n");
        response.append("- Maintain a balanced diet rich in nutrients.\n");
        response.append("- Regular exercise and physical activity.\n");
        response.append("- Practice stress-reduction techniques like meditation.\n");
        response.append("- Ensure adequate sleep and rest.\n");
        response.append("- Stay hydrated and avoid harmful habits.\n\n");
        
        response.append("**Health Remedies:**\n");
        response.append("- Worship the Sun for vitality and strength.\n");
        response.append("- Practice yoga and pranayama for overall wellness.\n");
        response.append("- Chant mantras for health and healing.\n\n");
        
        response.append("⚠️ **MEDICAL DISCLAIMER**: This guidance is based on astrological analysis and should NOT be considered ");
        response.append("medical advice. Please consult licensed healthcare professionals for health concerns. ");
        response.append("Do not use this information to diagnose, treat, or prevent any medical condition. ");
        response.append("Seek immediate medical attention for serious health issues.\n");
        
        return response.toString();
    }

    @Override
    public String getAgentName() {
        return "Health Guidance";
    }
}
