package com.ledger.valuation.domain;

import java.time.Instant;

public record NormalizedMarketTick(
        String instrumentId,
        String quoteCurrency,
        long priceMinorUnits,
        Instant tickTimestamp,
        String valuationRunId
) {}
