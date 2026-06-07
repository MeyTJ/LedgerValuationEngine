package com.ledger.valuation.infrastructure.health;

import com.ledger.valuation.application.port.outbound.OutboxPort;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class OutboxHealthIndicator implements HealthIndicator {

    private static final long PENDING_THRESHOLD = 1000L;

    private final OutboxPort outboxPort;

    public OutboxHealthIndicator(OutboxPort outboxPort) {
        this.outboxPort = outboxPort;
    }

    @Override
    public Health health() {
        long pending = outboxPort.countPending();
        if (pending > PENDING_THRESHOLD) {
            return Health.down()
                    .withDetail("pendingCount", pending)
                    .withDetail("reason", "Outbox backlog exceeds threshold")
                    .build();
        }
        return Health.up().withDetail("pendingCount", pending).build();
    }
}
