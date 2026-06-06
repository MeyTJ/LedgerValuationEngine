package com.ledger.valuation.infrastructure.readside;

import com.github.benmanes.caffeine.cache.Cache;
import com.ledger.valuation.application.port.outbound.AccountValueReadModelPort;
import com.ledger.valuation.application.readmodel.AccountValueDashboardView;
import com.ledger.valuation.domain.PortfolioLedgerEvent;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryAccountValueReadModelStore implements AccountValueReadModelPort {

    private final Cache<String, AccountValueDashboardView> accountCodeIndex;
    private final Cache<UUID, AccountValueDashboardView> portfolioIdIndex;
    private final ConcurrentHashMap<UUID, AccountValueDashboardView> dashboardShard;

    public InMemoryAccountValueReadModelStore(
            Cache<String, AccountValueDashboardView> accountCodeIndex,
            Cache<UUID, AccountValueDashboardView> portfolioIdIndex
    ) {
        this.accountCodeIndex = accountCodeIndex;
        this.portfolioIdIndex = portfolioIdIndex;
        this.dashboardShard = new ConcurrentHashMap<>();
    }

    @Override
    public void project(PortfolioLedgerEvent event) {
        switch (event) {
            case PortfolioLedgerEvent.PortfolioAccountOpened opened -> projectAccountOpened(opened);
            case PortfolioLedgerEvent.TransactionCommitted committed -> projectMutation(
                    committed.portfolioId(),
                    committed.sequenceNumber(),
                    committed.occurredAt(),
                    current -> current.accountValueMinorUnits()
                            + committed.creditMinorUnits()
                            - committed.debitMinorUnits()
            );
            case PortfolioLedgerEvent.FeeAccrued fee -> projectMutation(
                    fee.portfolioId(),
                    fee.sequenceNumber(),
                    fee.occurredAt(),
                    current -> current.accountValueMinorUnits() - fee.feeMinorUnits()
            );
            case PortfolioLedgerEvent.InterestCredited interest -> projectMutation(
                    interest.portfolioId(),
                    interest.sequenceNumber(),
                    interest.occurredAt(),
                    current -> current.accountValueMinorUnits() + interest.interestMinorUnits()
            );
            case PortfolioLedgerEvent.AccountValueAdjustmentPosted adjustment -> projectMutation(
                    adjustment.portfolioId(),
                    adjustment.sequenceNumber(),
                    adjustment.occurredAt(),
                    current -> current.accountValueMinorUnits() + adjustment.accountValueDeltaMinorUnits()
            );
        }
    }

    @Override
    public Optional<AccountValueDashboardView> findByAccountCode(String accountCode) {
        return Optional.ofNullable(accountCodeIndex.getIfPresent(accountCode));
    }

    @Override
    public Optional<AccountValueDashboardView> findByPortfolioId(UUID portfolioId) {
        return Optional.ofNullable(portfolioIdIndex.getIfPresent(portfolioId));
    }

    @Override
    public Collection<AccountValueDashboardView> findAll() {
        return dashboardShard.values();
    }

    private void projectAccountOpened(PortfolioLedgerEvent.PortfolioAccountOpened opened) {
        AccountValueDashboardView view = new AccountValueDashboardView(
                opened.portfolioId(),
                opened.accountCode(),
                opened.currency(),
                0L,
                opened.sequenceNumber(),
                opened.occurredAt()
        );
        storeView(view);
    }

    private void projectMutation(
            UUID portfolioId,
            long sequenceNumber,
            java.time.Instant occurredAt,
            java.util.function.ToLongFunction<AccountValueDashboardView> nextAccountValueCalculator
    ) {
        portfolioIdIndex.asMap().compute(portfolioId, (_, current) -> {
            if (current == null) {
                throw new IllegalStateException("Cannot project event for unknown portfolio " + portfolioId);
            }
            if (sequenceNumber <= current.lastSequenceNumber()) {
                return current;
            }
            long nextAccountValueMinorUnits = nextAccountValueCalculator.applyAsLong(current);
            AccountValueDashboardView updated = current.withAccountValue(
                    nextAccountValueMinorUnits,
                    sequenceNumber,
                    occurredAt
            );
            accountCodeIndex.put(updated.accountCode(), updated);
            dashboardShard.put(portfolioId, updated);
            return updated;
        });
    }

    private void storeView(AccountValueDashboardView view) {
        portfolioIdIndex.put(view.portfolioId(), view);
        accountCodeIndex.put(view.accountCode(), view);
        dashboardShard.put(view.portfolioId(), view);
    }
}
