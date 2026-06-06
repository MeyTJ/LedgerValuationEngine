package com.ledger.valuation.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.valuation.application.port.outbound.PortfolioEventStorePort;
import com.ledger.valuation.domain.PortfolioLedgerEvent;
import com.ledger.valuation.domain.PortfolioLedgerEventStream;
import com.ledger.valuation.infrastructure.persistence.eventstore.EventStore;
import com.ledger.valuation.infrastructure.persistence.eventstore.StoredEventRecord;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class CockroachPortfolioEventStoreAdapter implements PortfolioEventStorePort {

    private final EventStore eventStore;
    private final ObjectMapper objectMapper;

    public CockroachPortfolioEventStoreAdapter(EventStore eventStore, ObjectMapper objectMapper) {
        this.eventStore = eventStore;
        this.objectMapper = objectMapper;
    }

    @Override
    public PortfolioLedgerEventStream loadStream(UUID portfolioId) {
        List<StoredEventRecord> records = eventStore.loadByAggregateId(portfolioId);
        var eventRecords = new ArrayList<PortfolioLedgerEvent>(records.size());
        for (StoredEventRecord record : records) {
            eventRecords.add(deserialize(record));
        }
        return new PortfolioLedgerEventStream(eventRecords);
    }

    @Override
    public Optional<PortfolioLedgerEvent.TransactionCommitted> findTransactionCommittedByIdempotencyToken(
            String idempotencyToken
    ) {
        return eventStore.findByIdempotencyToken(idempotencyToken)
                .map(this::deserialize)
                .flatMap(this::asTransactionCommitted);
    }

    @Override
    public void append(PortfolioLedgerEvent event) {
        if (eventStore.existsByAggregateIdAndSequenceNumber(event.portfolioId(), event.sequenceNumber())) {
            throw new IllegalStateException("Duplicate sequence for portfolio " + event.portfolioId());
        }
        eventStore.append(toStoredRecord(event));
    }

    private Optional<PortfolioLedgerEvent.TransactionCommitted> asTransactionCommitted(PortfolioLedgerEvent event) {
        if (event instanceof PortfolioLedgerEvent.TransactionCommitted committed) {
            return Optional.of(committed);
        }
        throw new IllegalStateException(
                "idempotencyToken is bound to non-transaction event type: " + event.getClass().getSimpleName()
        );
    }

    private PortfolioLedgerEvent deserialize(StoredEventRecord record) {
        try {
            return objectMapper.readValue(record.payload(), PortfolioLedgerEvent.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to deserialize portfolio event " + record.id(), ex);
        }
    }

    private StoredEventRecord toStoredRecord(PortfolioLedgerEvent event) {
        try {
            String idempotencyToken = event instanceof PortfolioLedgerEvent.TransactionCommitted committed
                    ? committed.idempotencyToken()
                    : null;
            return new StoredEventRecord(
                    event.eventId(),
                    event.portfolioId(),
                    event.sequenceNumber(),
                    event.getClass().getSimpleName(),
                    objectMapper.writeValueAsString(event),
                    event.occurredAt(),
                    idempotencyToken
            );
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Unable to serialize portfolio event " + event.eventId(), ex);
        }
    }
}
