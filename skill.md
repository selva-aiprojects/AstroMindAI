# Development Skills & Patterns — AstroMindAI

## Project Layout

```
Astra/
├── backend/              # Spring Boot 3.2 / Java 17 (Maven)
│   └── src/main/java/com/astra/
│       ├── ai/           # 10 AI agents + AgentRouter + vector
│       ├── astrology/    # SwissEphemeris + Dasha + Transit
│       ├── config/       # AiConfig, Security, Jpa, Chroma, CORS
│       ├── controller/   # REST endpoints
│       ├── model/        # JPA entities
│       ├── repository/   # JPA repositories
│       ├── security/     # JWT + Auth
│       └── service/      # Business logic
├── mobile/astra_app/     # Flutter (Dart)
├── infrastructure/       # Docker Compose + K8s
└── docs/                 # PRD, documentation
```

## Development Setup

### Quick Start (Zero Dependencies)
```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
# API: http://localhost:8080/api/v1
# H2 Console: http://localhost:8080/h2-console (JDBC: jdbc:h2:mem:astra_db)
```

### Full Stack (With Docker)
```bash
cd infrastructure
docker-compose up -d    # Postgres, Chroma, Redis, Backend
cd mobile/astra_app
flutter run
```

## Backend Patterns

### Adding a New Controller
1. Create class in `backend/src/main/java/com/astra/controller/`
2. Annotate with `@RestController` and `@RequestMapping("/api/v1/{resource}")`
3. Inject services via **constructor injection**
4. Follow existing patterns (see `AuthController`, `AstrologyController`)

### Adding a New Entity
1. Create JPA entity in `backend/src/main/java/com/astra/model/`
2. Annotate with `@Entity`, `@Table`, `@Id`, etc., use `@Data` from Lombok
3. Create repository in `backend/src/main/java/com/astra/repository/` extending `JpaRepository`
4. Add Flyway migration in `backend/src/main/resources/db/migration/` as `V{next}__{Description}.sql`
5. **For dev profile**: `spring.jpa.hibernate.ddl-auto=create-drop` auto-creates tables

### Adding a New AI Agent
1. Create class in `backend/src/main/java/com/astra/ai/` implementing `Agent`
2. Annotate with `@Component`, inject `ChatLanguageModel` via constructor
3. Implement `process(AgentRequest)` — build prompt with chart context, call LLM, fall back to template
4. Register in `AgentRouter.initializeAgents()` with a unique key
5. Build context in `AstrologyController.chat()` before routing

### Using LangChain4j in an Agent
```java
@Component
public class MyAgent implements Agent {
    private final ChatLanguageModel llm;

    public MyAgent(ChatLanguageModel llm) { this.llm = llm; }

    private String generateResponse(String query, BirthChart chart) {
        String summary = summarizeChart(chart);
        try {
            return llm.generate("Your prompt with " + summary + " and " + query);
        } catch (Exception e) {
            return templateResponse(chart);
        }
    }
}
```

### Adding LLM API Keys
Set env vars before running:
```bash
$env:OPENAI_API_KEY = "sk-..."
$env:ANTHROPIC_API_KEY = "sk-ant-..."
$env:GOOGLE_AI_API_KEY = "..."
```

Without keys, agents use chart-aware template responses (still functional).

### Configuration
- `application.properties` — Production config (PostgreSQL, all services)
- `application-dev.properties` — Dev profile (H2 in-memory, no external deps)
- Activate dev: `-Dspring-boot.run.profiles=dev`

## Mobile Patterns

### Adding a New Feature Screen
1. Create folder under `mobile/astra_app/lib/features/{feature_name}/`
2. Structure: `presentation/` for UI, `providers/` for state
3. Register route in `main.dart`'s `MaterialApp.routes`
4. Use `Provider` / `Riverpod` for state management

### API Client
- Singleton `ApiClient` in `mobile/astra_app/lib/core/network/api_client.dart`
- Uses Dio for HTTP, all endpoints defined as methods
- Default base URL: `http://10.0.2.2:8080/api/v1` (Android emulator) or `http://localhost:8080/api/v1` (web)
- Override at build: `--dart-define=BACKEND_BASE_URL=http://your-host:8080/api/v1`
- Auth token set automatically via `AuthProvider`

### Demo Mode
- "Demo Mode (Dev)" button on auth screen
- Calls `POST /api/v1/auth/demo` → gets JWT → auto-login
- Creates user `demo@astromindai.app` with PREMIUM tier

## Infrastructure Patterns

### Docker Compose (Full Stack)
```bash
cd infrastructure
docker-compose up -d
```
Services: Postgres 15, Chroma, Redis 7, Backend on `:8080`

### Kubernetes
Full manifests in `infrastructure/k8s/`:
- `backend-deployment.yaml` — 3 replicas, HPA (2-10 pods)
- `horizontal-pod-autoscaler.yaml` — CPU 70%, Mem 80%
- `ingress.yaml` — `api.astra.ai` with Let's Encrypt TLS

## Key Technical Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| AI Framework | LangChain4j | Native Java, Spring Boot integration |
| LLM Integration | Constructor injection per agent | Each agent gets its own model instance |
| Embeddings | `all-MiniLM-L6-v2` (ONNX) | Local, fast, no API key needed |
| Embedding Fallback | Random vectors (legacy) | For when ONNX runtime unavailable |
| Routing | LLM-first, keyword fallback | Accuracy with graceful degradation |
| Dev DB | H2 in-memory | Zero setup for development |
| Auth | JWT + Demo mode | Flexible, works without external providers |
| Chart Calc | Swiss Ephemeris + fallback | Accuracy when native lib available |

## Common Commands

```bash
# Backend (dev mode, no deps)
cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Backend (production)
cd backend && mvn clean install -DskipTests && mvn spring-boot:run

# Mobile
cd mobile/astra_app && flutter pub get
cd mobile/astra_app && flutter run
cd mobile/astra_app && flutter run --dart-define=BACKEND_BASE_URL=http://10.0.2.2:8080/api/v1
cd mobile/astra_app && flutter build apk --debug

# Infrastructure
cd infrastructure && docker-compose up -d
cd infrastructure && docker-compose logs -f backend

# Testing
cd backend && mvn test
cd mobile/astra_app && flutter test

# Analysis
cd mobile/astra_app && dart analyze lib/
```
