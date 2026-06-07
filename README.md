# LedgerValuationEngine

Enterprise event-sourced ledger valuation platform built on Java 25, Spring Boot 3.4, CockroachDB, and Kafka.

## Architecture

- **Clean Architecture**: `domain` → `application` → `infrastructure` / `interfaces`
- **CQRS**: append-only `event_store` write model + tiered L1 (Caffeine) / L2 (`account_value_read_model`) read model
- **Transactional Outbox**: claim-based relay with `FOR UPDATE SKIP LOCKED`
- **Virtual Threads**: platform-wide + Tomcat + async projection executors
- **ShedLock**: leader-elected schedulers for multi-replica safety

## Production Capabilities

### Core
- Unified command plane with idempotency (`command_idempotency` + event tokens)
- Domain-driven projection (no duplicated account-value arithmetic)
- JDBC-backed instrument positions and federated read model
- RFC 7807 `ProblemDetail` errors + OpenAPI (`/swagger-ui.html`)

### Operations
- Prometheus metrics + outbox backlog health indicator
- Kubernetes readiness/liveness probes + HPA + PDB
- CI pipeline (GitHub Actions)
- Multi-stage Docker image (non-root)

## Key APIs

| Endpoint | Purpose |
|---|---|
| `POST /api/v1/portfolios` | Open portfolio |
| `POST /api/v1/portfolios/{id}/transactions` | Commit transaction |
| `GET /api/v1/dashboard/account-values` | Dashboard snapshot |
| `GET /api/v1/audit/portfolios/{id}/events` | Regulatory audit trail |
| `POST /api/v1/admin/readside/rebuild` | Warm/rebuild read model |

## Configuration

| Property | Default | Description |
|---|---|---|
| `ledger.readside.local-only` | `false` | L1-only reads (dev); `false` enables L2 fallback |
| `ledger.tenant.enforcement` | `false` | Require `X-Tenant-Id` header |
| `ledger.security.enabled` | `false` | OAuth2 resource server |

## Run Locally

```bash
mvn spring-boot:run
```

## Deploy

```bash
mvn test package
docker build -t ledger-valuation-engine:0.1.0-SNAPSHOT .
helm install lve deploy/helm/ledger-valuation-engine
```
