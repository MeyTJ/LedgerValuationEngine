package com.ledger.valuation.infrastructure.readside;

import com.ledger.valuation.application.port.outbound.LedgerEventCommittedPublisherPort;
import com.ledger.valuation.domain.PortfolioLedgerEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringLedgerEventCommittedPublisher implements LedgerEventCommittedPublisherPort {

    private final ApplicationEventPublisher eventPublisher;

    public SpringLedgerEventCommittedPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void publish(PortfolioLedgerEvent event) {
        eventPublisher.publishEvent(new LedgerEventCommittedSpringEvent(this, event));
    }
}
