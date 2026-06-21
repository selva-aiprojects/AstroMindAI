# Product Requirement Document (PRD)
# Project ASTRA: AI Life Intelligence Platform

## 1. Executive Summary

Project ASTRA is an advanced AI-powered life intelligence and astrological guidance platform. It blends traditional Vedic astrology systems with modern Large Language Models (LLMs). The platform delivers hyper-personalized, real-time life insights across career, finance, health, and relationships. It eliminates generic daily horoscopes by calculating precise planetary positions and utilizing an orchestration layer of 10 specialized AI agents.

### 1.1 Vision Statement
To democratize personalized Vedic astrology wisdom through AI, making ancient knowledge accessible, accurate, and actionable for modern life decisions.

### 1.2 Target Audience
- **Primary**: Individuals aged 25-45 seeking personalized life guidance
- **Secondary**: Astrology enthusiasts transitioning from generic horoscopes to precise calculations
- **Tertiary**: Spiritual seekers interested in Vedic wisdom and remedies

---

## 2. System Architecture & Tech Stack

### 2.1 Visual Architecture Flow

```
[Flutter Mobile App] <---> [Spring Boot Gateway / APIs] <---> [JWT / OAuth Auth]
                                    │
                        [Astrology Engine: Swiss Ephemeris]
                                    │
                [Knowledge Base: Parashara, KP, Jaimini, Nadi]
                                    │
                        [LangGraph AI Agent Layer]
                                    │
                    [LLMs: OpenAI / Claude / Gemini]
                                    │
          [PostgreSQL Relational DB] ─── [Chroma Vector DB]
```

### 2.2 Technology Stack

| Component | Technology | Purpose |
|-----------|-----------|---------|
| **Mobile Frontend** | Flutter (iOS and Android) | Cross-platform mobile application |
| **Backend Application** | Spring Boot (Java) | RESTful APIs, business logic |
| **Relational Database** | PostgreSQL | User profiles, transactions, system logs |
| **Vector Database** | Chroma | Semantic search, Vedic texts, long-term memory chunks |
| **AI Orchestration** | LangGraph | Multi-agent routing and state management |
| **Foundation LLMs** | OpenAI GPT-4o, Anthropic Claude 3.5 Sonnet, Google Gemini 1.5 Pro | Natural language understanding and generation |
| **Astrology Engine** | Swiss Ephemeris | Precise planetary position calculations |
| **Authentication** | JWT, Google OAuth 2.0, Firebase Phone Auth | Secure user authentication |
| **DevOps & Infrastructure** | Docker, Kubernetes, GitHub Actions (CI/CD) | Containerization, orchestration, deployment |

### 2.3 Infrastructure Architecture

#### 2.3.1 Cloud Infrastructure
- **Primary Cloud**: AWS (us-east-1)
- **Kubernetes Cluster**: EKS with auto-scaling (2-10 nodes)
- **Load Balancer**: AWS ALB with SSL termination
- **CDN**: CloudFront for static assets and API caching
- **Monitoring**: Prometheus + Grafana for metrics, CloudWatch for logs

#### 2.3.2 Database Architecture
- **PostgreSQL**: Amazon RDS Multi-AZ deployment
- **Chroma DB**: Self-hosted on Kubernetes with persistent volumes
- **Redis**: ElastiCache for session management and caching
- **S3**: For chart image storage and user uploads

#### 2.3.3 Security Architecture
- **WAF**: AWS WAF for DDoS protection and SQL injection prevention
- **VPC**: Private subnets for databases, public subnets for APIs
- **Secrets Manager**: AWS Secrets Manager for API keys and credentials
- **Encryption**: AES-256 at rest, TLS 1.3 in transit

---

## 3. Mobile App Features (Flutter)

### 3.1 Authentication & Onboarding

#### 3.1.1 Google Sign-In
- One-tap OAuth 2.0 integration
- Automatic profile creation from Google account data
- Optional: Link existing account to Google ID

#### 3.1.2 Phone Authentication
- OTP verification via SMS using Firebase Auth
- Support for international phone numbers
- Resend OTP functionality with rate limiting

#### 3.1.3 Birth Profile Creation
Mandatory user onboarding wizard collecting:
- **Personal Details**: Full Name, Gender
- **Birth Date**: Exact Date of Birth (DD/MM/YYYY)
- **Birth Time**: Exact Time of Birth (HH:MM:SS) with AM/PM toggle
- **Birth Place**: Google Places API integration for precise Latitude, Longitude, and Timezone detection
- **Validation**: Timezone auto-detection with manual override option

