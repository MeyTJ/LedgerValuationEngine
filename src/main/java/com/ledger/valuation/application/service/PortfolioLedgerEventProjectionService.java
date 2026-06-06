package com.ledger.valuation.application.service;

import com.ledger.valuation.application.port.outbound.AccountValueReadModelPort;
import com.ledger.valuation.domain.PortfolioLedgerEvent;

public final class PortfolioLedgerEventProjectionService {

    private final AccountValueReadModelPort readModel;

    public PortfolioLedgerEventProjectionService(AccountValueReadModelPort readModel) {
        this.readModel = readModel;
    }

    public void project(PortfolioLedgerEvent event) {
        readModel.project(event);
    }
}
