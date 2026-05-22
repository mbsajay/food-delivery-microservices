# order-service

Cart lifecycle and order state machine. The choreography hub of the platform.

## Endpoints

| Method | Path                        | Description                                |
|--------|-----------------------------|--------------------------------------------|
| POST   | `/cart/items`               | Add item to cart                           |
| GET    | `/cart`                     | Current user's cart                        |
| DELETE | `/cart/items/{id}`          | Remove cart item                           |
| POST   | `/orders`                   | Place order from cart → PENDING_PAYMENT    |
| GET    | `/orders`                   | Current user's orders (paged)              |
| GET    | `/orders/{id}`              | Order detail                               |

## State machine

```
PENDING_PAYMENT ──payment.completed──▶ CONFIRMED ──order.dispatched──▶ OUT_FOR_DELIVERY
       │                                   │
       └─payment.failed/timeout─▶ CANCELLED                              │
                                                                         ▼
                                                                     DELIVERED
```

## Events

**Produces:**
- `order.placed` (when status becomes `PENDING_PAYMENT`)
- `order.cancelled`

**Consumes:**
- `payment.completed` → `CONFIRMED`
- `payment.failed`    → `CANCELLED`
- `order.delivered`   → `DELIVERED` (from delivery-service)

## Data model

```
Order
├── id, userId, restaurantId, status, totalCents, addressSnapshot, createdAt
└── items[]  (OrderItem: menuItemId, name, priceCents, qty)

Cart
├── userId (PK)
└── items[]
```

## Port

`8003`
