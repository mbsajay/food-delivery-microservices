# api-gateway

Single ingress for all client traffic. Built with **Spring Cloud Gateway** (reactive).

## Responsibilities

- Route external requests to internal services using path predicates.
- Verify JWTs at the edge and forward `X-User-Id` / `X-User-Roles` headers downstream.
- Apply per-route rate limiting via Redis (`RequestRateLimiter` filter).
- CORS handling.

## Routes (initial)

| Path                       | Service              |
|----------------------------|----------------------|
| `/api/auth/**`             | `user-service`       |
| `/api/users/**`            | `user-service`       |
| `/api/restaurants/**`      | `restaurant-service` |
| `/api/orders/**`           | `order-service`      |
| `/api/payments/**`         | `payment-service`    |
| `/api/payments/webhook`    | `payment-service` (auth-skipped, signature-verified inside) |
| `/api/delivery/**`         | `delivery-service`   |

## Port

`8080`