### 3.2 Core Astrology Visualizations

#### 3.2.1 Accurate Birth Chart
- Dynamic rendering of North Indian and South Indian style charts
- Support for multiple chart types:
  - Lagna (D1) - Birth Chart
  - Navamsha (D9) - Marriage Chart
  - Moon Chart - Moon-based chart
  - Dashamsha (D10) - Career Chart
- Interactive chart elements with tap-to-view details
- Color-coded planets based on functional nature (benefic/malefic)

#### 3.2.2 Dasha Timeline
- Interactive, nested tree-view displaying Vimshottari Dasha levels:
  - Mahadasha (Major period)
  - Antardasha (Sub-period)
  - Pratyantardasha (Sub-sub-period)
- Visual timeline with current period highlighting
- Swipe navigation through time periods
- Dasha change notifications and predictions

#### 3.2.3 Transit Analysis
- Overlay of current planetary positions (Gochara) against natal chart
- Real-time transit updates (daily)
- Major transit alerts:
  - Saturn Sade Sati periods
  - Jupiter transits
  - Rahu/Ketu transits
- Transit aspect visualization with aspect lines

### 3.3 Engagement & Personalization

#### 3.3.1 Daily Horoscope
- AI-generated daily summary based on real-time transit positions
- Personalized to user's Moon sign and Lagna
- Morning push notifications (configurable time)
- Key themes: Career, Relationships, Health, Finance

#### 3.3.2 AI Chat Interface
- Streaming text responses for real-time interaction
- Quick-reply prompts for common queries
- Speech-to-text input support
- Chat history with session management
- Context-aware follow-up suggestions

#### 3.3.3 Favourites & Bookmarks
- Save specific AI responses
- Bookmark chat sessions
- Save transit alerts and predictions
- Organize with custom tags/labels

#### 3.3.4 Profile Management
- Edit birth details with version history
- Manage premium subscriptions
- Configure push notification preferences
- Account settings and privacy controls
- Data export and deletion options

---

## 4. AI Agent Layer (LangGraph Orchestration)

### 4.1 Architecture Overview

Every query passes through a central router in LangGraph to invoke specialized agents:

```
                    [User Chat Query]
                            │
                  [LangGraph Central Router]
                            │
         ┌──────────────────┼──────────────────┐
         ▼                  ▼                  ▼
 [Agent 1: Chart Gen]  [Agent 2: Planet]  [Agent 3: Dasha] 
         │                  │                  │
    [Agent 4: Transit] [Agent 5: Career] [Agent 6: Marriage]
         │                  │                  │
    [Agent 7: Finance]  [Agent 8: Health] [Agent 9: Spiritual]
         │                  │                  │
         └──────────────────┼──────────────────┘
                            │
                            ▼
               [Agent 10: Conversation Memory]
                            │
                            ▼
                    [Final Streaming Response]
```

### 4.2 Agent Specifications

#### Agent 1: Birth Chart Generator
- **Input**: Raw birth coordinates, date, time
- **Output**: JSON payload containing:
  - Planetary longitudes (accurate to seconds)
  - House placements (Bhava Madhyas)
  - Functional benefics/malefics
  - Ascendant (Lagna) details
- **Engine**: Swiss Ephemeris with ayanamsa correction
- **Ayanamsa Options**: Lahiri, Raman, KP, Fagan-Bradley

#### Agent 2: Planet Analysis
- **Function**: Interprets planetary strengths and conditions
- **Analysis Parameters**:
  - Shadbala (six-fold strength)
  - Retrograde status
  - Combustion degrees
  - Planetary conjunctions
  - Planetary aspects (Drishti)
- **Output**: Strength scores and interpretation text

#### Agent 3: Dasha Analysis
- **Function**: Maps active Dasha lords to life themes
- **Analysis**:
  - Current Mahadasha, Antardasha, Pratyantardasha
  - Dasha lord's house ownership
  - Dasha lord's house placement
  - Dasha lord's strength and relationships
- **Output**: Period-specific predictions and themes

#### Agent 4: Transit Analysis
- **Function**: Evaluates current planetary transits
- **Key Transits**:
  - Saturn's Sade Sati (2.5-year periods)
  - Jupiter's yearly transits
  - Rahu/Ketu 18-month transits
  - Mars transits (for timing)
