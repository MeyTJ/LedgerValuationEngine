package com.ledger.valuation.application.port.outbound;

import com.ledger.valuation.domain.DomainEvent;
import com.ledger.valuation.domain.EventStream;

import java.util.UUID;

public interface EventStorePort {

    void append(DomainEvent event);

    EventStream loadStream(UUID aggregateId);
}
