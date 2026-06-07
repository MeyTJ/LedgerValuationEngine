package com.ledger.valuation.application.service;

import com.ledger.valuation.application.port.outbound.TenantPortfolioRegistryPort;

import java.util.UUID;

public final class TenantAccessService {

    private final TenantPortfolioRegistryPort tenantRegistry;
    private final boolean enforcementEnabled;

    public TenantAccessService(TenantPortfolioRegistryPort tenantRegistry, boolean enforcementEnabled) {
        this.tenantRegistry = tenantRegistry;
        this.enforcementEnabled = enforcementEnabled;
    }

    public void assertTenantContext(UUID portfolioId, String tenantId) {
        if (enforcementEnabled && (tenantId == null || tenantId.isBlank())) {
            throw new IllegalArgumentException("X-Tenant-Id header is required");
        }
        if (tenantId != null && !tenantId.isBlank()) {
            assertPortfolioBelongsToTenant(portfolioId, tenantId);
        }
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