- **Output**: Transit impact assessment and timing predictions

#### Agent 5: Career Guidance
- **Function**: Career and professional life analysis
- **Parameters**:
  - 10th house (Karma Sthana) analysis
  - Amatyakarka (career indicator)
  - D10 (Dashamsha) chart analysis
  - Current Dasha influence on career
- **Output**: Career path suggestions, timing for job changes, promotion prospects

#### Agent 6: Marriage & Relationship Guidance
- **Function**: Relationship compatibility and timing
- **Parameters**:
  - 7th house (Kalatra Sthana) analysis
  - Venus and Jupiter positions
  - D9 (Navamsha) chart analysis
  - Current Dasha influence on relationships
- **Output**: Relationship timing, compatibility insights, remedies

#### Agent 7: Finance Guidance
- **Function**: Financial prosperity analysis
- **Parameters**:
  - 2nd house (Dhana Sthana) - wealth
  - 11th house (Labha Sthana) - gains
  - 9th house (Bhagya Sthana) - fortune
  - Dhana yogas (wealth combinations)
- **Output**: Investment timing, income prospects, financial planning
- **Disclaimer**: "Not financial advice. Consult certified financial advisors."

#### Agent 8: Health Guidance
- **Function**: Health and wellness analysis
- **Parameters**:
  - 6th house (Roga Sthana) - disease
  - 8th house (Ayur Sthana) - longevity
  - Lagna (Ascendant) strength
  - Active Dashas affecting health
- **Output**: Preventative wellness windows, vulnerable periods
- **Disclaimer**: "Not medical advice. Consult licensed healthcare professionals."

#### Agent 9: Spiritual Guidance
- **Function**: Spiritual growth and remedies
- **Parameters**:
  - 5th house (Punya Sthana) - merit
  - 9th house (Dharma Sthana) - spirituality
  - 12th house (Moksha Sthana) - liberation
  - Atmakaraka (soul indicator)
  - D20 chart (Vimshamsha) - spiritual life
- **Output**: Meditation techniques, mantra suggestions, remedial measures

#### Agent 10: Conversation Memory Agent
- **Function**: Context management and long-term memory
- **Operations**:
  - Condenses immediate chat history
  - Retrieves relevant past conversations from Chroma DB
  - Injects long-term user context into active agent prompts
  - Maintains user preference profiles
- **Storage**: Chroma Vector DB with semantic embeddings

---

## 5. Admin Portal Requirements

### 5.1 User & Subscription Management

#### 5.1.1 User Management
- View user profiles with birth chart data
- Access system logs and activity history
- Account suspension/ban functionality
- Account deletion with data cleanup
- User search and filtering capabilities
- Export user data (GDPR compliance)

#### 5.1.2 Subscription Management
- Configure subscription tiers:
  - **Free**: Limited daily queries, basic chart access
  - **Premium**: Unlimited queries, all charts, priority support
  - **VIP**: Personalized consultations, exclusive features
- Promotional pricing and discount codes
- Track payment webhooks:
  - Stripe (web payments)
  - App Store (iOS)
  - Play Store (Android)
- Subscription analytics and churn metrics

### 5.2 Content & System Control

#### 5.2.1 Prompt Management
- Web interface for live-editing system prompts
- Version control for all 10 AI agent prompts
- A/B testing framework for prompt variations
- Prompt performance analytics
- Rollback capabilities for prompt changes

#### 5.2.2 Content Management System (CMS)
- Upload and manage static articles
- Planetary remedy catalog management
- Automated push notification configuration
- Content scheduling and publishing
- Multi-language support framework

#### 5.2.3 Feedback & AI Analytics Dashboard
- User ratings aggregation (thumbs-up/down)
- AI response quality metrics
- API latency monitoring
- LLM token usage tracking
- Engine calculation exception logging
- Cost per query analysis

---

## 6. Functional & Data Requirements

### 6.1 Data Architecture

#### 6.1.1 PostgreSQL Schema (Relational Data)

**Users Table**
```sql
- user_id (UUID, PK)
- email (VARCHAR, unique)
- phone (VARCHAR, unique)
- auth_provider (ENUM: google, phone, email)
- subscription_tier (ENUM: free, premium, vip)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
- is_active (BOOLEAN)
```

