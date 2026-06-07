package com.ledger.valuation.domain;

import java.util.Objects;

public record Money(String currency, long minorUnits) {

    public Money {
        Objects.requireNonNull(currency, "currency");
        if (currency.isBlank()) {
            throw new IllegalArgumentException("currency must not be blank");
        }
    }

    public static Money zero(String currency) {
        return new Money(currency, 0L);
    }

    public Money plus(Money other) {
        assertSameCurrency(other);
        return new Money(currency, Math.addExact(minorUnits, other.minorUnits));
    }

    public Money minus(Money other) {
        assertSameCurrency(other);
        return new Money(currency, Math.subtractExact(minorUnits, other.minorUnits));
    }

    public Money plusMinorUnits(long amount) {
        return new Money(currency, Math.addExact(minorUnits, amount));
    }

    public Money minusMinorUnits(long amount) {
        return new Money(currency, Math.subtractExact(minorUnits, amount));
    }

    private void assertSameCurrency(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Currency mismatch: " + currency + " vs " + other.currency
            );
        }
    }
}
