# discovery-server

Netflix Eureka server. All other services register here on startup and discover each other by logical name (e.g. `lb://order-service`).

## Why Eureka over hard-coded hostnames?

Lets us scale services horizontally and survive container restarts without rewiring config. Also a curriculum requirement (Spring Cloud module).

## Port

`8761` (Eureka dashboard at `http://localhost:8761`)
