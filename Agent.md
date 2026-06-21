# AI Agent Architecture — AstroMindAI

## Overview

AstroMindAI uses a **multi-agent AI architecture** with 10 specialized agents. A central `AgentRouter` classifies user intent (via LLM or keyword matching) and delegates to the appropriate agent. Each agent uses LangChain4j's `ChatLanguageModel` to generate responses with full birth chart context.

## Agent Interface

All agents implement `Agent` (`backend/src/main/java/com/astra/ai/Agent.java`):

```java
public interface Agent {
    AgentResponse process(AgentRequest request);
    String getAgentName();
}
```

## The 10 Agents

| Agent Key | Class | Purpose |
|-----------|-------|---------|
| `chart_generator` | `ChartGeneratorAgent` | Generate and interpret birth charts |
| `planet_analysis` | `PlanetAnalysisAgent` | Analyze planetary positions, strengths, retrogrades |
| `dasha_analysis` | `DashaAnalysisAgent` | Calculate and interpret Vimshottari Dasha periods |
| `transit_analysis` | `TransitAnalysisAgent` | Current transit effects (Gochara) |
| `career_guidance` | `CareerGuidanceAgent` | Career, job, profession, business insights |
| `marriage_guidance` | `MarriageGuidanceAgent` | Marriage, relationship, spouse, love |
| `finance_guidance` | `FinanceGuidanceAgent` | Finance, wealth, investments, income |
| `health_guidance` | `HealthGuidanceAgent` | Health, wellness, disease, illness |
| `spiritual_guidance` | `SpiritualGuidanceAgent` | Meditation, mantras, remedies, puja |
| `memory_agent` | `MemoryAgent` | Conversation memory (in-memory per user) |

## Routing Logic

`AgentRouter.routeQuery()` (`backend/src/main/java/com/astra/ai/AgentRouter.java`):

1. **LLM Classification** (primary): Sends query to `ChatLanguageModel` with prompt asking to classify into one of 9 categories
2. **Keyword Fallback** (secondary): If LLM unavailable, uses `string.contains()` keyword matching
3. Delegates to matched agent, falls back to `chart_generator`
4. Attaches `MemoryAgent` for conversation history
5. Returns `AgentResponse` with agentType, response, metadata, processingTimeMs

## LangChain4j Integration

Each agent receives a `ChatLanguageModel` injected via constructor. When an LLM API key is configured (OpenAI/Anthropic/Gemini), the agent:

1. Builds a structured prompt with the birth chart summary (planet positions, houses, nakshatras, signs, functional natures)
2. Includes domain-specific instructions (e.g., "focus on 10th house for career")
3. Sends to LLM and returns the response

When no API key is configured, each agent falls back to a **chart-aware template** that uses actual computed planetary data rather than hardcoded text.

### AiConfig (`backend/src/main/java/com/astra/config/AiConfig.java`)

```java
@Bean @Primary
public ChatLanguageModel chatLanguageModel() {
    // OpenAI GPT-4o if key configured, otherwise placeholder
}

@Bean
public EmbeddingModel embeddingModel() {
    return new AllMiniLmL6V2EmbeddingModel();
}
```

## Vedic Knowledge Systems

The Chroma vector DB is seeded with texts from four classical systems:
- **Parashara** — Core house ownership, aspects, yogas, Shadbala, Ashtakavarga
- **KP (Krishnamurti Paddhati)** — Sub-lord analysis, 4-step theory
- **Jaimini** — Chara Karakas, Arudha Padas, special yogas
- **Nadi** — Structural yogas based on nakshatra padas

Also includes: Vimshottari Dasha, Divisional Charts (D1-D60), Muhurtha, Yoga formations, and planetary remedies.

## Data Flow

```
[Mobile App] → REST API → [AstrologyController]
                                ↓
                     [ChatLanguageModel] ← intent classification
                                ↓
                      [SwissEphemerisService]  — planetary positions
                                ↓
                      [AgentRouter.routeQuery()]
                                ↓
              ┌───────────────┬──┴──┬───────────────┐
              ▼               ▼     ▼               ▼
        [Chart Gen]   [Career]  ...  [Memory Agent]
              │               │     │               │
              └───────────────┴──┬──┴───────────────┘
                                ↓
                      [ChatLanguageModel] — response generation
                                ↓
                      [KnowledgeBaseService] — Vedic texts (Chroma)
                                ↓
                      [Response → Mobile App]
```

## Key Source Files

| File | Description |
|------|-------------|
| `backend/src/main/java/com/astra/ai/Agent.java` | Agent interface |
| `backend/src/main/java/com/astra/ai/AgentRouter.java` | Central query router with LLM classification |
| `backend/src/main/java/com/astra/ai/*Agent.java` | 10 agent implementations |
| `backend/src/main/java/com/astra/config/AiConfig.java` | LangChain4j beans |
| `backend/src/main/java/com/astra/ai/vector/ChromaService.java` | Vector DB with real embeddings |
| `backend/src/main/java/com/astra/ai/vector/KnowledgeBaseService.java` | Vedic knowledge + remedies |
| `backend/src/main/java/com/astra/config/KnowledgeBaseInitializer.java` | Seeds knowledge base on startup |
