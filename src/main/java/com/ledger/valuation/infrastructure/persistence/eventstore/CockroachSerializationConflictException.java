package com.ledger.valuation.infrastructure.persistence.eventstore;

import java.sql.SQLException;

public final class CockroachSerializationConflictException extends RuntimeException {

    public CockroachSerializationConflictException(String message, SQLException cause) {
        super(message, cause);
    }
}
