package com.ledger.valuation.application.port.inbound;

import com.ledger.valuation.domain.OpenPortfolioCommand;

import java.util.UUID;

public interface OpenPortfolioUseCase {

    UUID handle(OpenPortfolioCommand command);
}
