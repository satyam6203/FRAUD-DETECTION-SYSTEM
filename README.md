# 🔐 AI-Powered Fraud Detection System

An **AI-powered, event-driven payment fraud detection system** built using **Java, Spring Boot, Apache Kafka, PostgreSQL, Redis, FastAPI, and LangChain**.

The system processes payment transactions in real time, evaluates transaction risk using **rule-based and AI-driven detection**, and automatically determines whether a transaction should be **approved, reviewed, or blocked**.

The project follows a **scalable microservices architecture** designed for real-time financial transaction processing.

---

## 🚀 Features

- ⚡ Real-time payment transaction processing
- 🔐 Fraud detection using configurable business rules
- 🤖 AI-powered transaction risk analysis
- 🧠 LangChain-based AI reasoning service
- 📨 Event-driven communication using Apache Kafka
- 🗄️ PostgreSQL for persistent transaction data
- ⚡ Redis for fast data access and caching
- 🌐 API Gateway for centralized request routing
- 🧩 Independent microservices architecture
- 🐳 Docker-ready architecture
- 📊 Transaction risk classification
- 🛡️ Automatic transaction decision:
  - `APPROVED`
  - `REVIEW`
  - `BLOCKED`

---

## 🏗️ System Architecture

```text
                         ┌─────────────────────┐
                         │       Client        │
                         │  Web / REST Client  │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │     API Gateway     │
                         │    Spring Cloud     │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │   Payment Service   │
                         │    Spring Boot     │
                         └──────────┬──────────┘
                                    │
                                    │ Kafka Event
                                    ▼
                         ┌─────────────────────┐
                         │        Kafka        │
                         │ Event Streaming Bus │
                         └──────────┬──────────┘
                                    │
                                    ▼
                  ┌─────────────────────────────────┐
                  │     Fraud Detection Service     │
                  │                                 │
                  │  • Rule-Based Detection         │
                  │  • Risk Evaluation              │
                  │  • Transaction Classification   │
                  └──────────────┬──────────────────┘
                                 │
                                 │ AI Analysis
                                 ▼
                  ┌─────────────────────────────────┐
                  │      AI Reasoning Service       │
                  │                                 │
                  │       FastAPI + LangChain       │
                  │                                 │
                  │  • Risk Analysis                │
                  │  • AI Reasoning                 │
                  │  • Fraud Explanation            │
                  └─────────────────────────────────┘

                  ┌─────────────────┐
                  │   PostgreSQL    │
                  │ Transaction DB  │
                  └─────────────────┘

                  ┌─────────────────┐
                  │      Redis      │
                  │ Cache / Fast DB │
                  └─────────────────┘
```

---

## 🧩 Microservices

The project is organized inside the `MICROSERVICE` directory.

### 1. Payment Service

**Technology:** Java + Spring Boot

Responsible for:

- Receiving payment requests
- Validating transaction information
- Creating payment transactions
- Publishing transaction events to Kafka
- Communicating with downstream fraud detection services

---

### 2. Fraud Detection Service

**Technology:** Java + Spring Boot

Responsible for:

- Consuming payment events from Kafka
- Evaluating transaction risk
- Applying fraud detection rules
- Checking suspicious transaction patterns
- Requesting AI-based analysis when required
- Generating the final transaction decision

Possible decisions:

```text
APPROVED
REVIEW
BLOCKED
```

---

### 3. AI Reasoning Service

**Technology:** Python + FastAPI + LangChain

Responsible for AI-powered fraud analysis.

It can analyze transaction information and provide additional reasoning about potentially suspicious activity.

Main responsibilities:

- AI-based risk analysis
- Fraud reasoning
- Transaction context analysis
- Risk explanation
- Supporting the fraud detection service with intelligent analysis

---

### 4. API Gateway

**Technology:** Spring Boot / Spring Cloud

The API Gateway provides a single entry point for clients.

Responsibilities include:

- Request routing
- Service discovery/routing
- Centralized API access
- Hiding internal microservice endpoints
- Providing a clean interface for clients

---

## 🛠️ Tech Stack

