package com.ledger.valuation.infrastructure.persistence.eventstore;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventStore {

    void append(StoredEventRecord record);

    List<StoredEventRecord> loadByAggregateId(UUID aggregateId);

    Optional<StoredEventRecord> findByIdempotencyToken(String idempotencyToken);

    boolean existsByAggregateIdAndSequenceNumber(UUID aggregateId, long sequenceNumber);

    List<UUID> listAggregateIds();
}
