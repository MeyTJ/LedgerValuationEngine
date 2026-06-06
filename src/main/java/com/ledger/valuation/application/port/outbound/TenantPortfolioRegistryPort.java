package com.ledger.valuation.application.port.outbound;

import java.util.Optional;
import java.util.UUID;

public interface TenantPortfolioRegistryPort {

    void register(UUID portfolioId, String tenantId);

    Optional<String> findTenantByPortfolio(UUID portfolioId);
}
