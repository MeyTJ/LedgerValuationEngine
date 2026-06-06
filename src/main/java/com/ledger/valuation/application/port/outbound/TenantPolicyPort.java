package com.ledger.valuation.application.port.outbound;

import com.ledger.valuation.domain.PolicyRuleType;
import com.ledger.valuation.domain.TenantPolicy;

import java.util.List;

public interface TenantPolicyPort {

    List<TenantPolicy> findByTenant(String tenantId);

    void upsert(TenantPolicy policy);

    void delete(String tenantId, PolicyRuleType ruleType);
}
