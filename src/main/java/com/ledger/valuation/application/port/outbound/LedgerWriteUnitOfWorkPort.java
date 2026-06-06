package com.ledger.valuation.application.port.outbound;

import java.util.function.Supplier;

public interface LedgerWriteUnitOfWorkPort {

    <T> T execute(Supplier<T> operation);
}
