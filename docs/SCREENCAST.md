# Foodly — Screencast / Demo Script

A recording script for the Phase 9 submission screencast. It walks the full order
lifecycle end-to-end against `docker compose up`, narrating what each step proves.
Target length: **8–12 minutes**. Record at 1080p, terminal font ≥ 16pt.

> Tip: keep two terminals visible — one for `docker compose logs -f`, one for the
> `curl` calls — so the audience sees the Kafka choreography react in real time.

---

## 0. Before you hit record (setup, off-camera)

```bash
docker compose build
docker compose up -d
# Wait until discovery + config are healthy, then the rest register.
docker compose ps
```

Confirm all containers are `healthy`/`running`, then in a spare terminal start the
log tail you'll show on camera:

```bash
docker compose logs -f order-service payment-service delivery-service notification-service
```

Have the README §7 demo block open as a cue card.

---

## 1. Intro (≈1 min)

- One sentence on the project: *"Foodly is a food-delivery backend built as nine
  Spring Boot microservices that coordinate over Kafka, submitted for the Scaler
  Neovarsity MS CS Backend capstone."*
- Show the architecture diagram (README §2): clients → API gateway → services,
  with Kafka as the async spine and Eureka + Config Server as the platform plane.
- Name the slice you'll demo: **signup → login → browse → order → pay → dispatch →
  deliver**, all through the gateway on `:8080`.

## 2. The platform is up (≈1 min)

- `docker compose ps` — point out the eight services + Postgres/Kafka/Redis.
- Open the **Eureka dashboard** at http://localhost:8761 — show every service
  registered. This proves service discovery is live.
- (Optional) http://localhost:8081 **Kafka UI** — show the five topics:
  `order.placed`, `payment.completed`, `payment.failed`, `order.dispatched`,
  `order.delivered`.

## 3. Identity: signup + login (≈1.5 min)

```bash
# Sign up a customer
curl -s -X POST localhost:8080/api/auth/signup \
  -H 'Content-Type: application/json' \
  -d '{"email":"ada@foodly.io","password":"supersecret","fullName":"Ada Lovelace"}' | jq

# Log in → capture the access token
ACCESS=$(curl -s -X POST localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"ada@foodly.io","password":"supersecret"}' | jq -r .data.accessToken)
echo $ACCESS
```

Narrate: the gateway routed `/api/auth/**` to user-service, which issued an
**RS256 JWT**. Decode the token at jwt.io (or `jq` the payload) to show the
`roles` claim — this is what downstream services validate with the public key from
config-server.

```bash
# Prove the token works on a protected endpoint
curl -s localhost:8080/api/users/me -H "Authorization: Bearer $ACCESS" | jq
```

## 4. Catalog: create a restaurant + menu (≈1.5 min)

Sign up / log in a `RESTAURANT_OWNER` (or reuse the curl block with role set), then:

```bash
RID=$(curl -s -X POST localhost:8080/api/restaurants \
  -H "Authorization: Bearer $OWNER" -H 'Content-Type: application/json' \
  -d '{"name":"Trattoria","cuisine":"Italian","city":"Pune","address":"MG Road"}' | jq -r .data.id)

curl -s -X POST localhost:8080/api/restaurants/$RID/menu \
  -H "Authorization: Bearer $OWNER" -H 'Content-Type: application/json' \
  -d '{"name":"Margherita","category":"Mains","price":9.50,"available":true}' | jq

# Public search — no token needed
curl -s "localhost:8080/api/restaurants?city=Pune" | jq
```

Narrate the `@PreAuthorize` owner check (POST is owner-only) versus the public GET.

## 5. The money shot: place an order and watch the choreography (≈3 min)

This is the centerpiece — switch focus to the **log tail** terminal as you fire it.

```bash
ORDER=$(curl -s -X POST localhost:8080/api/orders \
  -H "Authorization: Bearer $ACCESS" -H 'Content-Type: application/json' \
  -d "{\"restaurantId\":\"$RID\",\"currency\":\"USD\",
       \"items\":[{\"menuItemId\":\"$MID\",\"name\":\"Margherita\",\"quantity\":2,\"unitPrice\":9.50}]}")
OID=$(echo $ORDER | jq -r .data.id)
echo "Order $OID placed as PENDING_PAYMENT"
```

Walk the audience through the cascade visible in the logs, in order:

1. **order-service** saves the order `PENDING_PAYMENT`, emits `order.placed`.
2. **payment-service** consumes it, charges the (simulated) gateway, emits
   `payment.completed`.
3. **order-service** consumes `payment.completed` → order → `CONFIRMED`.
4. **delivery-service** consumes `payment.completed`, assigns a courier, emits
   `order.dispatched` → order → `OUT_FOR_DELIVERY`.
5. **notification-service** has been recording a notification at every step.

Then poll the order to show it advanced without any further client call:

```bash
curl -s localhost:8080/api/orders/$OID -H "Authorization: Bearer $ACCESS" | jq '.data.status'
# → "OUT_FOR_DELIVERY"
```

## 6. Complete the trip (≈1 min)

```bash
# Delivery agent marks the trip complete
curl -s -X POST localhost:8080/api/delivery/order/$OID/complete \
  -H "Authorization: Bearer $AGENT" -H 'Content-Type: application/json' \
  -d '{"customerRating":5}' | jq

curl -s localhost:8080/api/orders/$OID -H "Authorization: Bearer $ACCESS" | jq '.data.status'
# → "DELIVERED"
```

Show `order.delivered` in the logs driving the terminal state.

## 7. Show the notifications + reconciliation safety net (≈1 min)

```bash
# Every lifecycle event fanned out to the in-memory notification buffer
curl -s localhost:8080/api/notifications | jq
```

Mention the **payment reconciliation job**: a `@Scheduled` task in payment-service
that every 15 min re-checks stale `PENDING` payments against the gateway, fixes
drift, re-emits the event, and writes a `payment_recon_log` audit row — the safety
net behind the synchronous charge.

## 8. Wrap (≈30 s)

- Recap the slice you just proved: one HTTP call set off an event-driven cascade
  across four services, ending in a delivered order, with auth enforced at every hop.
- Point to the GitHub repo and `docs/Foodly_Project_Report.docx` for the full writeup.
- `docker compose down -v` to tear down (off-camera).

---

## Quick reference — token variables used above

| Variable | Role          | How to get it |
|----------|---------------|---------------|
| `$ACCESS` | CUSTOMER       | signup/login `ada@foodly.io` |
| `$OWNER`  | RESTAURANT_OWNER | signup with `"role":"RESTAURANT_OWNER"`, then login |
| `$AGENT`  | DELIVERY_AGENT | signup with `"role":"DELIVERY_AGENT"`, then login |
| `$RID`    | —             | restaurant id from step 4 |
| `$MID`    | —             | menu item id from step 4 |
| `$OID`    | —             | order id from step 5 |
