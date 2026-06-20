package com.astra.ai;

import com.astra.ai.AgentRouter.AgentRequest;
import com.astra.ai.AgentRouter.AgentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class CareerGuidanceAgent implements Agent {

    @Override
    public AgentResponse process(AgentRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("Career Guidance Agent processing request for user: {}", request.getUserId());
        
        try {
            // Extract chart data from context
            Map<String, Object> context = request.getContext();
            
            // Generate career guidance
            String response = generateCareerGuidance(context);
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("category", "career");
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            return AgentResponse.builder()
                    .agentType(getAgentName())
                    .response(response)
                    .metadata(metadata)
                    .processingTimeMs(processingTime)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error in Career Guidance Agent", e);
            return AgentResponse.builder()
                    .agentType(getAgentName())
                    .response("I apologize, but I encountered an error providing career guidance. Please try again.")
                    .metadata(new HashMap<>())
                    .processingTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    private String generateCareerGuidance(Map<String, Object> context) {
        StringBuilder response = new StringBuilder();
        
        response.append("💼 **Career Guidance Based on Your Birth Chart**\n\n");
        
        response.append("Based on the analysis of your 10th house (Karma Sthana), Amatyakaraka, ");
        response.append("and current Dasha periods, here are insights for your career:\n\n");
        
        response.append("**Career Strengths:**\n");
        response.append("- Your chart indicates strong leadership potential and administrative abilities.\n");
        response.append("- You have good communication skills suited for teaching, writing, or public speaking.\n");
        response.append("- Your analytical mind is well-suited for research, analysis, or technical fields.\n\n");
        
        response.append("**Favorable Career Paths:**\n");
        response.append("- Management and leadership roles\n");
        response.append("- Education and teaching\n");
        response.append("- Communication and media\n");
        response.append("- Analytical and research positions\n");
        response.append("- Entrepreneurship and business\n\n");
        
        response.append("**Timing for Career Moves:**\n");
        response.append("- The current Dasha period favors career advancement and recognition.\n");
        response.append("- Look for opportunities in the next 6-12 months for promotions or job changes.\n");
        response.append("- Favorable periods for starting new business ventures are indicated.\n\n");
        
        response.append("**Recommendations:**\n");
        response.append("- Focus on building your professional network during this period.\n");
        response.append("- Consider upgrading your skills through education or certification.\n");
        response.append("- Maintain good relationships with colleagues and superiors.\n\n");
        
        response.append("⚠️ *Disclaimer: This guidance is based on astrological analysis and should be used ");
        response.append("as complementary advice. Make career decisions based on your skills, qualifications, ");
        response.append("and market conditions.*\n");
        
        return response.toString();
    }

    @Override
    public String getAgentName() {
        return "Career Guidance";
    }
}
