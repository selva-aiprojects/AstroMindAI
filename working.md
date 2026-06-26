# Working Status — AstroMindAI (Project ASTRA)

## Current Phase: Wired MVP

All core components have been wired for a fully working demo. The app can run in development mode with zero external dependencies.

## What's Wired

### Backend (Spring Boot 3.2 / Java 17)
- [x] **Render Hosting**: Configured for Render deployment. Health check at `/api/v1/health` is pinged by UptimeRobot every 10-14 minutes to prevent idle sleep.
- [x] **Yearly AI Projection**: `/api/v1/astrology/yearly-projection` endpoint wired to LLM for comprehensive 12-month future forecasting.
- [x] **Swiss Ephemeris** (`SwissEphemerisService.java`): Real `SwissEph` library calls (`swe_calc_ut`, `swe_houses`) with simplified fallback when native lib unavailable
- [x] **AI Agents** (10 agents): All wired to LangChain4j `ChatLanguageModel` — sends birth chart context as structured prompt to LLM (OpenAI GPT-4o / Claude / Gemini). Falls back to smart chart-aware template responses when no API key configured
- [x] **AgentRouter** (`AgentRouter.java`): LLM-based intent classification (falls back to keyword matching). All agents injected with `ChatLanguageModel`
- [x] **Chroma Vector DB** (`ChromaService.java`): Real embeddings via `AllMiniLmL6V2EmbeddingModel`. Full REST API client for Chroma HTTP API
- [x] **Knowledge Base** (`KnowledgeBaseService.java`): Seeded with 12 Vedic texts (Parashara, Jaimini, KP, Nadi, etc.) and 10 planetary remedies with metadata
- [x] **Dev Profile** (`application-dev.properties`): H2 in-memory DB, zero external deps needed
- [x] **Demo Auth** (`POST /api/v1/auth/demo`): Auto-creates/returns premium user
- [x] **CORS**: Fixed `allowedOriginPatterns` for mobile-backend communication
- [x] **Build**: Fixed `AstraApplication` class name, Lombok `@Builder` on all DTOs

### Mobile (Flutter / Dart)
- [x] **UI/UX Pro Max Refactor**: Fully redesigned using a "Liquid Glass" premium aesthetic (Cinematic Void, Luxury Gold `#D4AF37`, Royal Amethyst `#7E57C2`) with `BackdropFilter` glassmorphism.
- [x] **Insights Toggle**: Added segmented control in `InsightsTab` to switch between 'Current Transits' and '1-Year AI Projection'.
- [x] **Split APK Builds**: Implemented `--split-per-abi` build output for lightweight Android artifacts (`armeabi-v7a`, `arm64-v8a`, `x86_64`).
- [x] **Demo Mode**: "Demo Mode (Dev)" button on auth screen — calls `/api/v1/auth/demo`, gets JWT, saves session
- [x] **Chat Fallback**: When backend unavailable, uses local `_generateAIResponse()` with chart-aware hardcoded replies
- [x] **API Client**: Full `demoLogin()` method, all endpoints wired including Render production URLs.
- [x] **Auth Flow**: Google OAuth, Phone OTP, and Demo mode all functional
- [x] **Language Persistence**: Language selection now persists to `SharedPreferences` and survives app restarts. Fixed bottom sheet context to resolve provider correctly.
- [x] **Location Settings Guidance**: When location services are disabled or permission permanently denied, styled dialogs guide the user to device/app settings via `Geolocator.openLocationSettings()` / `openAppSettings()`.
- [x] **Date/Time Picker Dark Theme**: Custom `DatePickerThemeData` and `TimePickerThemeData` with white text on deep purple backgrounds and gold accents for clear visibility.

### Infrastructure
- [x] `docker-compose.yml` — PostgreSQL 15, Chroma, Redis 7, Backend
- [x] All K8s manifests for production deployment

### CI/CD
- [x] Backend, Mobile, Security workflows

