package com.ledger.valuation.application.port.inbound;

import com.ledger.valuation.domain.PolicyRuleType;
import com.ledger.valuation.domain.TenantPolicy;

import java.util.List;

public interface ManageTenantPolicyUseCase {

    List<TenantPolicy> listPolicies(String tenantId);

    void upsertPolicy(TenantPolicy policy);

    void deletePolicy(String tenantId, PolicyRuleType ruleType);
}
