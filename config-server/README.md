# config-server

Spring Cloud Config Server. Serves environment-specific properties to every service from a central place (`config-repo/` directory or a Git repo).

## Why centralized config?

- Rotate secrets / change feature flags without rebuilding services.
- Differentiate `dev` / `staging` / `prod` from one source of truth.
- Curriculum requirement.

## Port

`8888`

## Layout

```
config-repo/
├── application.yml              # defaults for all services
├── user-service.yml
├── user-service-prod.yml
├── order-service.yml
└── ...
```
