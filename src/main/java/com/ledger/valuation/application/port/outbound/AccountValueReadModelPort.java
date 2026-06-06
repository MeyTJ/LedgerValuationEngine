package com.ledger.valuation.application.port.outbound;

import com.ledger.valuation.application.readmodel.AccountValueDashboardView;
import com.ledger.valuation.domain.PortfolioLedgerEvent;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface AccountValueReadModelPort {

    void project(PortfolioLedgerEvent event);

    Optional<AccountValueDashboardView> findByAccountCode(String accountCode);

    Optional<AccountValueDashboardView> findByPortfolioId(UUID portfolioId);

    Collection<AccountValueDashboardView> findAll();
}
