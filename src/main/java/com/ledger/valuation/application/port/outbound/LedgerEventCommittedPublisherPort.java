package com.ledger.valuation.application.port.outbound;

import com.ledger.valuation.domain.PortfolioLedgerEvent;

public interface LedgerEventCommittedPublisherPort {

    void publish(PortfolioLedgerEvent event);
}
