package com.ledger.valuation.infrastructure.persistence.eventstore;

import java.time.Instant;
import java.util.UUID;

public record StoredEventRecord(
        UUID id,
        UUID aggregateId,
        long sequenceNumber,
        String eventType,
        String payload,
        Instant occurredAt,
        String idempotencyToken
) {}