## Still Placeholder / Not Yet Wired
- [ ] **iOS CI build** still disabled (`if: false`)
- [ ] **K8s secrets** need real base64 values
- [ ] **Firebase project** references dev `education-apps-f2032`
- [ ] **Unit/integration tests** minimal
- [ ] **Mobile screens**: Dasha timeline, transit analysis, subscription pages not implemented
- [ ] **Swiss Ephemeris native lib** needs ephemeris data files for full accuracy
- [ ] **Chroma** on REST API only when Chroma server is running

## How to Run

### Backend (zero-dependency dev mode)
```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
# API available at http://localhost:8080/api/v1
# Health check: http://localhost:8080/api/v1/health
# H2 Console: http://localhost:8080/h2-console
```

### Backend (with Docker)
```bash
cd infrastructure
docker-compose up -d
# Starts Postgres, Chroma, Redis, Backend
```

### Mobile App
```bash
cd mobile/astra_app
flutter run
# Uses demo mode button for quick login
```

## Key Changes in This Wiring

| File | Change |
|------|--------|
| `pom.xml` | Added `langchain4j` core + `langchain4j-embeddings-all-minilm-l6-v2` |
| `SwissEphemerisService.java` | Real `SwissEph.swe_calc_ut()` calls + full 12-sign functional nature |
| `AgentRouter.java` | LLM routing + Lombok builder fix |
| `AiConfig.java` (new) | `ChatLanguageModel` bean + `EmbeddingModel` bean |
| All `*Agent.java` (10 files) | LangChain4j `ChatLanguageModel` injection + smart template fallback |
| `ChromaService.java` | Real `EmbeddingModel` for vector generation |
| `KnowledgeBaseService.java` | 12 Vedic texts + 10 remedies seeded |
| `AstrologyController.java` | `/yearly-projection` endpoint added |
| `AuthController.java` | `/api/v1/auth/demo` endpoint |
| `AuthenticationService.java` | `authenticateDemo()` method |
| `application-dev.properties` (new) | H2 profile for dev |
| `app_theme.dart` | Refactored to Premium Dark + Gold "Liquid Glass" palette |
| `dashboard_tab.dart` | Glassmorphism UI update using `BackdropFilter` |
| `insights_tab.dart` | Glassmorphism UI + Yearly Projection toggle |
| `auth_provider.dart` | `signInDemo()` method |
| `api_client.dart` | `demoLogin()`, `getYearlyProjection()`, and Render prod URL |
| `auth_screen.dart` | "Demo Mode (Dev)" button |
| `chat_screen.dart` | Local AI fallback when backend unreachable |
| `language_provider.dart` | SharedPreferences persistence for selected language; loads on startup |
| `language_picker.dart` | Passes parent context to bottom sheet for correct provider scope |
| `birth_profile_onboarding.dart` | Location settings guidance dialogs + themed date/time pickers |

## API Endpoints
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/health` | GET | Health check |
| `/api/v1/auth/demo` | POST | Demo login (returns JWT) |
| `/api/v1/auth/google` | POST | Google OAuth login |
| `/api/v1/auth/phone/send-otp` | POST | Send phone OTP |
| `/api/v1/auth/phone/verify-otp` | POST | Verify phone OTP |
| `/api/v1/users/profile` | GET/POST | User CRUD |
| `/api/v1/users/birth-profile` | GET/POST/PUT | Birth profile CRUD |
| `/api/v1/astrology/birth-chart` | GET | Get birth chart by userId |
| `/api/v1/astrology/chat` | POST | AI chat with routing |
| `/api/v1/astrology/dasha-timeline` | GET | Dasha periods |
| `/api/v1/astrology/transits` | GET | Current transits |
| `/api/v1/astrology/yearly-projection` | GET | 1-year AI-driven projection |
| `/api/v1/astrology/daily-horoscope` | GET | Daily horoscope |
| `/api/v1/subscriptions/plans` | GET | Subscription tiers |
