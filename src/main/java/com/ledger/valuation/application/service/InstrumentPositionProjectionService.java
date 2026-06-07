package com.ledger.valuation.application.service;

import com.ledger.valuation.application.port.outbound.InstrumentPositionRegistryPort;
import com.ledger.valuation.domain.InstrumentPosition;
import com.ledger.valuation.domain.PortfolioLedgerEvent;

public final class InstrumentPositionProjectionService {

    private final InstrumentPositionRegistryPort positionRegistry;

    public InstrumentPositionProjectionService(InstrumentPositionRegistryPort positionRegistry) {
        this.positionRegistry = positionRegistry;
    }

    public void project(PortfolioLedgerEvent event) {
        if (event instanceof PortfolioLedgerEvent.PositionOpened opened) {
            positionRegistry.register(opened.portfolioId(), new InstrumentPosition(
                    opened.instrumentId(),
                    opened.quantityMinorUnits(),
                    opened.costBasisMinorUnits(),
                    opened.costBasisMinorUnits()
            ));
        } else if (event instanceof PortfolioLedgerEvent.MarkToMarketApplied mark) {
            positionRegistry.updateMark(mark.portfolioId(), mark.instrumentId(), mark.markPriceMinorUnits());
        }
    }
}