**BirthProfiles Table**
```sql
- profile_id (UUID, PK)
- user_id (UUID, FK)
- full_name (VARCHAR)
- gender (ENUM: male, female, other)
- birth_date (DATE)
- birth_time (TIME)
- birth_latitude (DECIMAL)
- birth_longitude (DECIMAL)
- timezone (VARCHAR)
- ayanamsa (VARCHAR)
- created_at (TIMESTAMP)
```

**Transactions Table**
```sql
- transaction_id (UUID, PK)
- user_id (UUID, FK)
- amount (DECIMAL)
- currency (VARCHAR)
- payment_method (ENUM: stripe, apple, google)
- status (ENUM: pending, completed, failed)
- created_at (TIMESTAMP)
```

**ChatSessions Table**
```sql
- session_id (UUID, PK)
- user_id (UUID, FK)
- agent_type (VARCHAR)
- query_text (TEXT)
- response_text (TEXT)
- tokens_used (INTEGER)
- latency_ms (INTEGER)
- user_rating (INTEGER)
- created_at (TIMESTAMP)
```

#### 6.1.2 Chroma DB Schema (Vector Data)

**Collections:**
- **VedicTexts**: Parashara, Jaimini translations, classical texts
- **Remedies**: Mantras, gemstones, rituals, fasting guidelines
- **UserLongTermMemory**: Semantic summaries of past user interactions
- **KnowledgeBase**: FAQ, common queries, response templates

### 6.2 Knowledge Base Rule Mapping

#### 6.2.1 Parashara System
- Core logic for house ownership
- Planetary aspects (Drishti) calculations
- Vimshottari Dasha period calculations
- Yogas (planetary combinations) identification
- Planetary friendships and enmities

#### 6.2.2 KP System (Krishnamurti Paddhati)
- Sub-lord tables for precise timing
- Significators (Karakas) for each house
- Sub-lord analysis for event prediction
- Used by Agent 5 (Career) and Agent 7 (Finance)

#### 6.2.3 Jaimini System
- Chara Karakas:
  - Atmakaraka (soul indicator)
  - Amatyakarka (career indicator)
  - Bhratrukaraka (siblings)
  - Matrukaraka (mother)
  - Pitrikaraka (father)
  - Putrakaraka (children)
  - Gnathikaraka (relatives)
  - Darakaraka (spouse)
- Rashi aspects and Argala
- Calculated by Agent 1, used by Agent 9

#### 6.2.4 Nadi Astrology
- Structural planetary combinations (Yogas)
- Macro lifecycle predictions
- Special combinations for specific life events
- Timing techniques based on Nadiamsa

---

## 7. API Specifications

### 7.1 Authentication Endpoints

```
POST /api/v1/auth/google
POST /api/v1/auth/phone/send-otp
POST /api/v1/auth/phone/verify-otp
POST /api/v1/auth/refresh
POST /api/v1/auth/logout
```

### 7.2 User Management Endpoints

```
GET    /api/v1/users/profile
PUT    /api/v1/users/profile
POST   /api/v1/users/birth-profile
GET    /api/v1/users/birth-profile
PUT    /api/v1/users/birth-profile
DELETE /api/v1/users/account
```

### 7.3 Astrology Endpoints

```
GET /api/v1/astrology/birth-chart
GET /api/v1/astrology/dasha-timeline
GET /api/v1/astrology/transits
GET /api/v1/astrology/daily-horoscope
```

### 7.4 AI Chat Endpoints

```
POST /api/v1/chat/query (streaming)
GET  /api/v1/chat/sessions
GET  /api/v1/chat/sessions/{id}
POST /api/v1/chat/sessions/{id}/feedback
```

### 7.5 Subscription Endpoints

```
GET  /api/v1/subscriptions/plans
POST /api/v1/subscriptions/create
GET  /api/v1/subscriptions/current
POST /api/v1/subscriptions/cancel
```

---

## 8. Product Roadmap & Future Scope

### 8.1 Phase 1: Core App (Q1-Q2)

**MVP Features:**
- iOS/Android Launch
- 10 AI Agents Active
- Core Swiss Ephemeris Integration
- Basic birth chart visualization
- Daily horoscope generation
- AI chat interface

**Success Criteria:**
- 10,000 downloads in first month
- 35% D7 retention
- 4% conversion to paid tier

### 8.2 Phase 2: Partnerships (Q3-Q4)

