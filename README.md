# Foodly — Food Delivery Platform

A microservices-based food delivery backend built as a Master's degree capstone project for **Scaler Neovarsity — Woolf (MS in Computer Science, Backend Specialization)**. It demonstrates end-to-end backend engineering: Spring Boot REST APIs, Spring Data JPA, JWT-based authentication, asynchronous communication via Kafka, a simulated payment gateway with a scheduled reconciliation job, service discovery & gateway routing with Spring Cloud, and full containerization with Docker for local deployment.

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
   │users  │    │restau- │      │orders  │     │payments│      │deliv-  │      │ (none) │
   │  DB   │    │ rants  │      │  DB    │     │   DB   │      │ eries  │      │        │
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
| `api-gateway`        | Single ingress, routing, JWT verification edge                | —          | Spring Cloud Gateway                   |
| `discovery-server`   | Service registry                                              | —          | Eureka                                 |
| `config-server`      | Externalized configuration                                    | —          | Spring Cloud Config                    |
| `user-service`       | Signup, login, profile, JWT issuance                          | Postgres   | JPA, JWT, BCrypt, OAuth2 resource srv  |
| `restaurant-service` | Restaurants, menu items, search (paging/sorting)              | Postgres   | JPA, derived queries, @PreAuthorize    |
| `order-service`      | Order placement, lifecycle, status transitions               | Postgres   | JPA relations, Kafka producer/consumer |
| `payment-service`    | Simulated charge, reconciliation job, audit log               | Postgres   | Kafka consumer, scheduled jobs         |
| `delivery-service`   | Courier assignment, delivery completion                       | Postgres   | Domain modelling, Kafka consume→produce|
| `notification-service`| Lifecycle notifications (in-memory store)                    | —          | Kafka fan-out consumers                |
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
                       │ POST /api/orders   (Bearer JWT)
                       ▼
                     persist order (status=PENDING_PAYMENT)
                       │ produces → kafka: order.placed
                       ▼
                  payment-service consumes order.placed
                       │ creates Payment, charges the (simulated) gateway synchronously
                       │ produces → kafka: payment.completed   (or payment.failed)
                       ├───────────────────────────────┐
                       ▼                                ▼
       order-service consumes payment.completed   delivery-service consumes payment.completed
              marks order CONFIRMED                    assigns courier, persists delivery
                                                       produces → kafka: order.dispatched
                       ┌───────────────────────────────┘
                       ▼
              order-service consumes order.dispatched → marks order OUT_FOR_DELIVERY
                       │
   courier completes trip:  POST /api/delivery/order/{id}/complete
                       │ delivery-service produces → kafka: order.delivered
                       ▼
              order-service consumes order.delivered → marks order DELIVERED

   notification-service consumes every event above → records a notification (in-memory + log)
