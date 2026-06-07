package com.ledger.valuation.application.port.outbound;

import java.util.Optional;
import java.util.UUID;

public interface CommandIdempotencyPort {

    Optional<IdempotencyRecord> findByToken(String idempotencyToken);

    void record(String idempotencyToken, String commandType, UUID aggregateId, UUID resultEventId);

    record IdempotencyRecord(
            String idempotencyToken,
            String commandType,
            UUID aggregateId,
            UUID resultEventId
    ) {}
}