**New Features:**
- WhatsApp Integration
- Voice AI Astrologer (real-time audio)
- Synastry Matching (compatibility analysis)
- Family Horoscope (multiple profiles)
- Muhurta Finder (auspicious timing)

**Technical Enhancements:**
- WebRTC for voice interactions
- WhatsApp Business API integration
- Advanced matching algorithms

### 8.3 Phase 3: Ecosystem (Q1 Next Year)

**New Features:**
- Wearables (Apple Watch, Wear OS)
- Palm Reading AI (computer vision)
- Face Reading AI (physiognomy)
- Annual Varshaphala Maps
- Ecosystem integrations

**Technical Enhancements:**
- On-device ML models
- Wear OS complications
- Apple Watch widgets
- Offline mode support

### 8.4 Future Vision

**Voice AI Astrologer:**
- Real-time conversational audio processing
- WebSockets and Twilio/Vapi integration
- Natural voice interactions with AI agents

**Family Horoscope:**
- Dashboard for multiple dependent profiles
- Master account subscription management
- Family compatibility analysis

**Marriage Matching & Muhurta Finder:**
- Automated Synastry matching (Ashta-koota system)
- Auspicious time window calculators
- Wedding date recommendations

**Computer Vision Modules:**
- AI engines for palm line analysis (Palmistry)
- Facial structure analysis (Physiognomy)
- Photo-based predictions

**Ecosystem Expansion:**
- Native WhatsApp chatbot interfaces
- Real-time contextual complications for wearables
- Third-party API access for developers

---

## 9. Non-Functional Requirements & KPIs

### 9.1 Performance & Reliability

#### 9.1.1 Latency Requirements
- **Astrology Engine**: < 200ms for chart calculations
- **LLM First Token**: < 1.5 seconds for streaming response
- **API Endpoints**: < 100ms for non-LLM endpoints
- **Database Queries**: < 50ms for indexed queries

#### 9.1.2 Uptime Requirements
- **Core API Endpoints**: 99.9% availability
- **Kubernetes Autoscaling**: 2-10 nodes based on load
- **Database**: Multi-AZ deployment with automatic failover
- **CDN**: 99.99% availability for static assets

### 9.2 Security, Compliance & Data Privacy

#### 9.2.1 Data Protection
- End-to-end encryption for chat logs
- AES-256 encryption at rest
- TLS 1.3 encryption in transit
- GDPR and CCPA compliance
- Right-to-be-forgotten deletion workflows

#### 9.2.2 Medical & Financial Disclaimers
- **Agent 7 (Finance)**: "Not financial advice. Consult certified financial advisors before making investment decisions."
- **Agent 8 (Health)**: "Not medical advice. Consult licensed healthcare professionals for health concerns."
- **General Disclaimer**: "Astrological insights are for entertainment and guidance purposes only."

#### 9.2.3 Security Measures
- Regular security audits (quarterly)
- Penetration testing (bi-annual)
- Dependency vulnerability scanning
- Secrets rotation (monthly)
- Rate limiting and DDoS protection

### 9.3 Scalability Requirements

#### 9.3.1 User Capacity
- **Phase 1**: Support 100,000 concurrent users
- **Phase 2**: Support 500,000 concurrent users
- **Phase 3**: Support 1,000,000 concurrent users

#### 9.3.2 Database Scaling
- **PostgreSQL**: Read replicas for query scaling
- **Chroma DB**: Horizontal scaling with sharding
- **Redis**: Cluster mode for high availability

### 9.4 Monitoring & Observability

#### 9.4.1 Metrics to Track
- API response times (p50, p95, p99)
- Error rates by endpoint
- LLM token usage and costs
- Database query performance
- Kubernetes resource utilization
- User engagement metrics

#### 9.4.2 Alerting
- Critical alerts: Service downtime, database failures
- Warning alerts: High latency, elevated error rates
- Info alerts: Scheduled maintenance, deployments

---

## 10. Verification & Success Metrics

### 10.1 User Retention
- **D7 Retention**: > 35% target
- **D30 Retention**: > 15% target
- **D90 Retention**: > 8% target
- **Churn Rate**: < 5% monthly

### 10.2 AI Accuracy
- **Fallback Rate**: < 2% to generic responses
- **User Satisfaction**: > 4.0/5.0 average rating
- **Response Relevance**: > 85% positive feedback
- **Calculation Accuracy**: > 99.9% for Swiss Ephemeris

