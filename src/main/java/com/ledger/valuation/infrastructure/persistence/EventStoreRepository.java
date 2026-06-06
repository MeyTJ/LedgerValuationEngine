package com.ledger.valuation.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventStoreRepository extends JpaRepository<EventStoreRecord, UUID> {

    List<EventStoreRecord> findByAggregateIdOrderBySequenceNumberAsc(UUID aggregateId);

    boolean existsByAggregateIdAndSequenceNumber(UUID aggregateId, long sequenceNumber);
}
