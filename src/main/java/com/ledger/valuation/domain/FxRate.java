package com.ledger.valuation.domain;

public record FxRate(String baseCurrency, String quoteCurrency, long rateMinorUnits, int scale) {

    public long convertToBase(long quoteMinorUnits) {
        return (quoteMinorUnits * rateMinorUnits) / (long) Math.pow(10, scale);
    }
}
