package com.ledger.valuation.application.readmodel;

import java.time.Instant;
import java.util.UUID;

public record AuditEventRecord(
        UUID eventId,
        UUID portfolioId,
        long sequenceNumber,
        String eventType,
        String payload,
        Instant occurredAt,
        String checksum
) {}
