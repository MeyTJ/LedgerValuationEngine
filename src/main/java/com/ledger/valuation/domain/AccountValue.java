package com.ledger.valuation.domain;

import java.util.Objects;

public record AccountValue(String currency, long accountValueMinorUnits) {

    public AccountValue {
        Objects.requireNonNull(currency, "currency");
        if (currency.isBlank()) {
            throw new IllegalArgumentException("currency must not be blank");
        }
    }

    public static AccountValue zero(String currency) {
        return new AccountValue(currency, 0L);
    }

    public AccountValue applyCredit(long creditMinorUnits) {
        if (creditMinorUnits < 0L) {
            throw new IllegalArgumentException("creditMinorUnits must be non-negative");
        }
        return new AccountValue(currency, accountValueMinorUnits + creditMinorUnits);
    }

    public AccountValue applyDebit(long debitMinorUnits) {
        if (debitMinorUnits < 0L) {
            throw new IllegalArgumentException("debitMinorUnits must be non-negative");
        }
        return new AccountValue(currency, accountValueMinorUnits - debitMinorUnits);
    }

    public AccountValue applyAccountValueDelta(long deltaMinorUnits) {
        return new AccountValue(currency, accountValueMinorUnits + deltaMinorUnits);
    }
}
