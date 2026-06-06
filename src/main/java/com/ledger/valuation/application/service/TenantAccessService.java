package com.ledger.valuation.application.service;

import com.ledger.valuation.application.port.outbound.TenantPortfolioRegistryPort;

import java.util.UUID;

public final class TenantAccessService {

    private final TenantPortfolioRegistryPort tenantRegistry;

    public TenantAccessService(TenantPortfolioRegistryPort tenantRegistry) {
        this.tenantRegistry = tenantRegistry;
    }

    public void assertPortfolioBelongsToTenant(UUID portfolioId, String tenantId) {
        String ownerTenant = tenantRegistry.findTenantByPortfolio(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown portfolio " + portfolioId));
        if (!ownerTenant.equals(tenantId)) {
            throw new IllegalArgumentException(
                    "Portfolio " + portfolioId + " does not belong to tenant " + tenantId
            );
        }
    }
}
