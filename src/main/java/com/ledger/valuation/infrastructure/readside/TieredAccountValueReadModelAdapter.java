package com.ledger.valuation.infrastructure.readside;

import com.github.benmanes.caffeine.cache.Cache;
import com.ledger.valuation.application.port.outbound.AccountValueReadModelPort;
import com.ledger.valuation.application.port.outbound.TenantPortfolioRegistryPort;
import com.ledger.valuation.application.readmodel.AccountValueDashboardView;
import com.ledger.valuation.domain.Portfolio;
import com.ledger.valuation.domain.PortfolioLedgerEvent;
import com.ledger.valuation.domain.PortfolioStatus;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Primary
public class TieredAccountValueReadModelAdapter implements AccountValueReadModelPort {

    private final InMemoryAccountValueReadModelStore l1Store;
    private final JdbcAccountValueReadModelStore l2Store;
    private final boolean localOnly;

    public TieredAccountValueReadModelAdapter(
            InMemoryAccountValueReadModelStore l1Store,
            JdbcAccountValueReadModelStore l2Store,
            com.ledger.valuation.infrastructure.config.LedgerProperties ledgerProperties
    ) {
        this.l1Store = l1Store;
        this.l2Store = l2Store;
        this.localOnly = ledgerProperties.readside().localOnly();
    }

    @Override
    public void project(PortfolioLedgerEvent event) {
        l1Store.project(event);
        if (!localOnly) {
            l1Store.findByPortfolioId(event.portfolioId()).ifPresent(l2Store::upsert);
        }
    }

    @Override
    public Optional<AccountValueDashboardView> findByAccountCode(String accountCode) {
        return l1Store.findByAccountCode(accountCode)
                .or(() -> localOnly ? Optional.empty() : l2Store.findByAccountCode(accountCode)
                        .map(this::backfillL1));
    }

    @Override
    public Optional<AccountValueDashboardView> findByPortfolioId(UUID portfolioId) {
        return l1Store.findByPortfolioId(portfolioId)
                .or(() -> localOnly ? Optional.empty() : l2Store.findByPortfolioId(portfolioId)
                        .map(this::backfillL1));
    }

    @Override
    public Collection<AccountValueDashboardView> findAll() {
        if (localOnly) {
            return l1Store.findAll();
        }
        return l2Store.findAll();
    }

    private AccountValueDashboardView backfillL1(AccountValueDashboardView view) {
        l1Store.storeViewDirect(view);
        return view;
    }
}
