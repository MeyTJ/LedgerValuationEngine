package com.ledger.valuation.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.valuation.application.port.outbound.EventStorePort;
import com.ledger.valuation.domain.DomainEvent;
import com.ledger.valuation.domain.EventStream;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class CockroachEventStoreAdapter implements EventStorePort {

    private final EventStoreRepository repository;
    private final ObjectMapper objectMapper;

    public CockroachEventStoreAdapter(EventStoreRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void append(DomainEvent event) {
        if (repository.existsByAggregateIdAndSequenceNumber(event.aggregateId(), event.sequenceNumber())) {
            throw new IllegalStateException("Duplicate sequence for aggregate " + event.aggregateId());
        }
        repository.save(toRecord(event));
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
    public EventStream loadStream(UUID aggregateId) {
        List<EventStoreRecord> records = repository.findByAggregateIdOrderBySequenceNumberAsc(aggregateId);
        var events = new ArrayList<DomainEvent>(records.size());
        for (EventStoreRecord record : records) {
            events.add(deserialize(record));
        }
        return new EventStream(events);
    }

    private DomainEvent deserialize(EventStoreRecord record) {
        try {
            return objectMapper.readValue(record.getPayload(), DomainEvent.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to deserialize event " + record.getId(), ex);
        }
    }

    private EventStoreRecord toRecord(DomainEvent event) {
        try {
            return new EventStoreRecord(
                    event.eventId(),
                    event.aggregateId(),
                    event.sequenceNumber(),
                    event.getClass().getSimpleName(),
                    objectMapper.writeValueAsString(event),
                    event.occurredAt()
            );
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Unable to serialize event " + event.eventId(), ex);
        }
    }
}
