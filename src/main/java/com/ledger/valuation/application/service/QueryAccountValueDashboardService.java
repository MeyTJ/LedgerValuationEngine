package com.ledger.valuation.application.service;

import com.ledger.valuation.application.port.inbound.QueryAccountValueDashboardUseCase;
import com.ledger.valuation.application.port.outbound.AccountValueReadModelPort;
import com.ledger.valuation.application.readmodel.AccountValueDashboardView;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public final class QueryAccountValueDashboardService implements QueryAccountValueDashboardUseCase {

    private final AccountValueReadModelPort readModel;

    public QueryAccountValueDashboardService(AccountValueReadModelPort readModel) {
        this.readModel = readModel;
    }

    @Override
    public Optional<AccountValueDashboardView> getByAccountCode(String accountCode) {
        return readModel.findByAccountCode(accountCode);
    }

    @Override
    public Optional<AccountValueDashboardView> getByPortfolioId(UUID portfolioId) {
        return readModel.findByPortfolioId(portfolioId);
    }

    @Override
    public Collection<AccountValueDashboardView> getDashboardSnapshot() {
        return readModel.findAll();
    }
}
