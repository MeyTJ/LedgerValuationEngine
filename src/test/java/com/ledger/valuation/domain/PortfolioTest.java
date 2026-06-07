package com.ledger.valuation.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PortfolioTest {

    @Test
    void rehydratesAccountValueFromTransactionHistory() {
        UUID portfolioId = UUID.randomUUID();
        Instant now = Instant.parse("2026-01-01T00:00:00Z");

        var events = List.<PortfolioLedgerEvent>of(
                new PortfolioLedgerEvent.PortfolioAccountOpened(
                        UUID.randomUUID(), portfolioId, 1L, now, "ACC-1", "USD", "tenant-1",
                        PortfolioStatus.ACTIVE, "open-token"
                ),
                new PortfolioLedgerEvent.TransactionCommitted(
                        UUID.randomUUID(), portfolioId, 2L, now.plusSeconds(1),
                        1_000L, 200L, "tx-1", "token-1"
                )
        );

        Portfolio portfolio = Portfolio.rehydrateFromEventRecords(events);
        assertEquals(800L, portfolio.accountValueMinorUnits());
    }

    @Test
    void rejectsTransactionDrivingAccountValueBelowZero() {
        UUID portfolioId = UUID.randomUUID();
        Instant now = Instant.now();
        var events = List.<PortfolioLedgerEvent>of(
                new PortfolioLedgerEvent.PortfolioAccountOpened(
                        UUID.randomUUID(), portfolioId, 1L, now, "ACC-1", "USD", "tenant-1",
                        PortfolioStatus.ACTIVE, "open-token"
                ),
                new PortfolioLedgerEvent.TransactionCommitted(
                        UUID.randomUUID(), portfolioId, 2L, now.plusSeconds(1),
                        100L, 0L, "fund", "token-1"
                )
        );
        Portfolio portfolio = Portfolio.rehydrateFromEventRecords(events);

        assertThrows(InsufficientFundsException.class, () ->
                portfolio.ensureTransactionCommitPreservesNonNegativeAccountValue(0L, 200L)
        );
    }
}
