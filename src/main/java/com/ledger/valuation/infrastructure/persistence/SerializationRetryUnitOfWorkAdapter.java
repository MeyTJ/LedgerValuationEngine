package com.ledger.valuation.infrastructure.persistence;

import com.ledger.valuation.application.port.outbound.LedgerWriteUnitOfWorkPort;
import com.ledger.valuation.infrastructure.persistence.eventstore.CockroachSerializationConflictException;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Primary
@Component
public class SerializationRetryUnitOfWorkAdapter implements LedgerWriteUnitOfWorkPort {

    private final SerializableLedgerWriteUnitOfWorkAdapter delegate;
    private final MeterRegistry meterRegistry;
    private final int maxAttempts;
    private final long baseBackoffMs;

    public SerializationRetryUnitOfWorkAdapter(
            SerializableLedgerWriteUnitOfWorkAdapter delegate,
            MeterRegistry meterRegistry,
            @Value("${ledger.cockroach.retry.max-attempts:5}") int maxAttempts,
            @Value("${ledger.cockroach.retry.base-backoff-ms:25}") long baseBackoffMs
    ) {
        this.delegate = delegate;
        this.meterRegistry = meterRegistry;
        this.maxAttempts = maxAttempts;
        this.baseBackoffMs = baseBackoffMs;
    }

    @Override
    public <T> T execute(Supplier<T> operation) {
        int attempt = 0;
        while (true) {
            attempt++;
            try {
                return delegate.execute(operation);
            } catch (CockroachSerializationConflictException ex) {
                meterRegistry.counter("ledger.cockroach.serialization_retries").increment();
                if (attempt >= maxAttempts) {
                    throw ex;
                }
                sleepWithJitter(attempt);
            }
        }
    }

    private void sleepWithJitter(int attempt) {
        long backoff = baseBackoffMs * (1L << (attempt - 1));
        long jitter = (long) (Math.random() * baseBackoffMs);
        try {
            Thread.sleep(backoff + jitter);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted during serialization retry", ex);
        }
    }
}
