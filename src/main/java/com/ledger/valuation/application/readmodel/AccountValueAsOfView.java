package com.ledger.valuation.application.readmodel;

import java.time.Instant;
import java.util.UUID;

public record AccountValueAsOfView(
        UUID portfolioId,
        String accountCode,
        String currency,
        long accountValueMinorUnits,
        long lastSequenceNumber,
        Instant asOf,
        Instant computedAt,
        int eventsReplayed
) {}
