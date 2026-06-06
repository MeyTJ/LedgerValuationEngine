package com.ledger.valuation.application.service;

import com.ledger.valuation.application.port.inbound.OpenPortfolioUseCase;
import com.ledger.valuation.application.port.outbound.LedgerWriteUnitOfWorkPort;
import com.ledger.valuation.application.port.outbound.OutboxPort;
import com.ledger.valuation.application.port.outbound.PortfolioEventStorePort;
import com.ledger.valuation.domain.OpenPortfolioCommand;
import com.ledger.valuation.domain.PortfolioLedgerEvent;
import com.ledger.valuation.domain.PortfolioLedgerEventFactory;

import java.util.UUID;

public final class OpenPortfolioCommandHandler implements OpenPortfolioUseCase {

    private final LedgerWriteUnitOfWorkPort unitOfWork;
    private final PortfolioEventStorePort eventStore;
    private final PortfolioLedgerEventFactory eventFactory;
    private final OutboxPort outbox;

    public OpenPortfolioCommandHandler(
            LedgerWriteUnitOfWorkPort unitOfWork,
            PortfolioEventStorePort eventStore,
            PortfolioLedgerEventFactory eventFactory,
            OutboxPort outbox
    ) {
        this.unitOfWork = unitOfWork;
        this.eventStore = eventStore;
        this.eventFactory = eventFactory;
        this.outbox = outbox;
    }

    @Override
    public UUID handle(OpenPortfolioCommand command) {
        return unitOfWork.execute(() -> {
            if (!eventStore.loadStream(command.portfolioId()).isEmpty()) {
                throw new IllegalStateException("Portfolio already exists: " + command.portfolioId());
            }
            PortfolioLedgerEvent event = eventFactory.createPortfolioOpened(command);
            eventStore.append(event);
            outbox.enqueue(event);
            return event.eventId();
        });
    }
}
