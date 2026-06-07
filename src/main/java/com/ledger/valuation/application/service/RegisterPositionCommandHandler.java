package com.ledger.valuation.application.service;

import com.ledger.valuation.application.port.inbound.RegisterPositionUseCase;
import com.ledger.valuation.application.port.outbound.CommandIdempotencyPort;
import com.ledger.valuation.application.port.outbound.LedgerWriteUnitOfWorkPort;
import com.ledger.valuation.application.port.outbound.OutboxPort;
import com.ledger.valuation.application.port.outbound.PortfolioEventStorePort;
import com.ledger.valuation.domain.Portfolio;
import com.ledger.valuation.domain.PortfolioLedgerEvent;
import com.ledger.valuation.domain.PortfolioLedgerEventStream;
import com.ledger.valuation.domain.PortfolioNotFoundException;
import com.ledger.valuation.domain.RegisterPositionCommand;

import java.time.Clock;
import java.util.UUID;
import java.util.function.Supplier;

public final class RegisterPositionCommandHandler implements RegisterPositionUseCase {

    private static final String COMMAND_TYPE = "RegisterPosition";

    private final LedgerWriteUnitOfWorkPort unitOfWork;
    private final PortfolioEventStorePort eventStore;
    private final OutboxPort outbox;
    private final CommandIdempotencyPort idempotencyPort;
    private final Clock clock;
    private final Supplier<UUID> idSupplier;

    public RegisterPositionCommandHandler(
            LedgerWriteUnitOfWorkPort unitOfWork,
            PortfolioEventStorePort eventStore,
            OutboxPort outbox,
            CommandIdempotencyPort idempotencyPort,
            Clock clock,
            Supplier<UUID> idSupplier
    ) {
        this.unitOfWork = unitOfWork;
        this.eventStore = eventStore;
        this.outbox = outbox;
        this.idempotencyPort = idempotencyPort;
        this.clock = clock;
        this.idSupplier = idSupplier;
    }

    @Override
    public UUID handle(RegisterPositionCommand command) {
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

            Portfolio portfolio = rehydrate(command.portfolioId());
            portfolio.ensureActive();
            PortfolioLedgerEvent event = new PortfolioLedgerEvent.PositionOpened(
                    idSupplier.get(),
                    command.portfolioId(),
                    portfolio.lastSequenceNumber() + 1L,
                    clock.instant(),
                    command.instrumentId(),
                    command.quantityMinorUnits(),
                    command.costBasisMinorUnits(),
                    command.idempotencyToken()
            );
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

    private Portfolio rehydrate(UUID portfolioId) {
        PortfolioLedgerEventStream stream = eventStore.loadStream(portfolioId);
        if (stream.isEmpty()) {
            throw new PortfolioNotFoundException(portfolioId);
        }
        return Portfolio.rehydrateFromEventRecords(stream.eventRecords());
    }
}
