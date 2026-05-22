# Foodly — Food Delivery Platform

A microservices-based food delivery backend built as a Master's degree capstone project for **Scaler Neovarsity — Woolf (MS in Computer Science, Backend Specialization)**. It demonstrates end-to-end backend engineering: Spring Boot REST APIs, Spring Data JPA, JWT-based authentication, asynchronous communication via Kafka, payment processing with webhooks and reconciliation, service discovery & gateway routing with Spring Cloud, and full containerization with Docker for local deployment.

---

## 1. Project Goals

- Design and implement a realistic multi-service backend that mirrors production systems (Swiggy / DoorDash / Uber Eats class).
- Apply each topic from the Backend Projects curriculum in a meaningful way — not as a contrived demo.
- Deliver a thoroughly tested, documented codebase that runs end-to-end on a single laptop via `docker compose`, with no cloud dependencies.

---

## 2. High-Level Architecture

```
                                  ┌─────────────────────┐
                                  │      Clients        │
                                  │ (Postman / curl)    │
                                  └──────────┬──────────┘
                                             │ HTTP
                                             ▼
                                  ┌─────────────────────┐
                                  │   API Gateway       │
                                  │ (Spring Cloud GW)   │
                                  └──────────┬──────────┘
                                             │
        ┌────────────┬───────────────┬───────┴───────┬───────────────┬────────────┐
        ▼            ▼               ▼               ▼               ▼            ▼
  ┌──────────┐ ┌────────────┐  ┌───────────┐  ┌────────────┐  ┌────────────┐ ┌──────────────┐
  │  User    │ │ Restaurant │  │  Order    │  │  Payment   │  │ Delivery   │ │ Notification │
  │ Service  │ │  Service   │  │ Service   │  │  Service   │  │  Service   │ │   Service    │
  └────┬─────┘ └─────┬──────┘  └─────┬─────┘  └─────┬──────┘  └─────┬──────┘ └──────┬───────┘
       │             │               │              │               │               │
       ▼             ▼               ▼              ▼               ▼               ▼
   ┌───────┐    ┌────────┐      ┌────────┐     ┌────────┐      ┌────────┐      ┌────────┐
   │users  │    │restau- │      │orders  │     │payments│      │trips   │      │ (none) │
   │  DB   │    │ rants  │      │  DB    │     │   DB   │      │  DB    │      │        │
   └───────┘    └────────┘      └────────┘     └────────┘      └────────┘      └────────┘

                            ╔═════════════════════╗
                            ║       Kafka         ║   ← async event bus
                            ╚═════════════════════╝
                              order.placed
                              payment.completed
                              payment.failed
                              order.dispatched
                              order.delivered

                            ┌─────────────────────┐
                            │   Eureka Server     │  ← service discovery
                            └─────────────────────┘
                            ┌─────────────────────┐
                            │   Config Server     │  ← centralized config
                            └─────────────────────┘
```

All components run as Docker containers on the local machine. There is no cloud deployment — the project is submitted as source code on GitHub.

---

## 3. Microservices

| Service              | Responsibility                                                | DB         | Key Topics                             |
|----------------------|---------------------------------------------------------------|------------|----------------------------------------|
| `api-gateway`        | Single ingress, routing, rate limiting, JWT verification edge | —          | Spring Cloud Gateway                   |
| `discovery-server`   | Service registry                                              | —          | Eureka                                 |
| `config-server`      | Externalized configuration                                    | —          | Spring Cloud Config                    |
| `user-service`       | Signup, login, profile, JWT issuance                          | Postgres   | JPA, JWT, BCrypt, OAuth2 resource srv  |
| `restaurant-service` | Restaurants, menu items, search (paging/sorting)              | Postgres   | JPA inheritance, Specifications        |
| `order-service`      | Cart, order lifecycle, status updates                         | Postgres   | JPA relations, Kafka producer/consumer |
| `payment-service`    | Payment intents, gateway integration, webhooks, reconciliation| Postgres   | RestTemplate, Webhooks, scheduled jobs |
| `delivery-service`   | Agent assignment, trip tracking                               | Postgres   | Domain modelling, Kafka consumer       |
| `notification-service`| Email / SMS notifications                                    | —          | Kafka consumer, 3rd-party APIs         |
| `common-lib`         | Shared DTOs, exceptions, event schemas                        | —          | Library packaging                      |

---

## 4. Tech Stack

- **Language:** Java 17
- **Framework:** Spring Boot 3.2.x, Spring Cloud 2023.0.x
- **Persistence:** Spring Data JPA, PostgreSQL 16, Flyway for migrations
- **Messaging:** Apache Kafka (Bitnami images, KRaft mode)
- **Auth:** JWT (RS256), Spring Security
- **Build:** Maven (multi-module)
- **Containerization:** Docker, Docker Compose
- **Testing:** JUnit 5, Mockito, Testcontainers, Spring Boot Test
- **Observability:** Spring Boot Actuator, Micrometer (local Prometheus optional)

---

## 5. Core Flows

### 5.1 Place Order (happy path)