### 10.3 Monetization Conversion
- **Free to Paid**: > 4% conversion rate
- **Trial to Subscription**: > 15% conversion
- **Average Revenue Per User (ARPU)**: $5-10/month
- **Lifetime Value (LTV)**: > $60

### 10.4 Technical Metrics
- **API Success Rate**: > 99.9%
- **App Crash Rate**: < 0.1%
- **Load Time**: < 3 seconds for initial load
- **Battery Impact**: < 5% per hour of usage

---

## 11. Risk Assessment & Mitigation

### 11.1 Technical Risks

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| LLM API downtime | High | Medium | Multiple LLM providers, fallback mechanisms |
| Swiss Ephemeris calculation errors | High | Low | Extensive testing, validation against known charts |
| Database performance degradation | Medium | Medium | Read replicas, query optimization, caching |
| Scalability bottlenecks | High | Medium | Load testing, auto-scaling, architecture review |

### 11.2 Business Risks

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Low user adoption | High | Medium | Marketing campaigns, influencer partnerships |
| High churn rate | High | Medium | Engagement features, personalized content |
| Regulatory challenges | Medium | Low | Legal consultation, compliance frameworks |
| Competition | High | High | Unique features, superior accuracy, brand building |

### 11.3 Legal & Compliance Risks

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| GDPR/CCPA violations | High | Low | Data protection by design, regular audits |
| Medical advice liability | High | Low | Clear disclaimers, user agreements |
| Financial advice liability | High | Low | Clear disclaimers, user agreements |
| Data breaches | High | Low | Encryption, security audits, incident response plan |

---

## 12. Cost Estimation & Budget

### 12.1 Infrastructure Costs (Monthly)

| Component | Cost (USD) | Notes |
|-----------|------------|-------|
| AWS EKS (Kubernetes) | $500-1,000 | Auto-scaling 2-10 nodes |
| AWS RDS (PostgreSQL) | $200-400 | Multi-AZ deployment |
| AWS ElastiCache (Redis) | $100-200 | Cluster mode |
| AWS S3 + CloudFront | $50-100 | Storage and CDN |
| LLM API Costs | $1,000-5,000 | Based on usage |
| Monitoring & Logging | $100-200 | CloudWatch, Prometheus |
| **Total** | **$1,950-6,900** | Scales with usage |

### 12.2 Development Costs

| Phase | Duration | Cost (USD) |
|-------|----------|------------|
| Phase 1 (MVP) | 3-4 months | $150,000-200,000 |
| Phase 2 (Partnerships) | 3-4 months | $100,000-150,000 |
| Phase 3 (Ecosystem) | 4-6 months | $200,000-300,000 |
| **Total** | **10-14 months** | **$450,000-650,000** |

### 12.3 Ongoing Costs

| Category | Monthly Cost (USD) |
|----------|-------------------|
| Infrastructure | $2,000-7,000 |
| LLM APIs | $1,000-5,000 |
| Support & Maintenance | $5,000-10,000 |
| Marketing & Growth | $10,000-20,000 |
| **Total** | **$18,000-42,000** |

---

## 13. Competitive Analysis

### 13.1 Key Competitors

| Competitor | Strengths | Weaknesses | Our Advantage |
|------------|-----------|------------|---------------|
| Co-Star | Modern UI, AI-powered | Generic horoscopes, limited Vedic | Precise calculations, Vedic expertise |
| Astrology.com | Large user base | Outdated UI, generic content | Modern UX, personalized AI |
| Vedic Astrology Apps | Authentic Vedic | Poor UX, limited AI | Best of both worlds |
| ChatGPT Astrology | Powerful AI | No precise calculations | Swiss Ephemeris accuracy |

### 13.2 Differentiation Strategy

1. **Precision**: Swiss Ephemeris calculations vs. generic approximations
2. **Personalization**: Birth-chart-based vs. sun-sign-based
3. **Vedic Expertise**: Traditional systems (Parashara, KP, Jaimini) vs. Western-only
4. **AI Orchestration**: Specialized agents vs. single generic model
5. **Real-time**: Live transit calculations vs. static predictions

---

## 14. User Personas

### 14.1 Primary Persona: "Seeker Sarah"
- **Age**: 28-35
- **Occupation**: Professional/Entrepreneur
- **Goals**: Career guidance, relationship timing, life decisions
- **Pain Points**: Generic horoscopes, lack of personalization
- **Preferred Features**: AI chat, career guidance, transit alerts

