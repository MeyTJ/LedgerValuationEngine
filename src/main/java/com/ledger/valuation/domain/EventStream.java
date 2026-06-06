package com.ledger.valuation.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.SequencedCollection;

public final class EventStream {

    private final ArrayList<DomainEvent> events;

    public EventStream(Collection<DomainEvent> events) {
        this.events = new ArrayList<>(events);
    }

    public SequencedCollection<DomainEvent> events() {
        return events;
    }

    public DomainEvent first() {
        return events.getFirst();
    }

    public DomainEvent last() {
        return events.getLast();
    }

    public SequencedCollection<DomainEvent> reversed() {
        return events.reversed();
    }

    public int size() {
        return events.size();
    }
}