```
Customer → Gateway → order-service
                       │
                       │ POST /orders
                       ▼
                     persist order (status=PENDING_PAYMENT)
                       │
                       │ produces  → kafka: order.placed
                       ▼
                  payment-service consumes order.placed
                       │
                       │ creates PaymentIntent, calls gateway API (mock)
                       │
                       ▼
                  mock gateway → webhook → /payments/webhook
                       │
                       │ verifies signature, marks payment SUCCESS
                       │ produces → kafka: payment.completed
                       ▼
              order-service consumes payment.completed
                       │ marks order CONFIRMED
                       ▼
              delivery-service consumes order.confirmed
                       │ assigns agent, creates trip
                       │ produces → kafka: order.dispatched
                       ▼
              notification-service consumes events → logs / sends email
```

### 5.2 Reconciliation Job

A scheduled job in `payment-service` runs every 15 minutes:
1. Fetches pending/intermediate payments older than 5 min.
2. Calls the (mock) gateway's payment-status API for each.
3. Updates local state if the gateway disagrees; re-emits events if needed.
4. Logs discrepancies to a `payment_recon_log` table for audit.

---

## 6. Curriculum Coverage Map

| Curriculum Topic                          | Where in project                                                   |
|-------------------------------------------|--------------------------------------------------------------------|
| Git & version control                     | This repo, conventional commits, feature branches                  |
| Spring Framework & APIs                   | Every service exposes REST controllers                             |
| MVC + Requests                            | Controller / Service / Repository layering in every module         |
| Calling 3rd-party APIs                    | `payment-service` → mock payment gateway                           |
| RestTemplate & Exception handling         | Inter-service sync calls, global `@ControllerAdvice` per service   |
| Spring Data JPA                           | Every persistence module                                           |
| UUIDs & Inheritance                       | All primary keys are UUIDs; menu items use `JOINED` inheritance    |
| JPA Queries & Repositories                | Derived queries + `@Query` + Specifications in restaurant-service  |
| Fetch types & modes                       | Lazy on collections, eager on small refs — documented per entity   |
| Unit Testing & Mocking                    | JUnit 5 + Mockito across services; Testcontainers for repo tests   |
| Authentication / OAuth2 / JWT             | `user-service` issues JWT; gateway + services validate             |
| Search: Paging & Sorting                  | `/restaurants/search` with `Pageable` & filters                    |
| Payment microservice                      | `payment-service`                                                  |
| Webhooks + reconciliation                 | `payment-service` + scheduled reconciler                           |
| Kafka async comms                         | All inter-service events                                           |
| Spring Cloud                              | Gateway, Eureka, Config Server                                     |
| Docker                                    | Dockerfile per service + root `docker-compose.yml`                 |

---

## 7. Local Development

### Prerequisites
- JDK 17, Maven 3.9+
- Docker Desktop
- (Optional) Postman / curl for API testing

### Quick start (everything via Docker)

```bash
# 1. Build all service images
mvn clean package -DskipTests
docker compose build

# 2. Start the full stack (infra + all services)
docker compose up -d

# 3. Tail logs
docker compose logs -f api-gateway order-service
```

API gateway will be reachable at `http://localhost:8080`.
Kafka UI at `http://localhost:8081`.
Eureka dashboard at `http://localhost:8761`.

### Run a single service locally (faster iteration)

```bash
# Start only the shared infra
docker compose up -d postgres kafka redis

# Then run the service you're working on from your IDE or:
mvn -pl user-service spring-boot:run
```

### Run the test suite

```bash
mvn test                 # unit tests across all modules
mvn verify               # also runs integration tests via Testcontainers
```

---

## 8. Repository Layout

```
foodly/
├── README.md                  # this file
├── docker-compose.yml         # entire stack runs locally from here
├── pom.xml                    # parent (Maven multi-module)
├── .gitignore
├── docs/
│   ├── Foodly_Project_Report.docx   # final submission report
│   ├── build_report.py              # regenerates the report from the template
│   ├── architecture.md              # deeper architecture notes (optional)
│   └── adr/                         # Architecture Decision Records
├── docker/
│   └── postgres/init-multiple-dbs.sh  # creates the 5 logical DBs on container start
├── common-lib/
├── api-gateway/
├── discovery-server/
├── config-server/
├── user-service/
├── restaurant-service/
├── order-service/
├── payment-service/
├── delivery-service/
└── notification-service/
```

---

## 9. Build & Submission Roadmap

| Phase | Focus                                            | Deliverable                              |
|-------|--------------------------------------------------|------------------------------------------|
| 1     | Scaffolding, docker-compose, common-lib          | Empty services that boot                 |
| 2     | `user-service` (JWT, signup, login)              | Working auth + tests                     |
| 3     | `restaurant-service` (catalog, search)           | CRUD + paged search                      |
| 4     | `order-service` (cart, order)                    | Order placement & lifecycle              |
| 5     | `payment-service` + webhook + reconciliation     | End-to-end happy path                    |
| 6     | `delivery-service` + `notification-service`      | Full event-driven flow                   |
| 7     | Spring Cloud wiring (gateway, eureka, config)    | Single ingress, dynamic discovery        |
| 8     | Dockerization                                    | `docker compose up` runs the whole stack |
| 9     | Final docs, slides, screencast for submission    | Submission package + GitHub link         |

---

## 10. Submission

The project is submitted as source code in this repository:

```
https://github.com/<your-github-username>/foodly
```

Along with:

- `docs/Foodly_Project_Report.docx` — the formal project report following the Scaler Neovarsity template.
- A short screencast demonstrating an end-to-end order flow (recorded against `docker compose up`).

---

## 11. License

Built for academic purposes; reuse permitted under the MIT License (see `LICENSE`).
