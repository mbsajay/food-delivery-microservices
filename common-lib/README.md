# common-lib

Shared library packaged as a JAR and consumed by every service.

## Contents

- `dto/` — request/response DTOs that cross service boundaries (e.g. `UserSummary`, `OrderEventPayload`)
- `events/` — Kafka event schemas (`OrderPlacedEvent`, `PaymentCompletedEvent`, …)
- `exceptions/` — base exception types and `ApiError` response model
- `security/` — JWT claim constants and the public `JwtVerifier` helper

## Why a shared library?

To prevent every service from re-declaring identical event payload classes (and drifting). All events are versioned (`v1`, `v2`) under their own subpackage so consumers can evolve independently.
