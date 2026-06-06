package com.ledger.valuation.domain;

public record PolicyEvaluationResult(
        PolicyRuleType ruleType,
        PolicyDecision decision,
        long evaluatedAmountMinorUnits,
        long thresholdMinorUnits
) {
    public boolean isDenied() {
        return decision == PolicyDecision.DENY;
    }
}
