package com.ledger.valuation.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoneyTest {

    @Test
    void addsAndSubtractsWithOverflowProtection() {
        Money balance = Money.zero("USD");
        balance = balance.plusMinorUnits(100L);
        balance = balance.minusMinorUnits(30L);
        assertEquals(70L, balance.minorUnits());
    }

    @Test
    void rejectsCurrencyMismatch() {
        assertThrows(IllegalArgumentException.class, () ->
                Money.zero("USD").plus(Money.zero("EUR"))
        );
    }
}