### 14.2 Secondary Persona: "Spiritual Sam"
- **Age**: 35-45
- **Occupation**: Established professional
- **Goals**: Spiritual growth, remedies, deeper understanding
- **Pain Points**: Superficial content, lack of traditional wisdom
- **Preferred Features**: Spiritual guidance, remedies, Vedic texts

### 14.3 Tertiary Persona: "Curious Chris"
- **Age**: 22-28
- **Occupation**: Student/Early Career
- **Goals**: General guidance, daily insights
- **Pain Points**: Information overload, skepticism
- **Preferred Features**: Daily horoscope, free tier, simple UI

---

## 15. Testing Strategy

### 15.1 Unit Testing
- **Coverage Target**: > 80% for backend logic
- **Framework**: JUnit (Java), pytest (Python)
- **Focus**: Astrology calculations, business logic

### 15.2 Integration Testing
- **API Testing**: Postman, Newman for CI/CD
- **Database Testing**: Testcontainers for PostgreSQL
- **LLM Testing**: Mock responses for agent testing

### 15.3 End-to-End Testing
- **Mobile E2E**: Detox for Flutter
- **User Flows**: Authentication, chat, chart viewing
- **Performance**: Load testing with k6

### 15.4 Manual Testing
- **Beta Testing**: 100 users in Phase 1
- **UAT**: User acceptance testing before launch
- **Accessibility**: WCAG 2.1 compliance testing

---

## 16. Deployment Strategy

### 16.1 CI/CD Pipeline

```
[Code Push] → [GitHub Actions] → [Build & Test] → [Docker Build] → 
[Security Scan] → [Deploy to Staging] → [E2E Tests] → [Deploy to Production]
```

### 16.2 Release Strategy

**Phase 1 (MVP):**
- Soft launch to 1,000 users
- Gradual rollout to 10,000 users
- Monitor metrics and fix issues

**Phase 2 (Partnerships):**
- Feature flags for new features
- A/B testing for UI changes
- Gradual feature rollout

**Phase 3 (Ecosystem):**
- Beta program for wearables
- Limited release for CV features
- Full rollout after validation

### 16.3 Rollback Plan

- **Database**: Point-in-time recovery (PITR) enabled
- **Application**: Blue-green deployment for instant rollback
- **Configuration**: Version-controlled with quick revert capability
- **Monitoring**: Automated rollback on critical error rates

---

## 17. Support & Maintenance

### 17.1 Support Tiers

| Tier | Response Time | Scope |
|------|---------------|-------|
| Free | 48 hours | Email support only |
| Premium | 24 hours | Email + chat support |
| VIP | 4 hours | Priority chat + phone support |

### 17.2 Maintenance Schedule

- **Weekly**: Dependency updates, security patches
- **Monthly**: Performance optimization, feature updates
- **Quarterly**: Major releases, architecture reviews
- **Annually**: Strategic planning, tech stack evaluation

---

## 18. Appendices

### 18.1 Glossary

- **Ayanamsa**: The difference between tropical and sidereal zodiacs
- **Antardasha**: Sub-period within a Mahadasha
- **Ascendant (Lagna)**: The zodiac sign rising on the eastern horizon at birth
- **Bhava**: House in Vedic astrology
- **Dasha**: Planetary period system
- **Drishti**: Planetary aspect
- **Karakas**: Significators in Jaimini astrology
- **Mahadasha**: Major planetary period
- **Navamsha (D9)**: 9th divisional chart for marriage
- **Pratyantardasha**: Sub-sub-period within an Antardasha
- **Vimshottari Dasha**: 120-year planetary period system

### 18.2 References

- **Brihat Parashara Hora Shastra**: Ancient Vedic astrology text
- **KP Astrology Reader**: Krishnamurti Paddhati fundamentals
- **Jaimini Sutras**: Jaimini astrology principles
- **Swiss Ephemeris Documentation**: Planetary calculation library
- **LangGraph Documentation**: AI agent orchestration framework

### 18.3 Contact Information

- **Product Owner**: [To be assigned]
- **Tech Lead**: [To be assigned]
- **Project Manager**: [To be assigned]
- **Support Email**: support@astra.ai
- **Documentation**: docs.astra.ai

---

**Document Version**: 1.0  
**Last Updated**: June 20, 2026  
**Status**: Draft  
**Next Review**: July 20, 2026
Le