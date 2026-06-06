package com.ledger.valuation.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.valuation.application.port.outbound.EventStorePort;
import com.ledger.valuation.domain.DomainEvent;
import com.ledger.valuation.domain.EventStream;
import com.ledger.valuation.infrastructure.persistence.eventstore.EventStore;
import com.ledger.valuation.infrastructure.persistence.eventstore.StoredEventRecord;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class CockroachEventStoreAdapter implements EventStorePort {

    private final EventStore eventStore;
    private final ObjectMapper objectMapper;

    public CockroachEventStoreAdapter(EventStore eventStore, ObjectMapper objectMapper) {
        this.eventStore = eventStore;
        this.objectMapper = objectMapper;
    }

    @Override
    public void append(DomainEvent event) {
        if (eventStore.existsByAggregateIdAndSequenceNumber(event.aggregateId(), event.sequenceNumber())) {
            throw new IllegalStateException("Duplicate sequence for aggregate " + event.aggregateId());
        }
        eventStore.append(toStoredRecord(event));
    }

    @Override
    public List<UUID> listAggregateIds() {
        return eventStore.listAggregateIds();
    }

    @Override
    public EventStream loadStream(UUID aggregateId) {
        List<StoredEventRecord> records = eventStore.loadByAggregateId(aggregateId);
        var events = new ArrayList<DomainEvent>(records.size());
        for (StoredEventRecord record : records) {
            events.add(deserialize(record));
        }
        return new EventStream(events);
    }

    private DomainEvent deserialize(StoredEventRecord record) {
        try {
            return objectMapper.readValue(record.payload(), DomainEvent.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to deserialize event " + record.id(), ex);
        }
    }

    private StoredEventRecord toStoredRecord(DomainEvent event) {
        try {
            return new StoredEventRecord(
                    event.eventId(),
                    event.aggregateId(),
                    event.sequenceNumber(),
                    event.getClass().getSimpleName(),
                    objectMapper.writeValueAsString(event),
                    event.occurredAt(),
                    null
            );
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Unable to serialize event " + event.eventId(), ex);
        }
    }
}
