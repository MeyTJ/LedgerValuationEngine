package com.ledger.valuation.interfaces.rest;

import com.ledger.valuation.application.port.inbound.RegisterPositionUseCase;
import com.ledger.valuation.application.service.TenantAccessService;
import com.ledger.valuation.domain.RegisterPositionCommand;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/portfolios")
public class PositionController {

    private final RegisterPositionUseCase registerPositionUseCase;
    private final TenantAccessService tenantAccessService;

    public PositionController(
            RegisterPositionUseCase registerPositionUseCase,
            TenantAccessService tenantAccessService
    ) {
        this.registerPositionUseCase = registerPositionUseCase;
        this.tenantAccessService = tenantAccessService;
    }

    @PostMapping("/{portfolioId}/positions")
    public ResponseEntity<RegisterPositionResponse> register(
            @PathVariable UUID portfolioId,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId,
            @RequestBody RegisterPositionRequest request
    ) {
        if (tenantId != null) {
            tenantAccessService.assertPortfolioBelongsToTenant(portfolioId, tenantId);
        }
        UUID eventId = registerPositionUseCase.handle(new RegisterPositionCommand(
                request.idempotencyToken(),
                portfolioId,
                request.instrumentId(),
                request.quantityMinorUnits(),
                request.costBasisMinorUnits()
        ));
        return ResponseEntity.ok(new RegisterPositionResponse(portfolioId, request.instrumentId(), eventId));
    }

    public record RegisterPositionRequest(
            String idempotencyToken,
            String instrumentId,
            long quantityMinorUnits,
            long costBasisMinorUnits
    ) {}

    public record RegisterPositionResponse(UUID portfolioId, String instrumentId, UUID eventId) {}
}
