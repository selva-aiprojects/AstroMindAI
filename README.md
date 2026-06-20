# AstroMindAI - AI Life Intelligence Platform

AstroMindAI is an advanced AI-powered life intelligence and astrological guidance platform. It blends traditional Vedic astrology systems with modern Large Language Models (LLMs) to deliver hyper-personalized, real-time life insights.

## Project Structure

```
astromindai/
├── backend/              # Spring Boot backend application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/    # Java source code
│   │   │   └── resources/ # Configuration files
│   │   └── test/        # Test code
│   └── pom.xml         # Maven dependencies
├── mobile/             # Flutter mobile application
│   ├── lib/            # Dart source code
│   ├── android/        # Android-specific code
│   ├── ios/            # iOS-specific code
│   └── pubspec.yaml    # Flutter dependencies
├── infrastructure/     # Docker and Kubernetes configs
│   ├── docker/         # Docker files
│   └── k8s/            # Kubernetes manifests
├── docs/               # Documentation
│   └── PRD.md          # Product Requirements Document
└── README.md           # This file
```

## Technology Stack

- **Mobile Frontend**: Flutter (iOS and Android)
- **Backend**: Spring Boot (Java)
- **Database**: PostgreSQL (relational), Chroma (vector)
- **AI Orchestration**: LangGraph
- **LLMs**: OpenAI GPT-4o, Claude 3.5 Sonnet, Gemini 1.5 Pro
- **Astrology Engine**: Swiss Ephemeris
- **Authentication**: JWT, Google OAuth, Firebase Phone Auth
- **Infrastructure**: Docker, Kubernetes, GitHub Actions

## Getting Started

### Prerequisites

- Java 17+
- Node.js 18+
- Flutter 3.0+
- Docker 20+
- PostgreSQL 14+
- Python 3.9+ (for LangGraph agents)

### Backend Setup

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### Mobile App Setup

```bash
cd mobile
flutter pub get
flutter run
```

### Infrastructure Setup

```bash
cd infrastructure
docker-compose up -d
```

## API Documentation

API documentation will be available at `http://localhost:8080/swagger-ui.html` once the backend is running.

## Development Status

- [x] Project structure setup
- [ ] Spring Boot backend initialization
- [ ] Database schema configuration
- [ ] Authentication system
- [ ] Swiss Ephemeris integration
- [ ] LangGraph agent layer
- [ ] Flutter mobile app
- [ ] Docker configuration
- [ ] CI/CD pipeline

## Contributing

Please read the PRD in `docs/PRD.md` for detailed project requirements.

## License

Proprietary - All rights reserved
