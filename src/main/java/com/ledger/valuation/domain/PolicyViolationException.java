package com.ledger.valuation.domain;

import java.util.UUID;

public final class PolicyViolationException extends RuntimeException {

    private final UUID portfolioId;
    private final PolicyRuleType ruleType;

    public PolicyViolationException(UUID portfolioId, PolicyRuleType ruleType, String message) {
        super(message);
        this.portfolioId = portfolioId;
        this.ruleType = ruleType;
    }

    public UUID portfolioId() {
        return portfolioId;
    }

    public PolicyRuleType ruleType() {
        return ruleType;
    }
}
