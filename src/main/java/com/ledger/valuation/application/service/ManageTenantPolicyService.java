package com.ledger.valuation.application.service;

import com.ledger.valuation.application.port.inbound.ManageTenantPolicyUseCase;
import com.ledger.valuation.application.port.outbound.TenantPolicyPort;
import com.ledger.valuation.domain.PolicyRuleType;
import com.ledger.valuation.domain.TenantPolicy;

import java.util.List;

public final class ManageTenantPolicyService implements ManageTenantPolicyUseCase {

    private final TenantPolicyPort tenantPolicyPort;

    public ManageTenantPolicyService(TenantPolicyPort tenantPolicyPort) {
        this.tenantPolicyPort = tenantPolicyPort;
    }

    @Override
    public List<TenantPolicy> listPolicies(String tenantId) {
        return tenantPolicyPort.findByTenant(tenantId);
    }

    @Override
    public void upsertPolicy(TenantPolicy policy) {
        tenantPolicyPort.upsert(policy);
    }

    @Override
    public void deletePolicy(String tenantId, PolicyRuleType ruleType) {
        tenantPolicyPort.delete(tenantId, ruleType);
    }
}
