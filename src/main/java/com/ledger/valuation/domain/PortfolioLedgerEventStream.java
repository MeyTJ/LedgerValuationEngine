package com.ledger.valuation.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.SequencedCollection;

public final class PortfolioLedgerEventStream {

    private final ArrayList<PortfolioLedgerEvent> eventRecords;

    public PortfolioLedgerEventStream(Collection<PortfolioLedgerEvent> eventRecords) {
        this.eventRecords = new ArrayList<>(eventRecords);
    }

    public SequencedCollection<PortfolioLedgerEvent> eventRecords() {
        return eventRecords;
    }

    public boolean isEmpty() {
        return eventRecords.isEmpty();
    }

    public int size() {
        return eventRecords.size();
    }
}
