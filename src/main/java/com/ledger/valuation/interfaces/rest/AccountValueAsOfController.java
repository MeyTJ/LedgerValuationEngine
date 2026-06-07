package com.ledger.valuation.interfaces.rest;

import com.ledger.valuation.application.port.inbound.QueryAccountValueAsOfUseCase;
import com.ledger.valuation.application.readmodel.AccountValueAsOfView;
import com.ledger.valuation.application.service.TenantAccessService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/portfolios")
public class AccountValueAsOfController {

    private final QueryAccountValueAsOfUseCase asOfUseCase;
    private final TenantAccessService tenantAccessService;

    public AccountValueAsOfController(
            QueryAccountValueAsOfUseCase asOfUseCase,
            TenantAccessService tenantAccessService
    ) {
        this.asOfUseCase = asOfUseCase;
        this.tenantAccessService = tenantAccessService;
    }

    @GetMapping("/{portfolioId}/account-value/as-of")
    public ResponseEntity<AccountValueAsOfView> getAccountValueAsOf(
            @PathVariable UUID portfolioId,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant asOf
    ) {
        tenantAccessService.assertTenantContext(portfolioId, tenantId);
        return asOfUseCase.getAsOf(portfolioId, asOf)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
