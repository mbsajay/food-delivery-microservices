# payment-service

Owns payment intents, gateway integration, webhook handling, and reconciliation. The most curriculum-dense module.

## Endpoints

| Method | Path                          | Description                                    | Auth        |
|--------|-------------------------------|------------------------------------------------|-------------|
| GET    | `/payments/{orderId}`         | Payment status for an order                    | bearer      |
| POST   | `/payments/webhook`           | Provider webhook (signature-verified)          | webhook key |
| POST   | `/payments/{id}/refund`       | Issue refund (ADMIN)                           | bearer      |

## Flow

1. Consumes `order.placed` from Kafka.
2. Calls payment gateway API (mock or Razorpay/Stripe sandbox) via `RestTemplate`, creates a `PaymentIntent` record.
3. Gateway calls back via `/payments/webhook`. We verify HMAC, mark payment SUCCESS/FAILED.
4. Emits `payment.completed` or `payment.failed`.

## Reconciliation

A `@Scheduled` job every 15 min:
- Finds payments in `PENDING` older than 5 min.
- For each, calls the gateway's status endpoint.
- Updates local state if there's drift; re-emits the corresponding event.
- Logs all corrections to a `payment_recon_log` table for audit.

## Data model

```
Payment
├── id, orderId (unique), userId, amountCents, currency
├── provider, providerPaymentId
├── status: PENDING / SUCCESS / FAILED / REFUNDED
├── createdAt, updatedAt
```

## Events

**Produces:** `payment.completed`, `payment.failed`, `payment.refunded`
**Consumes:** `order.placed`

## Port

`8004`
