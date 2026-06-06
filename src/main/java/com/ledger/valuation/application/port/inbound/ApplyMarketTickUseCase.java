package com.ledger.valuation.application.port.inbound;

import com.ledger.valuation.domain.NormalizedMarketTick;

import java.util.UUID;

public interface ApplyMarketTickUseCase {

    void apply(UUID portfolioId, NormalizedMarketTick tick);
}
