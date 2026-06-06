package com.ledger.valuation.domain;

public record InstrumentPosition(
        String instrumentId,
        long quantityMinorUnits,
        long costBasisMinorUnits,
        long lastMarkPriceMinorUnits
) {}