```

> Note: the payment gateway is a deterministic in-process simulation (any positive amount
> succeeds), so the charge resolves synchronously rather than via an inbound webhook. The
> reconciliation job (§5.2) is the asynchronous safety net behind it.

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
| Calling 3rd-party APIs                    | `payment-service` → simulated payment gateway                      |
| Exception handling                        | Global `@RestControllerAdvice` per service → shared `ApiResponse`  |
| Spring Data JPA                           | Every persistence module                                           |
| UUIDs                                     | All primary keys are UUIDs (`gen_random_uuid()` + Hibernate)       |
| JPA Queries & Repositories                | Derived queries (`findByActiveTrueAndCity…`) across services       |
| Fetch types & modes                       | Lazy on `@ManyToOne`/collections (menu items, order items)         |
| Unit Testing & Mocking                    | JUnit 5 + Mockito; Testcontainers Postgres IT in user-service      |
| Authentication / OAuth2 / JWT             | `user-service` issues RS256 JWT; every service validates as RS     |
| Search: Paging & Sorting                  | `GET /api/restaurants?city=&q=&page=&size=` with `Pageable`        |
| Payment microservice                      | `payment-service`                                                  |
| Reconciliation                            | `payment-service` scheduled reconciler + `payment_recon_log`       |
| Kafka async comms                         | All inter-service events                                           |
| Spring Cloud                              | Gateway, Eureka, Config Server                                     |
| Docker                                    | Dockerfile per service + root `docker-compose.yml`                 |

---

## 7. Running the Application

### 7.1 Prerequisites

| Tool | Version | Notes |
|------|---------|-------|
| JDK | 17 | `java -version` should report 17.x |
| Maven | 3.9+ | or use the IDE's bundled Maven |
| Docker Desktop | latest | provides Postgres, Kafka, Redis (and the full stack in Option A) |
| `curl` + `jq` | any | for the demo walkthrough below (optional) |

### 7.2 Service & port map

| Component | URL | | Component | URL |
|---|---|---|---|---|
| api-gateway | http://localhost:8080 | | payment-service | http://localhost:8004 |
| discovery (Eureka) | http://localhost:8761 | | delivery-service | http://localhost:8005 |
| config-server | http://localhost:8888 | | notification-service | http://localhost:8006 |
| user-service | http://localhost:8001 | | Kafka UI | http://localhost:8081 |
| restaurant-service | http://localhost:8002 | | Postgres | localhost:5432 (`foodly`/`foodly`) |
| order-service | http://localhost:8003 | | Kafka (host listener) | localhost:9094 |

All client traffic goes through the gateway on **8080** under the `/api/**` prefix
(e.g. `POST /api/auth/login`). The single Postgres container hosts five logical DBs
(`users`, `restaurants`, `orders`, `payments`, `delivery`) created on first start by
`docker/postgres/init-multiple-dbs.sh`.

---

### 7.3 Option A — full stack via Docker Compose (recommended)

Each service image is built from a pre-compiled jar, so build the jars first, then the images:

```bash
# 1. Build every module's jar (multi-module reactor)
mvn clean package -DskipTests

# 2. Build the service images and start everything (infra + all 8 services)
docker compose build
docker compose up -d

# 3. Watch it come up — services boot in dependency order via healthchecks:
#    discovery → config-server → gateway + domain services
docker compose ps
docker compose logs -f config-server api-gateway
```

Startup is staged: domain services wait for `config-server` to be **healthy** and Postgres
to pass its healthcheck, then register with Eureka. Give it ~60–90s, then confirm all
instances are `UP` at http://localhost:8761.

```bash
# Tear down (keep data)            # Tear down + wipe volumes
docker compose down                docker compose down -v
```

> In containers, services talk to `config-server:8888`, `discovery-server:8761`,
> `postgres:5432` and `kafka:9092` — wired via env vars in `docker-compose.yml`.

---

### 7.4 Option B — run services on the host (fast iteration)

Start only the infra in Docker, then run the Spring Cloud pieces and the service you're
working on. Order matters: **discovery → config-server → (gateway) → domain service**.

```bash
# 1. Infra only
docker compose up -d postgres kafka redis kafka-ui

# 2. Spring Cloud backbone (separate terminals, or -D spring-boot:start)
mvn -pl discovery-server spring-boot:run     # 8761
mvn -pl config-server   spring-boot:run      # 8888  (serves config to everyone else)

# 3. The gateway + whichever service you're iterating on
mvn -pl api-gateway  spring-boot:run         # 8080
mvn -pl user-service spring-boot:run         # 8001
```

On the host, services read Kafka at `localhost:9094` and Postgres/Eureka/Config at their
`localhost` defaults — no env vars required. Override any of them with the variables in §7.6.

---

### 7.5 End-to-end demo (through the gateway)

This exercises the whole choreography: signup → login → create catalog → place order →
payment → dispatch → delivered, with notifications fanned out along the way.

```bash
GW=http://localhost:8080

# --- 1. A restaurant owner signs up and logs in ---
curl -s -X POST $GW/api/auth/signup -H 'Content-Type: application/json' -d '{
  "email":"owner@foodly.io","password":"supersecret","fullName":"Olivia Owner","role":"RESTAURANT_OWNER"}'
OWNER=$(curl -s -X POST $GW/api/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"owner@foodly.io","password":"supersecret"}' | jq -r .data.accessToken)

# --- 2. Owner creates a restaurant and a menu item ---
RID=$(curl -s -X POST $GW/api/restaurants -H "Authorization: Bearer $OWNER" -H 'Content-Type: application/json' \
  -d '{"name":"Pasta Palace","cuisine":"Italian","city":"Bengaluru","address":"MG Road"}' | jq -r .data.id)
MID=$(curl -s -X POST $GW/api/restaurants/$RID/menu -H "Authorization: Bearer $OWNER" -H 'Content-Type: application/json' \
  -d '{"name":"Margherita","category":"Pizza","price":9.50,"available":true}' | jq -r .data.id)

# --- 3. A customer signs up, logs in, and places an order ---
curl -s -X POST $GW/api/auth/signup -H 'Content-Type: application/json' -d '{
  "email":"cara@foodly.io","password":"supersecret","fullName":"Cara Customer"}'
CUST=$(curl -s -X POST $GW/api/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"cara@foodly.io","password":"supersecret"}' | jq -r .data.accessToken)
OID=$(curl -s -X POST $GW/api/orders -H "Authorization: Bearer $CUST" -H 'Content-Type: application/json' \
  -d "{\"restaurantId\":\"$RID\",\"currency\":\"USD\",\"items\":[{\"menuItemId\":\"$MID\",\"name\":\"Margherita\",\"quantity\":2,\"unitPrice\":9.50}]}" \
  | jq -r .data.id)

# --- 4. Within a moment payment + dispatch happen via Kafka. Check the order status: ---
curl -s $GW/api/orders/$OID -H "Authorization: Bearer $CUST" | jq '.data.status'   # CONFIRMED → OUT_FOR_DELIVERY
curl -s $GW/api/payments/order/$OID -H "Authorization: Bearer $CUST" | jq '.data.status'  # COMPLETED

# --- 5. A delivery agent completes the trip (emits order.delivered) ---
curl -s -X POST $GW/api/auth/signup -H 'Content-Type: application/json' -d '{
  "email":"dan@foodly.io","password":"supersecret","fullName":"Dan Driver","role":"DELIVERY_AGENT"}'
AGENT=$(curl -s -X POST $GW/api/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"dan@foodly.io","password":"supersecret"}' | jq -r .data.accessToken)
curl -s -X POST "$GW/api/delivery/order/$OID/complete?rating=5" -H "Authorization: Bearer $AGENT" | jq '.data.status'

curl -s $GW/api/orders/$OID -H "Authorization: Bearer $CUST" | jq '.data.status'   # DELIVERED
curl -s $GW/api/notifications -H "Authorization: Bearer $CUST" | jq '.data[].message'
```

You can also watch the events flow in **Kafka UI** (http://localhost:8081): topics
`order.placed`, `payment.completed`, `order.dispatched`, `order.delivered`.

---

### 7.6 Configuration & environment variables

Config is centralized in `config-server` (`config-server/src/main/resources/config/*.yml`),
served to each service at boot. Common overrides (defaults shown):

| Variable | Default (host) | Purpose |
|----------|----------------|---------|
| `CONFIG_SERVER_URI` | `http://localhost:8888` | where a service fetches its config |
| `EUREKA_DEFAULT_ZONE` | `http://localhost:8761/eureka/` | service registry URL |
| `POSTGRES_HOST` / `POSTGRES_PORT` | `localhost` / `5432` | database host |
| `POSTGRES_USER` / `POSTGRES_PASSWORD` | `foodly` / `foodly` | database credentials |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9094` | Kafka (host listener; `kafka:9092` in-container) |
| `JWT_PRIVATE_KEY` / `JWT_PUBLIC_KEY` | bundled dev RS256 keypair | base64 DER keys — **override in any real deployment** |

### 7.7 Tests

```bash
mvn test       # unit tests across all modules (Mockito, MockMvc)
mvn verify     # also runs Testcontainers integration tests (needs Docker running)
```

### 7.8 Troubleshooting

- **A service can't reach config-server on boot** — start `config-server` first; clients
  retry 6× (see each `bootstrap.yml`) but will fail-fast if it never comes up.
- **Kafka connection refused from the host** — use `localhost:9094`, not `9092`. The broker
  advertises `kafka:9092` to the Docker network and `localhost:9094` to the host.
- **Flyway / `ddl-auto: validate` mismatch on startup** — the schema is owned by the Flyway
  `V1__*.sql` migration in each service; if you change an entity, add a new migration rather
  than editing the validated one.
- **401 from a service endpoint** — make sure you send `Authorization: Bearer <accessToken>`
  from a fresh `/api/auth/login` (access tokens expire after 15 min).

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

| Phase | Focus                                            | Deliverable                              | Status |
|-------|--------------------------------------------------|------------------------------------------|--------|
| 1     | Scaffolding, docker-compose, common-lib          | Empty services that boot                 | ✅ done |
| 2     | `user-service` (JWT, signup, login)              | Working auth + tests                     | ✅ done |
| 3     | `restaurant-service` (catalog, search)           | CRUD + paged search                      | ✅ done |
| 4     | `order-service` (order placement)                | Order placement & lifecycle              | ✅ done |
| 5     | `payment-service` + reconciliation               | End-to-end happy path                    | ✅ done |
| 6     | `delivery-service` + `notification-service`      | Full event-driven flow                   | ✅ done |
| 7     | Spring Cloud wiring (gateway, eureka, config)    | Single ingress, dynamic discovery        | ✅ done |
| 8     | Dockerization                                    | `docker compose up` runs the whole stack | ✅ done |
| 9     | Final docs, slides, screencast for submission    | Submission package + GitHub link         | ⏳ pending |

> All nine services build from the root reactor and the full stack starts with
> `docker compose up`. Phase 9 (formal report + screencast) is the remaining
> submission packaging work.

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
