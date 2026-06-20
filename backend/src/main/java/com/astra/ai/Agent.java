package com.astra.ai;

import com.astra.ai.AgentRouter.AgentRequest;
import com.astra.ai.AgentRouter.AgentResponse;

public interface Agent {
    AgentResponse process(AgentRequest request);
    String getAgentName();
}
