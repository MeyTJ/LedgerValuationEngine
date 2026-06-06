package com.ledger.valuation.application.service;

import com.ledger.valuation.application.port.inbound.ApplyMarketTickUseCase;
import com.ledger.valuation.application.port.outbound.InstrumentPositionRegistryPort;
import com.ledger.valuation.domain.NormalizedMarketTick;

import java.util.UUID;

public final class MarketTickValuationService {

    private final InstrumentPositionRegistryPort positionRegistry;
    private final ApplyMarketTickUseCase applyMarketTickUseCase;

    public MarketTickValuationService(
            InstrumentPositionRegistryPort positionRegistry,
            ApplyMarketTickUseCase applyMarketTickUseCase
    ) {
        this.positionRegistry = positionRegistry;
        this.applyMarketTickUseCase = applyMarketTickUseCase;
    }

    public void processTick(NormalizedMarketTick tick) {
        for (UUID portfolioId : positionRegistry.findPortfoliosByInstrument(tick.instrumentId())) {
            applyMarketTickUseCase.apply(portfolioId, tick);
        }
    }
}
