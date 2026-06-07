package com.ledger.valuation.infrastructure.scheduling;

import com.ledger.valuation.application.port.outbound.LedgerWriteUnitOfWorkPort;
import com.ledger.valuation.application.port.outbound.OutboxPort;
import com.ledger.valuation.application.port.outbound.PortfolioEventStorePort;
import com.ledger.valuation.domain.Portfolio;
import com.ledger.valuation.domain.PortfolioLedgerEvent;
import com.ledger.valuation.domain.PortfolioLedgerEventFactory;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SnapshotCompactionScheduler {

    private final PortfolioEventStorePort portfolioEventStore;
    private final PortfolioLedgerEventFactory eventFactory;
    private final LedgerWriteUnitOfWorkPort unitOfWork;
    private final OutboxPort outbox;
    private final long compactionThreshold;
    private final boolean enabled;

    public SnapshotCompactionScheduler(
            PortfolioEventStorePort portfolioEventStore,
            PortfolioLedgerEventFactory eventFactory,
            LedgerWriteUnitOfWorkPort unitOfWork,
            OutboxPort outbox,
            com.ledger.valuation.infrastructure.config.LedgerProperties ledgerProperties
    ) {
        this.portfolioEventStore = portfolioEventStore;
        this.eventFactory = eventFactory;
        this.unitOfWork = unitOfWork;
        this.outbox = outbox;
        this.compactionThreshold = ledgerProperties.snapshot().compactionThreshold();
        this.enabled = ledgerProperties.snapshot().enabled();
    }

    @Scheduled(cron = "${ledger.snapshot.cron:0 30 3 * * SUN}")
    @SchedulerLock(name = "snapshotCompaction", lockAtMostFor = "PT2H", lockAtLeastFor = "PT5M")
    public void compactLongStreams() {
        if (!enabled) {
            return;
        }
        for (UUID portfolioId : portfolioEventStore.listPortfolioIds()) {
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
