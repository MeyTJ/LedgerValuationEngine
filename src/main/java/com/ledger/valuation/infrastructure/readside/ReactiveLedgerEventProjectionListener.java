package com.ledger.valuation.infrastructure.readside;

import com.ledger.valuation.application.service.PortfolioLedgerEventProjectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ReactiveLedgerEventProjectionListener {

    private static final Logger log = LoggerFactory.getLogger(ReactiveLedgerEventProjectionListener.class);

    private final PortfolioLedgerEventProjectionService projectionService;

    public ReactiveLedgerEventProjectionListener(PortfolioLedgerEventProjectionService projectionService) {
        this.projectionService = projectionService;
    }

    @Async("ledgerProjectionExecutor")
    @EventListener
    public void onLedgerEventCommitted(LedgerEventCommittedSpringEvent event) {
        var eventRecord = event.eventRecord();
        log.debug(
                "Projecting committed ledger event type={} portfolioId={} sequence={}",
                eventRecord.getClass().getSimpleName(),
                eventRecord.portfolioId(),
                eventRecord.sequenceNumber()
        );
        projectionService.project(eventRecord);
    }
}
