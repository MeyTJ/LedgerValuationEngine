package com.ledger.valuation.infrastructure.readside;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.valuation.application.port.outbound.LedgerEventCommittedPublisherPort;
import com.ledger.valuation.application.port.outbound.OutboxPort;
import com.ledger.valuation.domain.PortfolioLedgerEvent;
import io.micrometer.core.instrument.MeterRegistry;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

@Component
public class OutboxRelayScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelayScheduler.class);

    private final OutboxPort outbox;
    private final LedgerEventCommittedPublisherPort publisher;
    private final ObjectMapper objectMapper;
    private final int batchSize;
    private final String instanceId;

    public OutboxRelayScheduler(
            OutboxPort outbox,
            LedgerEventCommittedPublisherPort publisher,
            ObjectMapper objectMapper,
            com.ledger.valuation.infrastructure.config.LedgerProperties ledgerProperties,
            MeterRegistry meterRegistry
    ) {
        this.outbox = outbox;
        this.publisher = publisher;
        this.objectMapper = objectMapper;
        this.batchSize = ledgerProperties.outbox().batchSize();
        this.instanceId = resolveInstanceId();
        meterRegistry.gauge("ledger.outbox.pending", outbox, OutboxPort::countPending);
    }

    @Scheduled(fixedDelayString = "${ledger.outbox.relay-interval-ms:500}")
    @SchedulerLock(name = "outboxRelay", lockAtMostFor = "PT30S", lockAtLeastFor = "PT1S")
    public void relayPendingEvents() {
        for (OutboxPort.OutboxEntry entry : outbox.claimPending(batchSize, instanceId)) {
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

    private static String resolveInstanceId() {
        try {
            return InetAddress.getLocalHost().getHostName() + "-" + UUID.randomUUID();
        } catch (UnknownHostException ex) {
            return "unknown-" + UUID.randomUUID();
        }
    }
}
