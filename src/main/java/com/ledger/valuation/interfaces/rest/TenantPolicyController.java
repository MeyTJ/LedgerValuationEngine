package com.ledger.valuation.interfaces.rest;

import com.ledger.valuation.application.port.inbound.ManageTenantPolicyUseCase;
import com.ledger.valuation.domain.PolicyRuleType;
import com.ledger.valuation.domain.TenantPolicy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/tenants/{tenantId}/policies")
public class TenantPolicyController {

    private final ManageTenantPolicyUseCase manageTenantPolicyUseCase;

    public TenantPolicyController(ManageTenantPolicyUseCase manageTenantPolicyUseCase) {
        this.manageTenantPolicyUseCase = manageTenantPolicyUseCase;
    }

    @GetMapping
    public List<TenantPolicy> list(@PathVariable String tenantId) {
        return manageTenantPolicyUseCase.listPolicies(tenantId);
    }

    @PutMapping
    public ResponseEntity<Void> upsert(
            @PathVariable String tenantId,
            @RequestBody UpsertPolicyRequest request
    ) {
        manageTenantPolicyUseCase.upsertPolicy(new TenantPolicy(
                tenantId,
                request.ruleType(),
                request.thresholdMinorUnits(),
                request.effectiveFrom() == null ? Instant.now() : request.effectiveFrom()
        ));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{ruleType}")
    public ResponseEntity<Void> delete(
            @PathVariable String tenantId,
            @PathVariable PolicyRuleType ruleType
    ) {
        manageTenantPolicyUseCase.deletePolicy(tenantId, ruleType);
        return ResponseEntity.noContent().build();
    }

    public record UpsertPolicyRequest(
            PolicyRuleType ruleType,
            long thresholdMinorUnits,
            Instant effectiveFrom
    ) {}
}
