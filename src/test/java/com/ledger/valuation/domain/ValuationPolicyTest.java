package com.ledger.valuation.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ValuationPolicyTest {

    @Test
    void calculatesMarkToMarketDeltaFromPositionAndPriceChange() {
        InstrumentPosition position = new InstrumentPosition("AAPL", 1000L, 100_00L, 100_00L);
        long delta = ValuationPolicy.calculateMarkToMarketDeltaMinorUnits(position, 110_00L);
        assertEquals(100L, delta);
    }

    @Test
    void returnsZeroWhenNoPriorMarkPrice() {
        InstrumentPosition position = new InstrumentPosition("AAPL", 1000L, 100_00L, 0L);
        long delta = ValuationPolicy.calculateMarkToMarketDeltaMinorUnits(position, 110_00L);
        assertEquals(0L, delta);
    }
}
