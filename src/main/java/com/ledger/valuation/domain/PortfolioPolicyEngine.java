package com.ledger.valuation.domain;

import java.util.ArrayList;
import java.util.List;

public final class PortfolioPolicyEngine {

    private PortfolioPolicyEngine() {}

    public static List<PolicyEvaluationResult> evaluateTransactionCommit(
            Portfolio portfolio,
            CommitTransactionCommand command,
            List<TenantPolicy> policies
    ) {
        var results = new ArrayList<PolicyEvaluationResult>();
        for (TenantPolicy policy : policies) {
            results.add(evaluatePolicy(portfolio, command, policy));
        }
        return results;
    }

    private static PolicyEvaluationResult evaluatePolicy(
            Portfolio portfolio,
            CommitTransactionCommand command,
            TenantPolicy policy
    ) {
        return switch (policy.ruleType()) {
            case MAX_SINGLE_DEBIT -> new PolicyEvaluationResult(
                    policy.ruleType(),
                    command.debitMinorUnits() > policy.thresholdMinorUnits() ? PolicyDecision.DENY : PolicyDecision.ALLOW,
                    command.debitMinorUnits(),
                    policy.thresholdMinorUnits()
            );
            case MAX_ACCOUNT_VALUE -> {
                long projected = portfolio
                        .projectAccountValueAfterTransactionCommit(command.creditMinorUnits(), command.debitMinorUnits())
                        .accountValueMinorUnits();
                yield new PolicyEvaluationResult(
                        policy.ruleType(),
                        projected > policy.thresholdMinorUnits() ? PolicyDecision.DENY : PolicyDecision.ALLOW,
                        projected,
                        policy.thresholdMinorUnits()
                );
            }
        };
    }
}
