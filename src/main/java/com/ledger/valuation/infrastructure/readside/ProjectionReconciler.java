package com.ledger.valuation.infrastructure.readside;

import com.ledger.valuation.application.port.outbound.AccountValueReadModelPort;
import com.ledger.valuation.application.port.outbound.PortfolioEventStorePort;
import com.ledger.valuation.application.service.PortfolioLedgerEventProjectionService;
import com.ledger.valuation.domain.Portfolio;
import io.micrometer.core.instrument.MeterRegistry;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProjectionReconciler {

    private static final Logger log = LoggerFactory.getLogger(ProjectionReconciler.class);

    private final PortfolioEventStorePort eventStore;
    private final AccountValueReadModelPort readModel;
    private final PortfolioLedgerEventProjectionService projectionService;
    private final MeterRegistry meterRegistry;

    public ProjectionReconciler(
            PortfolioEventStorePort eventStore,
            AccountValueReadModelPort readModel,
            PortfolioLedgerEventProjectionService projectionService,
            MeterRegistry meterRegistry
    ) {
        this.eventStore = eventStore;
        this.readModel = readModel;
        this.projectionService = projectionService;
        this.meterRegistry = meterRegistry;
    }

    @Scheduled(fixedDelayString = "${ledger.reconciliation.interval-ms:30000}")
    @SchedulerLock(name = "projectionReconciler", lockAtMostFor = "PT5M", lockAtLeastFor = "PT10S")
    public void reconcile() {
        for (var view : readModel.findAll()) {
            UUID portfolioId = view.portfolioId();
            var stream = eventStore.loadStream(portfolioId);
            if (stream.isEmpty()) {
                continue;
            }
            Portfolio portfolio = Portfolio.rehydrateFromEventRecords(stream.eventRecords());
            long expected = portfolio.accountValueMinorUnits();
            long actual = view.accountValueMinorUnits();
            if (expected != actual) {
                meterRegistry.counter("ledger.projection.drift_detected").increment();
                log.warn("Projection drift portfolioId={} expected={} actual={}", portfolioId, expected, actual);
                for (var event : stream.eventRecords()) {
                    projectionService.project(event);
                }
            }
        }
    }
}
