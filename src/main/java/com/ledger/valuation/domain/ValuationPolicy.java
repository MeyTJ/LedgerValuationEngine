package com.ledger.valuation.domain;

public final class ValuationPolicy {

    private ValuationPolicy() {}

    public static long calculateMarkToMarketDeltaMinorUnits(
            InstrumentPosition position,
            long currentPriceMinorUnits
    ) {
        if (position.lastMarkPriceMinorUnits() == 0L) {
            return 0L;
        }
        long priceDelta = currentPriceMinorUnits - position.lastMarkPriceMinorUnits();
        return (position.quantityMinorUnits() * priceDelta) / position.lastMarkPriceMinorUnits();
    }

    public static InstrumentPosition withUpdatedMark(InstrumentPosition position, long currentPriceMinorUnits) {
        return new InstrumentPosition(
                position.instrumentId(),
                position.quantityMinorUnits(),
                position.costBasisMinorUnits(),
                currentPriceMinorUnits
        );
    }
}
