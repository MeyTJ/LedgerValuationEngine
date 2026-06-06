package com.ledger.valuation.domain;

import java.time.Instant;
import java.util.UUID;

public record AuditExportJob(
        UUID id,
        UUID portfolioId,
        String tenantId,
        long fromSequence,
        long toSequence,
        AuditExportJobStatus status,
        String storagePath,
        String manifestChecksum,
        int eventCount,
        Instant createdAt,
        Instant completedAt
) {}
