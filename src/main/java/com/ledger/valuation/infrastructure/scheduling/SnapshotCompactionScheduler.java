package com.ledger.valuation.infrastructure.scheduling;

import com.ledger.valuation.application.port.outbound.EventStorePort;
import com.ledger.valuation.application.port.outbound.LedgerWriteUnitOfWorkPort;
import com.ledger.valuation.application.port.outbound.OutboxPort;
import com.ledger.valuation.application.port.outbound.PortfolioEventStorePort;
import com.ledger.valuation.domain.Portfolio;
import com.ledger.valuation.domain.PortfolioLedgerEvent;
import com.ledger.valuation.domain.PortfolioLedgerEventFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SnapshotCompactionScheduler {

    private final EventStorePort eventStore;
    private final PortfolioEventStorePort portfolioEventStore;
    private final PortfolioLedgerEventFactory eventFactory;
    private final LedgerWriteUnitOfWorkPort unitOfWork;
    private final OutboxPort outbox;
    private final long compactionThreshold;
    private final boolean enabled;

    public SnapshotCompactionScheduler(
            EventStorePort eventStore,
            PortfolioEventStorePort portfolioEventStore,
            PortfolioLedgerEventFactory eventFactory,
            LedgerWriteUnitOfWorkPort unitOfWork,
            OutboxPort outbox,
            @Value("${ledger.snapshot.compaction-threshold:1000}") long compactionThreshold,
            @Value("${ledger.snapshot.enabled:false}") boolean enabled
    ) {
        this.eventStore = eventStore;
        this.portfolioEventStore = portfolioEventStore;
        this.eventFactory = eventFactory;
        this.unitOfWork = unitOfWork;
        this.outbox = outbox;
        this.compactionThreshold = compactionThreshold;
        this.enabled = enabled;
    }

    @Scheduled(cron = "${ledger.snapshot.cron:0 30 3 * * SUN}")
    public void compactLongStreams() {
        if (!enabled) {
            return;
        }
        for (UUID portfolioId : eventStore.listAggregateIds()) {
            var stream = portfolioEventStore.loadStream(portfolioId);
            if (stream.size() < compactionThreshold) {
                continue;
            }
            unitOfWork.execute(() -> {
                Portfolio portfolio = Portfolio.rehydrateFromEventRecords(stream.eventRecords());
                PortfolioLedgerEvent snapshot = eventFactory.createAccountValueSnapshot(portfolio);
                portfolioEventStore.append(snapshot);
                outbox.enqueue(snapshot);
                return null;
            });
        }
    }
}
