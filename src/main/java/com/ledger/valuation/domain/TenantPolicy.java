package com.ledger.valuation.domain;

import java.time.Instant;

public record TenantPolicy(
        String tenantId,
        PolicyRuleType ruleType,
        long thresholdMinorUnits,
        Instant effectiveFrom
) {}
