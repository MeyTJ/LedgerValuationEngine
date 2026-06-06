package com.ledger.valuation.infrastructure.readside;

import com.ledger.valuation.application.port.outbound.InstrumentPositionRegistryPort;
import com.ledger.valuation.domain.InstrumentPosition;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryInstrumentPositionRegistry implements InstrumentPositionRegistryPort {

    private final Map<UUID, Map<String, InstrumentPosition>> positionsByPortfolio = new ConcurrentHashMap<>();
    private final Map<String, Set<UUID>> portfoliosByInstrument = new ConcurrentHashMap<>();

    @Override
    public void register(UUID portfolioId, InstrumentPosition position) {
        positionsByPortfolio
                .computeIfAbsent(portfolioId, _ -> new ConcurrentHashMap<>())
                .put(position.instrumentId(), position);
        portfoliosByInstrument
                .computeIfAbsent(position.instrumentId(), _ -> ConcurrentHashMap.newKeySet())
                .add(portfolioId);
    }

    @Override
    public Optional<InstrumentPosition> find(UUID portfolioId, String instrumentId) {
        Map<String, InstrumentPosition> positions = positionsByPortfolio.get(portfolioId);
        if (positions == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(positions.get(instrumentId));
    }

    @Override
    public void updateMark(UUID portfolioId, String instrumentId, long markPriceMinorUnits) {
        Map<String, InstrumentPosition> positions = positionsByPortfolio.get(portfolioId);
        if (positions == null) {
            return;
        }
        InstrumentPosition current = positions.get(instrumentId);
        if (current != null) {
            positions.put(instrumentId, new InstrumentPosition(
                    instrumentId,
                    current.quantityMinorUnits(),
                    current.costBasisMinorUnits(),
                    markPriceMinorUnits
            ));
        }
    }

    @Override
    public Set<UUID> findPortfoliosByInstrument(String instrumentId) {
        return portfoliosByInstrument.getOrDefault(instrumentId, Set.of());
    }
}
