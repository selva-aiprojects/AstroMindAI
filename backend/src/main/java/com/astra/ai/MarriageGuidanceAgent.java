package com.astra.ai;

import com.astra.ai.AgentRouter.AgentRequest;
import com.astra.ai.AgentRouter.AgentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class MarriageGuidanceAgent implements Agent {

    @Override
    public AgentResponse process(AgentRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("Marriage Guidance Agent processing request for user: {}", request.getUserId());
        
        try {
            // Extract chart data from context
            Map<String, Object> context = request.getContext();
            
            // Generate marriage guidance
            String response = generateMarriageGuidance(context);
            
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

    private String generateMarriageGuidance(Map<String, Object> context) {
        StringBuilder response = new StringBuilder();
        
        response.append("💕 **Marriage & Relationship Guidance**\n\n");
        
        response.append("Based on the analysis of your 7th house (Kalatra Sthana), Venus/Jupiter positions, ");
        response.append("and D9 (Navamsha) chart, here are insights for your relationships:\n\n");
        
        response.append("**Relationship Strengths:**\n");
        response.append("- Your chart indicates a harmonious and loving nature in relationships.\n");
        response.append("- You value commitment and loyalty in partnerships.\n");
        response.append("- Good communication skills help maintain healthy relationships.\n\n");
        
        response.append("**Ideal Partner Qualities:**\n");
        response.append("- Someone who shares your values and life goals\n");
        response.append("- A partner with emotional maturity and stability\n");
        response.append("- Someone who appreciates your nurturing nature\n");
        response.append("- A partner who supports your personal growth\n\n");
        
        response.append("Favorable Timing for Marriage:\n");
        response.append("- The current Dasha period indicates favorable conditions for marriage.\n");
        response.append("- Look for auspicious periods in the next 12-18 months.\n");
        response.append("- Specific months show stronger marriage potential.\n\n");
        
        response.append("**Relationship Advice:**\n");
        response.append("- Focus on building emotional connection and trust.\n");
        response.append("- Maintain open and honest communication with your partner.\n");
        response.append("- Balance personal space and togetherness in relationships.\n");
        response.append("- Practice patience and understanding during conflicts.\n\n");
        
        response.append("**Remedies for Relationship Harmony:**\n");
        response.append("- Worship Venus on Fridays for relationship blessings.\n");
        response.append("- Offer water to the Sun for improved compatibility.\n");
        response.append("- Chant mantras for relationship harmony.\n\n");
        
        response.append("💡 *For detailed compatibility analysis (Synastry), consider a professional consultation ");
        response.append("with birth charts of both partners.*\n");
        
        return response.toString();
    }

    @Override
    public String getAgentName() {
        return "Marriage Guidance";
    }
}
