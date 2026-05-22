# user-service

Owns customer identity: signup, login, profile management, JWT issuance.

## Endpoints

| Method | Path                       | Description                       | Auth      |
|--------|----------------------------|-----------------------------------|-----------|
| POST   | `/auth/signup`             | Create a new user                 | public    |
| POST   | `/auth/login`              | Email+password → JWT (access+refresh) | public |
| POST   | `/auth/refresh`            | Exchange refresh token for new pair | public  |
| GET    | `/users/me`                | Get current user profile          | bearer    |
| PUT    | `/users/me`                | Update name / phone / address     | bearer    |
| GET    | `/users/{id}/summary`      | Lightweight user lookup (internal)| bearer    |

## Data model

```
User
├── id            UUID (PK)
├── email         unique
├── passwordHash  bcrypt
├── name
├── phone
├── role          ENUM(CUSTOMER, ADMIN, DELIVERY_AGENT)
├── createdAt
└── updatedAt
```

## JWT

- Algorithm: **RS256** (keypair lives in `config-server`, public key shared via JWKS endpoint).
- Access token TTL: 15 min. Refresh token TTL: 7 days, rotated on each refresh.
- Claims: `sub` (userId), `roles`, `iat`, `exp`, `tokenType`.

## Events emitted

- `user.created` — on signup, consumed by `notification-service` for welcome email.

## Port

`8001`
