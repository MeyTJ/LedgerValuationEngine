package com.ledger.valuation.infrastructure.readside;

import com.ledger.valuation.domain.PortfolioLedgerEvent;
import org.springframework.context.ApplicationEvent;

public final class LedgerEventCommittedSpringEvent extends ApplicationEvent {

    private final PortfolioLedgerEvent eventRecord;

    public LedgerEventCommittedSpringEvent(Object source, PortfolioLedgerEvent eventRecord) {
        super(source);
        this.eventRecord = eventRecord;
    }

    public PortfolioLedgerEvent eventRecord() {
        return eventRecord;
    }
}
