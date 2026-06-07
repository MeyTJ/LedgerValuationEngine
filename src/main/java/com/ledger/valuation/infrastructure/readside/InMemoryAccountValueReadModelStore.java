package com.ledger.valuation.infrastructure.readside;

import com.github.benmanes.caffeine.cache.Cache;
import com.ledger.valuation.application.port.outbound.TenantPortfolioRegistryPort;
import com.ledger.valuation.application.readmodel.AccountValueDashboardView;
import com.ledger.valuation.domain.Portfolio;
import com.ledger.valuation.domain.PortfolioLedgerEvent;
import com.ledger.valuation.domain.PortfolioStatus;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryAccountValueReadModelStore {

    private final Cache<String, AccountValueDashboardView> accountCodeIndex;
    private final Cache<UUID, AccountValueDashboardView> portfolioIdIndex;
    private final ConcurrentHashMap<UUID, AccountValueDashboardView> dashboardShard;
    private final TenantPortfolioRegistryPort tenantRegistry;

    public InMemoryAccountValueReadModelStore(
            Cache<String, AccountValueDashboardView> accountCodeIndex,
            Cache<UUID, AccountValueDashboardView> portfolioIdIndex,
            TenantPortfolioRegistryPort tenantRegistry
    ) {
        this.accountCodeIndex = accountCodeIndex;
        this.portfolioIdIndex = portfolioIdIndex;
        this.tenantRegistry = tenantRegistry;
        this.dashboardShard = new ConcurrentHashMap<>();
    }

    public void project(PortfolioLedgerEvent event) {
        switch (event) {
            case PortfolioLedgerEvent.PortfolioAccountOpened opened -> projectAccountOpened(opened);
            case PortfolioLedgerEvent.TransactionCommitted committed -> projectViaDomain(committed);
            case PortfolioLedgerEvent.FeeAccrued fee -> projectViaDomain(fee);
            case PortfolioLedgerEvent.InterestCredited interest -> projectViaDomain(interest);
            case PortfolioLedgerEvent.AccountValueAdjustmentPosted adjustment -> projectViaDomain(adjustment);
            case PortfolioLedgerEvent.MarkToMarketApplied mark -> projectViaDomain(mark);
            case PortfolioLedgerEvent.AccrualPosted accrual -> projectViaDomain(accrual);
            case PortfolioLedgerEvent.AccountValueSnapshot snapshot -> projectViaDomain(snapshot);
            case PortfolioLedgerEvent.FxRateCommitted ignored -> { }
            case PortfolioLedgerEvent.PortfolioStatusChanged ignored -> { }
            case PortfolioLedgerEvent.PositionOpened ignored -> { }
            case PortfolioLedgerEvent.PolicyEvaluated ignored -> { }
        }
    }

    public Optional<AccountValueDashboardView> findByAccountCode(String accountCode) {
        return Optional.ofNullable(accountCodeIndex.getIfPresent(accountCode));
    }

    public Optional<AccountValueDashboardView> findByPortfolioId(UUID portfolioId) {
        return Optional.ofNullable(portfolioIdIndex.getIfPresent(portfolioId));
    }

    public Collection<AccountValueDashboardView> findAll() {
        return dashboardShard.values();
    }

    public void storeViewDirect(AccountValueDashboardView view) {
        storeView(view);
    }

    private void projectAccountOpened(PortfolioLedgerEvent.PortfolioAccountOpened opened) {
        AccountValueDashboardView view = AccountValueDashboardView.fromPortfolio(
                Portfolio.fromReadModelState(
                        opened.portfolioId(),
                        opened.accountCode(),
                        opened.tenantId(),
                        opened.currency(),
                        0L,
                        opened.status(),
                        opened.sequenceNumber()
                ),
                opened.occurredAt()
        );
        tenantRegistry.register(opened.portfolioId(), opened.tenantId());
        storeView(view);
    }

    private void projectViaDomain(PortfolioLedgerEvent event) {
        portfolioIdIndex.asMap().compute(event.portfolioId(), (_, current) -> {
            if (current == null) {
                throw new IllegalStateException("Cannot project event for unknown portfolio " + event.portfolioId());
            }
            if (event.sequenceNumber() <= current.lastSequenceNumber()) {
                return current;
            }
            Portfolio portfolio = Portfolio.fromReadModelState(
                    current.portfolioId(),
                    current.accountCode(),
                    current.tenantId(),
                    current.currency(),
                    current.accountValueMinorUnits(),
                    PortfolioStatus.ACTIVE,
                    current.lastSequenceNumber()
            );
            Portfolio updated = portfolio.applyEvent(event);
            AccountValueDashboardView view = AccountValueDashboardView.fromPortfolio(updated, event.occurredAt());
            accountCodeIndex.put(view.accountCode(), view);
            dashboardShard.put(event.portfolioId(), view);
            return view;
        });
    }

    private void storeView(AccountValueDashboardView view) {
        portfolioIdIndex.put(view.portfolioId(), view);
        accountCodeIndex.put(view.accountCode(), view);
        dashboardShard.put(view.portfolioId(), view);
    }
}
