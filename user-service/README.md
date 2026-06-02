# user-service

Owns customer identity: signup, login, profile management, JWT issuance. Runs as an
OAuth2 resource server — it both **issues** RS256 tokens and **validates** them on its
own protected endpoints, the same way every downstream service will.

## Endpoints

| Method | Path                       | Description                            | Auth   |
|--------|----------------------------|----------------------------------------|--------|
| POST   | `/auth/signup`             | Create a new user                      | public |
| POST   | `/auth/login`              | Email+password → JWT (access+refresh)  | public |
| POST   | `/auth/refresh`            | Exchange refresh token for a new pair  | public |
| GET    | `/users/me`                | Current user profile                   | bearer |
| PUT    | `/users/me`                | Update full name / phone               | bearer |
| GET    | `/users/{id}/summary`      | Lightweight user lookup (service-to-service) | bearer |

Through the gateway every path is prefixed with `/api` (e.g. `POST /api/auth/login`),
which `StripPrefix=1` rewrites back to the paths above.

## Data model

```
User
├── id             UUID (PK, gen_random_uuid)
├── email          unique (case-insensitive)
├── password_hash  bcrypt
├── full_name
├── phone
├── role           ENUM(CUSTOMER, RESTAURANT_OWNER, DELIVERY_AGENT, ADMIN) as VARCHAR
├── enabled        boolean
├── created_at
└── updated_at
```

Schema is created by Flyway (`db/migration/V1__create_users.sql`) and validated at
startup (`ddl-auto: validate`).

## JWT

- Algorithm **RS256** (jjwt). The keypair lives in `config-server`
  (`foodly.security.jwt.{private-key,public-key}`, base64 DER); the private key signs
  here, the public key is read by every resource server. Override via `JWT_PRIVATE_KEY`
  / `JWT_PUBLIC_KEY` env vars in real deployments.
- Access token TTL 15 min, refresh token TTL 7 days. `/auth/refresh` mints a fresh pair.
- Claims: `sub` (userId), `email`, `roles`, `tokenType` (`access`/`refresh`), `iat`, `exp`.

## Running locally

```bash
# infra + config/discovery must be up first
docker compose up -d postgres
mvn -pl discovery-server spring-boot:run   # 8761
mvn -pl config-server   spring-boot:run    # 8888
# then, from inside the module dir (root reactor still lists not-yet-built services):
cd user-service && mvn spring-boot:run      # 8001
```

## Try it (direct to :8001)

```bash
# 1. sign up
curl -s -X POST localhost:8001/auth/signup -H 'Content-Type: application/json' \
  -d '{"email":"ada@foodly.io","password":"supersecret","fullName":"Ada Lovelace"}'

# 2. log in → capture the access token
ACCESS=$(curl -s -X POST localhost:8001/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"ada@foodly.io","password":"supersecret"}' | jq -r .data.accessToken)

# 3. call a protected endpoint
curl -s localhost:8001/users/me -H "Authorization: Bearer $ACCESS"
```

Through the gateway, swap `localhost:8001/<path>` for `localhost:8080/api/<path>`.

## Tests

- `mvn -pl user-service test` — `AuthServiceTest` (Mockito) + `UserControllerIntegrationTest` (MockMvc).
- `mvn -pl user-service verify` — adds `UserRepositoryPostgresIT` (Testcontainers, needs Docker).

## Not yet wired

- `user.created` Kafka event (planned, lands once `notification-service` exists to consume it).
- Refresh-token persistence/revocation (currently stateless).

## Port

`8001`
