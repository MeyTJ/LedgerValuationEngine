package com.ledger.valuation.infrastructure.readside;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.valuation.application.port.outbound.LedgerEventCommittedPublisherPort;
import com.ledger.valuation.application.port.outbound.OutboxPort;
import com.ledger.valuation.domain.PortfolioLedgerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxRelayScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelayScheduler.class);

    private final OutboxPort outbox;
    private final LedgerEventCommittedPublisherPort publisher;
    private final ObjectMapper objectMapper;
    private final int batchSize;

    public OutboxRelayScheduler(
            OutboxPort outbox,
            LedgerEventCommittedPublisherPort publisher,
            ObjectMapper objectMapper,
            @Value("${ledger.outbox.batch-size:100}") int batchSize
    ) {
        this.outbox = outbox;
        this.publisher = publisher;
        this.objectMapper = objectMapper;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${ledger.outbox.relay-interval-ms:500}")
    public void relayPendingEvents() {
        for (OutboxPort.OutboxEntry entry : outbox.fetchPending(batchSize)) {
            try {
                PortfolioLedgerEvent event = objectMapper.readValue(entry.payload(), PortfolioLedgerEvent.class);
                publisher.publish(event);
                outbox.markPublished(entry.id());
            } catch (Exception ex) {
                log.error("Outbox relay failed for entry {}", entry.id(), ex);
                outbox.incrementRetry(entry.id());
            }
        }
    }
}
