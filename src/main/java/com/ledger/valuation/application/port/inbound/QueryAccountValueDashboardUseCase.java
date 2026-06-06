package com.ledger.valuation.application.port.inbound;

import com.ledger.valuation.application.readmodel.AccountValueDashboardView;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface QueryAccountValueDashboardUseCase {

    Optional<AccountValueDashboardView> getByAccountCode(String accountCode);

    Optional<AccountValueDashboardView> getByPortfolioId(UUID portfolioId);

    Collection<AccountValueDashboardView> getDashboardSnapshot();
}
