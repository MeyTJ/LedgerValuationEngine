package com.ledger.valuation.infrastructure.readside;

import com.ledger.valuation.application.port.outbound.AccountValueReadModelPort;
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
    private final ReadModelFederationWriter federationWriter;
    private final AccountValueReadModelPort readModel;

    public ReactiveLedgerEventProjectionListener(
            PortfolioLedgerEventProjectionService projectionService,
            ReadModelFederationWriter federationWriter,
            AccountValueReadModelPort readModel
    ) {
        this.projectionService = projectionService;
        this.federationWriter = federationWriter;
        this.readModel = readModel;
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
        readModel.findByPortfolioId(eventRecord.portfolioId()).ifPresent(federationWriter::federate);
    }
}
