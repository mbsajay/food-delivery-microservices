# delivery-service

Assigns delivery agents to confirmed orders and tracks trip status.

## Flow

1. Consumes `payment.completed`.
2. Picks an available agent for the restaurant's region (simple round-robin for v1).
3. Creates a `Trip` record, emits `order.dispatched`.
4. Agent updates status via `PATCH /trips/{id}/status` → emits `order.delivered` on completion.

## Endpoints

| Method | Path                       | Description                       |
|--------|----------------------------|-----------------------------------|
| GET    | `/trips/{id}`              | Trip detail                       |
| GET    | `/trips/agent/me`          | Current agent's active trips      |
| PATCH  | `/trips/{id}/status`       | Update trip status                |

## Data model

```
Agent
├── id, name, phone, isAvailable, currentRegion

Trip
├── id, orderId, agentId, status (ASSIGNED/PICKED_UP/DELIVERED), pickedUpAt, deliveredAt
```

## Port

`8005`
