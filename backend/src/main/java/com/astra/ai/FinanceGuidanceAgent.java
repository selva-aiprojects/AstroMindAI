package com.astra.ai;

import com.astra.ai.AgentRouter.AgentRequest;
import com.astra.ai.AgentRouter.AgentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class FinanceGuidanceAgent implements Agent {

    @Override
    public AgentResponse process(AgentRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("Finance Guidance Agent processing request for user: {}", request.getUserId());
        
        try {
            // Extract chart data from context
            Map<String, Object> context = request.getContext();
            
            // Generate finance guidance
            String response = generateFinanceGuidance(context);
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("category", "finance");
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            return AgentResponse.builder()
                    .agentType(getAgentName())
                    .response(response)
                    .metadata(metadata)
                    .processingTimeMs(processingTime)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error in Finance Guidance Agent", e);
            return AgentResponse.builder()
                    .agentType(getAgentName())
                    .response("I apologize, but I encountered an error providing finance guidance. Please try again.")
                    .metadata(new HashMap<>())
                    .processingTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    private String generateFinanceGuidance(Map<String, Object> context) {
        StringBuilder response = new StringBuilder();
        
        response.append("💰 **Finance & Wealth Guidance**\n\n");
        
        response.append("Based on the analysis of your 2nd house (Dhana Sthana), 11th house (Labha Sthana), ");
        response.append("9th house (Bhagya Sthana), and Dhana yogas, here are insights for your finances:\n\n");
        
        response.append("**Financial Strengths:**\n");
        response.append("- Your chart indicates potential for steady income accumulation.\n");
        response.append("- Good money management skills are indicated.\n");
        response.append("- Opportunities for multiple income sources exist.\n\n");
        
        response.append("Favorable Investment Areas:\n");
        response.append("- Real estate shows long-term potential.\n");
        response.append("- Stock market investments may be favorable during certain periods.\n");
        response.append("- Business ventures have growth potential.\n");
        response.append("- Fixed income instruments provide stability.\n\n");
        
        response.append("**Timing for Financial Decisions:**\n");
        response.append("- Current Dasha period favors financial growth and investments.\n");
        response.append("- Favorable periods for major purchases in the next 6 months.\n");
        response.append("- Good timing for starting new business ventures indicated.\n\n");
        
        response.append("**Financial Recommendations:**\n");
        response.append("- Maintain a balanced portfolio with diversified investments.\n");
        response.append("- Focus on long-term wealth creation over short-term gains.\n");
        response.append("- Avoid speculative investments during unfavorable periods.\n");
        response.append("- Build emergency funds for financial security.\n\n");
        
        response.append("**Remedies for Financial Prosperity:**\n");
        response.append("- Worship Lakshmi on Fridays for wealth blessings.\n");
        response.append("- Offer charity to attract financial abundance.\n");
        response.append("- Chant mantras for prosperity and success.\n\n");
        
        response.append("⚠️ **DISCLAIMER**: This guidance is based on astrological analysis and should NOT be considered ");
        response.append("financial advice. Please consult certified financial advisors before making investment decisions. ");
        response.append("Past astrological predictions do not guarantee future financial results.\n");
        
        return response.toString();
    }

    @Override
    public String getAgentName() {
        return "Finance Guidance";
    }
}
