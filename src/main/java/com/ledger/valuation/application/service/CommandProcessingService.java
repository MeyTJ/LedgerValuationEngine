package com.ledger.valuation.application.service;

import com.ledger.valuation.application.port.inbound.ProcessCommandUseCase;
import com.ledger.valuation.application.port.outbound.EventStorePort;
import com.ledger.valuation.domain.Command;
import com.ledger.valuation.domain.DomainEvent;
import com.ledger.valuation.domain.LedgerEventFactory;

public final class CommandProcessingService implements ProcessCommandUseCase {

    private final LedgerEventFactory eventFactory;
    private final EventStorePort eventStore;
    private final EventProjectionService projectionService;

    public CommandProcessingService(
            LedgerEventFactory eventFactory,
            EventStorePort eventStore,
            EventProjectionService projectionService
    ) {
        this.eventFactory = eventFactory;
        this.eventStore = eventStore;
        this.projectionService = projectionService;
    }

    @Override
    public void process(Command command) {
        DomainEvent event = eventFactory.createFrom(command);
        eventStore.append(event);
        projectionService.project(event);
    }
}
