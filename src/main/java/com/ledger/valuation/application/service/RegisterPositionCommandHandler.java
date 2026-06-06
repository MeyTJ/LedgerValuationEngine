package com.ledger.valuation.application.service;

import com.ledger.valuation.application.port.inbound.RegisterPositionUseCase;
import com.ledger.valuation.application.port.outbound.InstrumentPositionRegistryPort;
import com.ledger.valuation.application.port.outbound.LedgerWriteUnitOfWorkPort;
import com.ledger.valuation.application.port.outbound.OutboxPort;
import com.ledger.valuation.application.port.outbound.PortfolioEventStorePort;
import com.ledger.valuation.domain.InstrumentPosition;
import com.ledger.valuation.domain.Portfolio;
import com.ledger.valuation.domain.PortfolioLedgerEvent;
import com.ledger.valuation.domain.PortfolioLedgerEventFactory;
import com.ledger.valuation.domain.PortfolioLedgerEventStream;
import com.ledger.valuation.domain.PortfolioNotFoundException;
import com.ledger.valuation.domain.RegisterPositionCommand;

import java.time.Clock;
import java.util.UUID;
import java.util.function.Supplier;

public final class RegisterPositionCommandHandler implements RegisterPositionUseCase {

    private final LedgerWriteUnitOfWorkPort unitOfWork;
    private final PortfolioEventStorePort eventStore;
    private final PortfolioLedgerEventFactory eventFactory;
    private final OutboxPort outbox;
    private final InstrumentPositionRegistryPort positionRegistry;
    private final Clock clock;
    private final Supplier<UUID> idSupplier;

    public RegisterPositionCommandHandler(
            LedgerWriteUnitOfWorkPort unitOfWork,
            PortfolioEventStorePort eventStore,
            PortfolioLedgerEventFactory eventFactory,
            OutboxPort outbox,
            InstrumentPositionRegistryPort positionRegistry,
            Clock clock,
            Supplier<UUID> idSupplier
    ) {
        this.unitOfWork = unitOfWork;
        this.eventStore = eventStore;
        this.eventFactory = eventFactory;
        this.outbox = outbox;
        this.positionRegistry = positionRegistry;
        this.clock = clock;
        this.idSupplier = idSupplier;
    }

    @Override
    public UUID handle(RegisterPositionCommand command) {
        return unitOfWork.execute(() -> {
            Portfolio portfolio = rehydrate(command.portfolioId());
            portfolio.ensureActive();
            PortfolioLedgerEvent event = new PortfolioLedgerEvent.PositionOpened(
                    idSupplier.get(),
                    command.portfolioId(),
                    portfolio.lastSequenceNumber() + 1L,
                    clock.instant(),
                    command.instrumentId(),
                    command.quantityMinorUnits(),
                    command.costBasisMinorUnits()
            );
            eventStore.append(event);
            outbox.enqueue(event);
            positionRegistry.register(command.portfolioId(), new InstrumentPosition(
                    command.instrumentId(),
                    command.quantityMinorUnits(),
                    command.costBasisMinorUnits(),
                    command.costBasisMinorUnits()
            ));
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
