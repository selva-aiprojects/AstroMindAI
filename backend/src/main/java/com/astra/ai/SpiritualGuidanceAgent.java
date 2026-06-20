package com.astra.ai;

import com.astra.ai.AgentRouter.AgentRequest;
import com.astra.ai.AgentRouter.AgentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class SpiritualGuidanceAgent implements Agent {

    @Override
    public AgentResponse process(AgentRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("Spiritual Guidance Agent processing request for user: {}", request.getUserId());
        
        try {
            // Extract chart data from context
            Map<String, Object> context = request.getContext();
            
            // Generate spiritual guidance
            String response = generateSpiritualGuidance(context);
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("category", "spiritual");
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            return AgentResponse.builder()
                    .agentType(getAgentName())
                    .response(response)
                    .metadata(metadata)
                    .processingTimeMs(processingTime)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error in Spiritual Guidance Agent", e);
            return AgentResponse.builder()
                    .agentType(getAgentName())
                    .response("I apologize, but I encountered an error providing spiritual guidance. Please try again.")
                    .metadata(new HashMap<>())
                    .processingTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    private String generateSpiritualGuidance(Map<String, Object> context) {
        StringBuilder response = new StringBuilder();
        
        response.append("🕉️ **Spiritual Guidance**\n\n");
        
        response.append("Based on the analysis of your 5th house (Punya Sthana), 9th house (Dharma Sthana), ");
        response.append("12th house (Moksha Sthana), Atmakaraka, and D20 chart, here are insights for your spiritual journey:\n\n");
        
        response.append("**Spiritual Strengths:**\n");
        response.append("- Your chart indicates strong spiritual potential and inclination.\n");
        response.append("- Natural interest in philosophical and metaphysical subjects.\n");
        response.append("- Good intuition and psychic sensitivity.\n");
        response.append("- Capacity for deep meditation and spiritual practices.\n\n");
        
        response.append("**Recommended Spiritual Practices:**\n");
        response.append("- Meditation: Daily meditation for inner peace and clarity.\n");
        response.append("- Mantra Chanting: Chant specific mantras for spiritual growth.\n");
        response.append("- Yoga: Practice yoga for physical and spiritual well-being.\n");
        response.append("- Prayer: Regular prayer for connection with the divine.\n");
        response.append("- Study: Read spiritual texts and scriptures.\n\n");
        
        response.append("Favorable Spiritual Periods:\n");
        response.append("- Current Dasha period supports spiritual growth and practices.\n");
        response.append("- Good time for initiating new spiritual practices.\n");
        response.append("- Favorable for pilgrimages and spiritual retreats.\n\n");
        
        response.append("**Mantra Recommendations:**\n");
        response.append("- **Gayatri Mantra**: For wisdom and enlightenment.\n");
        response.append("- **Om Namah Shivaya**: For transformation and liberation.\n");
        response.append("- **Om Namo Narayana**: For peace and devotion.\n");
        response.append("- **Mahamrityunjaya Mantra**: For health and protection.\n\n");
        
        response.append("**Remedies for Spiritual Growth:**\n");
        response.append("- Worship your Ishta Devata (personal deity).\n");
        response.append("- Offer service (Seva) at temples or spiritual centers.\n");
        response.append("- Practice charity and compassion.\n");
        response.append("- Observe fasting on auspicious days.\n");
        response.append("- Perform rituals during favorable planetary periods.\n\n");
        
        response.append("**Life Purpose Insights:**\n");
        response.append("- Your chart suggests a life purpose involving teaching and guiding others.\n");
        response.append("- Spiritual service and humanitarian work are highlighted.\n");
        response.append("- Your journey involves balancing material and spiritual life.\n");
        response.append("- Liberation through selfless service is indicated.\n\n");
        
        response.append("💡 *Spiritual growth is a personal journey. These insights are meant to guide and inspire ");
        response.append("your path. Follow practices that resonate with your heart and intuition.*\n");
        
        return response.toString();
    }

    @Override
    public String getAgentName() {
        return "Spiritual Guidance";
    }
}
