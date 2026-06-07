package com.ledger.valuation.application.service;

import com.ledger.valuation.application.port.inbound.OpenPortfolioUseCase;
import com.ledger.valuation.application.port.outbound.CommandIdempotencyPort;
import com.ledger.valuation.application.port.outbound.LedgerWriteUnitOfWorkPort;
import com.ledger.valuation.application.port.outbound.OutboxPort;
import com.ledger.valuation.application.port.outbound.PortfolioEventStorePort;
import com.ledger.valuation.domain.OpenPortfolioCommand;
import com.ledger.valuation.domain.PortfolioLedgerEvent;
import com.ledger.valuation.domain.PortfolioLedgerEventFactory;

import java.util.UUID;

public final class OpenPortfolioCommandHandler implements OpenPortfolioUseCase {

    private static final String COMMAND_TYPE = "OpenPortfolio";

    private final LedgerWriteUnitOfWorkPort unitOfWork;
    private final PortfolioEventStorePort eventStore;
    private final PortfolioLedgerEventFactory eventFactory;
    private final OutboxPort outbox;
    private final CommandIdempotencyPort idempotencyPort;

    public OpenPortfolioCommandHandler(
            LedgerWriteUnitOfWorkPort unitOfWork,
            PortfolioEventStorePort eventStore,
            PortfolioLedgerEventFactory eventFactory,
            OutboxPort outbox,
            CommandIdempotencyPort idempotencyPort
    ) {
        this.unitOfWork = unitOfWork;
        this.eventStore = eventStore;
        this.eventFactory = eventFactory;
        this.outbox = outbox;
        this.idempotencyPort = idempotencyPort;
    }

    @Override
    public UUID handle(OpenPortfolioCommand command) {
        return unitOfWork.execute(() -> {
            var existing = idempotencyPort.findByToken(command.idempotencyToken());
            if (existing.isPresent()) {
                if (!existing.get().aggregateId().equals(command.portfolioId())) {
                    throw new IllegalStateException(
                            "idempotencyToken already bound to portfolio " + existing.get().aggregateId()
                    );
                }
                return existing.get().resultEventId();
            }

            if (!eventStore.loadStream(command.portfolioId()).isEmpty()) {
                throw new IllegalStateException("Portfolio already exists: " + command.portfolioId());
            }

            PortfolioLedgerEvent event = eventFactory.createPortfolioOpened(command);
            eventStore.append(event);
            outbox.enqueue(event);
            idempotencyPort.record(
                    command.idempotencyToken(),
                    COMMAND_TYPE,
                    command.portfolioId(),
                    event.eventId()
            );
            return event.eventId();
        });
    }
}
