package com.ledger.valuation.application.port.outbound;

import com.ledger.valuation.domain.PortfolioLedgerEvent;
import com.ledger.valuation.domain.PortfolioLedgerEventStream;

import java.util.Optional;
import java.util.UUID;

public interface PortfolioEventStorePort {

    PortfolioLedgerEventStream loadStream(UUID portfolioId);

    Optional<PortfolioLedgerEvent.TransactionCommitted> findTransactionCommittedByIdempotencyToken(String idempotencyToken);

    void append(PortfolioLedgerEvent event);
}
