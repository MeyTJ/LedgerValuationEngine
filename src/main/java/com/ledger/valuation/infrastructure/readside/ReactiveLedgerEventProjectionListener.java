package com.ledger.valuation.infrastructure.readside;

import com.ledger.valuation.application.port.outbound.AccountValueReadModelPort;
import com.ledger.valuation.application.port.outbound.AccountValueStreamPort;
import com.ledger.valuation.application.service.InstrumentPositionProjectionService;
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
    private final InstrumentPositionProjectionService positionProjectionService;
    private final AccountValueReadModelPort readModel;
    private final AccountValueStreamPort streamPort;

    public ReactiveLedgerEventProjectionListener(
            PortfolioLedgerEventProjectionService projectionService,
            InstrumentPositionProjectionService positionProjectionService,
            AccountValueReadModelPort readModel,
            AccountValueStreamPort streamPort
    ) {
        this.projectionService = projectionService;
        this.positionProjectionService = positionProjectionService;
        this.readModel = readModel;
        this.streamPort = streamPort;
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
        positionProjectionService.project(eventRecord);
        readModel.findByPortfolioId(eventRecord.portfolioId()).ifPresent(streamPort::publish);
    }
}
