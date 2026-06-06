package com.ledger.valuation.application.port.inbound;

import java.util.UUID;

public interface RebuildPortfolioReadSideUseCase {

    void rebuildPortfolio(UUID portfolioId);

    void rebuildAll();
}
