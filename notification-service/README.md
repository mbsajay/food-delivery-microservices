# notification-service

Stateless consumer that turns domain events into emails / SMS.

## Consumed events

| Event                | Channel | Template                       |
|----------------------|---------|--------------------------------|
| `user.created`       | email   | `welcome.html`                 |
| `order.placed`       | email   | `order-received.html`          |
| `payment.completed`  | email   | `payment-receipt.html`         |
| `payment.failed`     | email   | `payment-failed.html`          |
| `order.dispatched`   | sms     | `agent-on-the-way.txt`         |
| `order.delivered`    | email   | `order-delivered.html`         |

## Providers

- **Email:** AWS SES (configurable; local dev uses Mailhog or a no-op logger).
- **SMS:** Twilio (no-op logger in dev).

## Idempotency

Each event carries a UUID `eventId`. Notifications log `(eventId, channel)` to an in-memory dedup cache (Caffeine) for a 24 h window to survive Kafka retries without spamming.

## Port

`8006` (only exposes `/actuator/health`)
