package com.ledger.valuation.infrastructure.persistence.eventstore;

public final class ImmutableLedgerViolationException extends RuntimeException {

    public ImmutableLedgerViolationException(String message) {
        super(message);
    }

    public ImmutableLedgerViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}
