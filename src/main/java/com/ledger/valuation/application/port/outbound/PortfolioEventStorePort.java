package com.ledger.valuation.application.port.outbound;

import com.ledger.valuation.domain.PortfolioLedgerEvent;
import com.ledger.valuation.domain.PortfolioLedgerEventStream;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PortfolioEventStorePort {

    PortfolioLedgerEventStream loadStream(UUID portfolioId);

    Optional<PortfolioLedgerEvent.TransactionCommitted> findTransactionCommittedByIdempotencyToken(
            String idempotencyToken
    );

    Optional<PortfolioLedgerEvent> findEventByIdempotencyToken(String idempotencyToken);

    List<UUID> listPortfolioIds();

    void append(PortfolioLedgerEvent event);
}
