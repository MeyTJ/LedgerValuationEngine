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
        return new AccountValue(currency, Math.addExact(accountValueMinorUnits, creditMinorUnits));
    }

    public AccountValue applyDebit(long debitMinorUnits) {
        if (debitMinorUnits < 0L) {
            throw new IllegalArgumentException("debitMinorUnits must be non-negative");
        }
        return new AccountValue(currency, Math.subtractExact(accountValueMinorUnits, debitMinorUnits));
    }

    public AccountValue applyAccountValueDelta(long deltaMinorUnits) {
        return new AccountValue(currency, Math.addExact(accountValueMinorUnits, deltaMinorUnits));
    }

    public Money toMoney() {
        return new Money(currency, accountValueMinorUnits);
    }

    public static AccountValue fromMoney(Money money) {
        return new AccountValue(money.currency(), money.minorUnits());
    }
}
