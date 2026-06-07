package com.ledger.valuation.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "ledger")
public record LedgerProperties(
        ReadSide readside,
        Outbox outbox,
        Cockroach cockroach,
        Security security,
        Tenant tenant,
        Snapshot snapshot,
        Accrual accrual,
        Reconciliation reconciliation,
        Audit audit,
        Stream stream
) {
    public record ReadSide(Cache cache, boolean localOnly, boolean warmupOnStartup) {
        public record Cache(long maxEntries, Duration expireAfterWrite) {}
    }

    public record Outbox(int batchSize, Duration relayInterval, int maxRetries, Duration claimTimeout) {}

    public record Cockroach(Retry retry) {
        public record Retry(int maxAttempts, long baseBackoffMs) {}
    }

    public record Security(boolean enabled) {}

    public record Tenant(boolean enforcement) {}

    public record Snapshot(boolean enabled, long compactionThreshold, String cron) {}

    public record Accrual(boolean enabled, String cron, long defaultFeeMinorUnits) {}

    public record Reconciliation(Duration interval) {}

    public record Audit(Export export) {
        public record Export(String storagePath, int batchSize, Duration workerInterval) {}
    }

    public record Stream(int maxConnections) {}
}
