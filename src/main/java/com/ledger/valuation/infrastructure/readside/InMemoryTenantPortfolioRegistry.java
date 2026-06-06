package com.ledger.valuation.infrastructure.readside;

import com.ledger.valuation.application.port.outbound.TenantPortfolioRegistryPort;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryTenantPortfolioRegistry implements TenantPortfolioRegistryPort {

    private final ConcurrentHashMap<UUID, String> portfolioTenants = new ConcurrentHashMap<>();

    @Override
    public void register(UUID portfolioId, String tenantId) {
        portfolioTenants.put(portfolioId, tenantId);
    }

    @Override
    public Optional<String> findTenantByPortfolio(UUID portfolioId) {
        return Optional.ofNullable(portfolioTenants.get(portfolioId));
    }
}
