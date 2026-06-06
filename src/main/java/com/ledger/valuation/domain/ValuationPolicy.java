package com.ledger.valuation.domain;

public final class ValuationPolicy {

    private ValuationPolicy() {}

    public static long calculateMarkToMarketDeltaMinorUnits(
            long currentPositionMinorUnits,
            long previousPriceMinorUnits,
            long currentPriceMinorUnits
    ) {
        if (previousPriceMinorUnits == 0L) {
            return 0L;
        }
        return (currentPositionMinorUnits * (currentPriceMinorUnits - previousPriceMinorUnits)) / previousPriceMinorUnits;
    }
}
