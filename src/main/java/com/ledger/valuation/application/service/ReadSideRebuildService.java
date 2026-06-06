package com.ledger.valuation.application.service;

import com.ledger.valuation.application.port.inbound.RebuildReadSideUseCase;
import com.ledger.valuation.application.port.outbound.EventStorePort;

import java.util.UUID;

public final class ReadSideRebuildService implements RebuildReadSideUseCase {

    private final EventStorePort eventStore;
    private final EventProjectionService projectionService;

    public ReadSideRebuildService(EventStorePort eventStore, EventProjectionService projectionService) {
        this.eventStore = eventStore;
        this.projectionService = projectionService;
    }

    @Override
    public void rebuild(UUID aggregateId) {
        projectionService.projectStream(eventStore.loadStream(aggregateId).events());
    }
}
