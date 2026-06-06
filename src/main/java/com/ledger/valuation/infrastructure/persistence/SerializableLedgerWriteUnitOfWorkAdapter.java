package com.ledger.valuation.infrastructure.persistence;

import com.ledger.valuation.application.port.outbound.LedgerWriteUnitOfWorkPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

@Component
public class SerializableLedgerWriteUnitOfWorkAdapter implements LedgerWriteUnitOfWorkPort {

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public <T> T execute(Supplier<T> operation) {
        return operation.get();
    }
}
