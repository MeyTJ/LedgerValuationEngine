package com.ledger.valuation.application.service;

import com.ledger.valuation.application.port.outbound.AccountValueProjectionPort;
import com.ledger.valuation.domain.DomainEvent;

import java.util.SequencedCollection;

public final class EventProjectionService {

    private final AccountValueProjectionPort projectionPort;

    public EventProjectionService(AccountValueProjectionPort projectionPort) {
        this.projectionPort = projectionPort;
    }

    public void project(DomainEvent event) {
        switch (event) {
            case DomainEvent.AccountOpened opened -> projectionPort.onAccountOpened(opened);
            case DomainEvent.TransactionPosted posted -> projectionPort.onTransactionPosted(posted);
            case DomainEvent.AccountValued valued -> projectionPort.onAccountValued(valued);
        }
    }

    public void projectStream(SequencedCollection<DomainEvent> events) {
        for (DomainEvent event : events) {
            project(event);
        }
    }
}
