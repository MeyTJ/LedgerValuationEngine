package com.ledger.valuation.application.port.outbound;

import com.ledger.valuation.domain.PortfolioLedgerEvent;

import java.util.List;
import java.util.UUID;

public interface OutboxPort {

    void enqueue(PortfolioLedgerEvent event);

    List<OutboxEntry> claimPending(int limit, String claimedBy);

    void markPublished(UUID outboxId);

    void incrementRetry(UUID outboxId);

    void markFailed(UUID outboxId);

    long countPending();

    record OutboxEntry(UUID id, UUID aggregateId, String eventType, String payload) {}
}
