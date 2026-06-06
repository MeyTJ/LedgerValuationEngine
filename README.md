# LedgerValuationEngine

Enterprise event-sourced ledger valuation platform built on Java 25, Spring Boot 3.4, CockroachDB, and Kafka.

## Architecture

- **Clean Architecture**: `domain` → `application` → `infrastructure` / `interfaces`
- **CQRS**: append-only `event_store` write model + Caffeine L1 read model
- **Transactional Outbox**: reliable projection relay after commit
- **Virtual Threads**: platform-wide + Tomcat + async projection executors

## Production Phases Implemented

### P0 — Ship-Critical
- Unified command plane (`UnifiedLedgerCommandHandler` → portfolio events)
- Transactional outbox (`outbox` table + `OutboxRelayScheduler`)
- Cold-start read model warmup (`ReadModelWarmupListener`)
- Portfolio rebuild API (`/api/v1/admin/readside/rebuild`)
- Cockroach `SERIALIZABLE` retry (`SerializationRetryUnitOfWorkAdapter`)
- Domain/handler tests + ArchUnit boundary tests

### P1 — Financial Product
- Real-time mark-to-market (`ApplyMarketTickService`, `ValuationPolicy`)
- FX rate events (`FxRateCommitted`, `FxRate`)
- Portfolio lifecycle (`OpenPortfolioCommand`, `PortfolioStatus`, `PortfolioController`)
- Scheduled accruals (`AccrualScheduler`, `AccrualPostingService`)
- Audit export API (`/api/v1/audit/portfolios/{id}/events`)

### P2 — Operational Excellence
- Prometheus metrics (`/actuator/prometheus`)
- Optional OAuth2 resource server (`ledger.security.enabled`)
- Projection drift reconciler (`ProjectionReconciler`)
- Kafka DLQ (`NormalizedMarketTicks.dlq`)

### P3 — Scale & Platform
- Shard routing (`ShardRoutingService`)
- Snapshot compaction (`SnapshotCompactionScheduler`, `AccountValueSnapshot`)
- CRDB read model federation (`account_value_read_model` table)
- Docker + Helm deployment artifacts

## Key APIs

| Endpoint | Purpose |
|---|---|
| `POST /api/v1/portfolios` | Open portfolio |
| `POST /api/v1/portfolios/{id}/transactions` | Commit transaction |
| `GET /api/v1/dashboard/account-values` | Dashboard snapshot (L1 memory) |
| `GET /api/v1/audit/portfolios/{id}/events` | Regulatory audit trail |
| `POST /api/v1/admin/readside/rebuild` | Warm/rebuild read model |

## Run Locally

```bash
mvn spring-boot:run
```

## Deploy

```bash
mvn -DskipTests package
docker build -t ledger-valuation-engine:0.1.0-SNAPSHOT .
helm install lve deploy/helm/ledger-valuation-engine
```