| Category         | Technology                 |
| ---------------- | -------------------------- |
| Backend          | Java                       |
| Framework        | Spring Boot                |
| Microservices    | Spring Boot / Spring Cloud |
| Messaging        | Apache Kafka               |
| Database         | PostgreSQL                 |
| Cache            | Redis                      |
| AI Service       | Python                     |
| AI Framework     | LangChain                  |
| AI API           | FastAPI                    |
| Containerization | Docker                     |
| Build Tool       | Maven                      |
| Architecture     | Microservices              |
| Communication    | REST + Kafka               |

The repository's GitHub metadata also identifies Java, Spring Boot, FastAPI, LangChain, Docker, Azure, and microservices among its technologies.

---

## 📁 Project Structure

```text
FRAUD-DETECTION-SYSTEM/
│
├── MICROSERVICE/
│   │
│   ├── api-gateway/
│   │
│   ├── payment-service/
│   │
│   ├── fraud-detection-service/
│   │
│   ├── ai-reasoning-service/
│   │
│   └── .idea/
│
└── README.md
```

---

## 🔄 Transaction Flow

A typical transaction follows this flow:

```text
1. Client sends payment request
            │
            ▼
2. API Gateway
            │
            ▼
3. Payment Service
            │
            ▼
4. Transaction stored in PostgreSQL
            │
            ▼
5. Payment event published to Kafka
            │
            ▼
6. Fraud Detection Service consumes event
            │
            ▼
7. Fraud rules evaluate transaction
            │
            ▼
8. AI Reasoning Service analyzes suspicious transactions
            │
            ▼
9. Risk score / fraud decision generated
            │
            ▼
10. Transaction is:
       ├── APPROVED
       ├── REVIEW
       └── BLOCKED
```

---

## 🧠 Fraud Detection Approach

The system combines **traditional rule-based detection** with **AI-powered reasoning**.

### Rule-Based Detection

Transactions can be evaluated against conditions such as:

- Unusually high transaction amount
- Suspicious transaction frequency
- Multiple transactions within a short period
- Unusual transaction behavior
- High-risk transaction patterns
- Other configurable business rules

### AI-Based Detection

For transactions requiring deeper analysis, the AI reasoning service can analyze the available transaction context and provide an additional risk assessment.

This hybrid approach allows the system to combine:

```text
Business Rules
      +
Real-Time Event Processing
      +
AI Reasoning
      ↓
Fraud Risk Decision
```

---

## 📨 Event-Driven Architecture

Apache Kafka is used as the event streaming layer between services.

Instead of tightly coupling services through direct synchronous communication, payment events can be published to Kafka and consumed by the fraud detection service.

```text
Payment Service
      │
      │ Transaction Event
      ▼
   Kafka Topic
      │
      ▼
Fraud Detection Service
```

This architecture helps the system scale independently and process transactions asynchronously.

---

## 🗄️ Data Storage

### PostgreSQL

PostgreSQL is used for persistent transaction-related data.

Typical information may include:

```text
Transaction ID
Customer ID
Amount
Currency
Payment Method
Transaction Status
Risk Score
Created At
Updated At
```

### Redis

Redis can be used for low-latency operations such as:

- Caching
- Frequently accessed transaction information
- Temporary risk-related data
- Fast lookups

---

## ⚙️ Prerequisites

Before running the project, install:

- Java 17+
- Maven
- Python 3.10+
- PostgreSQL
- Redis
- Apache Kafka
- Docker
- Git

---

## 📥 Clone the Repository

```bash
git clone https://github.com/satyam6203/FRAUD-DETECTION-SYSTEM.git

cd FRAUD-DETECTION-SYSTEM
```

---

## ▶️ Running the Services

Navigate into each microservice and start it independently.

### API Gateway

```bash
cd MICROSERVICE/api-gateway

mvn spring-boot:run
```

### Payment Service

```bash
cd MICROSERVICE/payment-service

mvn spring-boot:run
```

### Fraud Detection Service

```bash
cd MICROSERVICE/fraud-detection-service

mvn spring-boot:run
```

### AI Reasoning Service

```bash
cd MICROSERVICE/ai-reasoning-service

pip install -r requirements.txt

uvicorn main:app --reload
```

> The exact startup commands may need to be adjusted if the service entry-point or configuration in your current branch differs.

---

## 🔑 Environment Variables

Create environment configuration for services that require external infrastructure or AI credentials.

Example:

```env
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=fraud_detection
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_password

REDIS_HOST=localhost
REDIS_PORT=6379

KAFKA_BOOTSTRAP_SERVERS=localhost:9092

AI_API_KEY=your_api_key
```

**Never commit real API keys, database passwords, or other secrets to GitHub.**

---

## 🐳 Docker

Docker can be used to containerize the individual services and supporting infrastructure.

A typical deployment can contain:

```text
API Gateway
     │
     ├── Payment Service
     │
     ├── Fraud Detection Service
     │
     └── AI Reasoning Service
            │
            ├── Kafka
            ├── PostgreSQL
            └── Redis
```

---

## 🔌 API Concept

### Create Payment

```http
POST /payments
Content-Type: application/json
```

Example:

```json
{
  "customerId": "CUST-001",
  "amount": 2500.00,
  "currency": "INR",
  "paymentMethod": "CARD"
}
```

Example response:

```json
{
  "transactionId": "TXN-10001",
  "status": "REVIEW",
  "riskScore": 0.82
}
```

> Update the endpoint names and request fields above if your implementation exposes different API contracts.

---

## 📊 Fraud Decision

The transaction can be classified based on its calculated risk.

```text
┌─────────────────────────┐
│     Transaction         │
└────────────┬────────────┘
             │
             ▼
       Risk Evaluation
             │
      ┌──────┼──────┐
      │      │      │
      ▼      ▼      ▼
    LOW    MEDIUM   HIGH
      │      │      │
      ▼      ▼      ▼
  APPROVED REVIEW  BLOCKED
```

---

## 🔒 Security Considerations

For production deployment, the following security mechanisms should be enabled:

- API authentication and authorization
- HTTPS/TLS
- Secure Kafka configuration
- Database credential protection
- Environment-based secrets
- API rate limiting
- Input validation
- Secure service-to-service communication
- Logging and monitoring
- Protection of customer and payment information

---

## 📈 Scalability

The microservices architecture allows individual components to scale independently.

For example:

```text
                 ┌── Payment Service × 3
                 │
Kafka ───────────┼── Fraud Service × 5
                 │
                 └── AI Service × 2
```

Kafka enables multiple instances of consumers to process transaction events concurrently.

This makes the architecture suitable for high-volume transaction processing.

---

## 🔮 Future Improvements

Potential improvements include:

- [ ] Add automated model retraining
- [ ] Add comprehensive automated tests
- [ ] Add centralized logging
- [ ] Add Prometheus and Grafana monitoring
- [ ] Add distributed tracing
- [ ] Add JWT/OAuth2 authentication
- [ ] Add advanced ML fraud models
- [ ] Add model-based risk scoring
- [ ] Add Kafka dead-letter topics
- [ ] Add retry and circuit-breaker mechanisms
- [ ] Add CI/CD with GitHub Actions
- [ ] Deploy services to Azure/AWS
- [ ] Add a fraud monitoring dashboard
- [ ] Add model explainability

---

## 🎯 Learning Objectives

This project demonstrates practical experience with:

- Java backend development
- Spring Boot
- Microservices architecture
- REST APIs
- Apache Kafka
- Event-driven architecture
- PostgreSQL
- Redis
- Python FastAPI
- LangChain
- AI integration
- Docker
- Distributed systems
- Real-time fraud detection

---

## 👨‍💻 Author

**Satyam Kumar**

GitHub:\
[https://github.com/satyam6203](https://github.com/satyam6203)

Repository:\
[https://github.com/satyam6203/FRAUD-DETECTION-SYSTEM](https://github.com/satyam6203/FRAUD-DETECTION-SYSTEM)

---

## ⭐ Contributing

Contributions, suggestions, and improvements are welcome.

1. Fork the repository
2. Create a feature branch

```bash
git checkout -b feature/new-feature
```

3. Commit your changes

```bash
git commit -m "Add new feature"
```

4. Push the branch

```bash
git push origin feature/new-feature
```

5. Open a Pull Request

---

## 📄 License

This project is available for educational and development purposes.

---

## ⭐ Support

If you find this project useful, consider giving the repository a ⭐ on GitHub.

**Repository:** [FRAUD-DETECTION-SYSTEM](https://github.com/satyam6203/FRAUD-DETECTION-SYSTEM?utm_source=chatgpt.com)
