package com.ledger.valuation.application.port.outbound;

import com.ledger.valuation.domain.InstrumentPosition;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface InstrumentPositionRegistryPort {

    void register(UUID portfolioId, InstrumentPosition position);

    Optional<InstrumentPosition> find(UUID portfolioId, String instrumentId);

    void updateMark(UUID portfolioId, String instrumentId, long markPriceMinorUnits);

    Set<UUID> findPortfoliosByInstrument(String instrumentId);
}
