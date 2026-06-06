package com.ledger.valuation.interfaces.rest;

import com.ledger.valuation.application.port.inbound.OpenPortfolioUseCase;
import com.ledger.valuation.application.port.inbound.CommitTransactionUseCase;
import com.ledger.valuation.application.model.CommitTransactionResult;
import com.ledger.valuation.domain.CommitTransactionCommand;
import com.ledger.valuation.domain.OpenPortfolioCommand;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/portfolios")
public class PortfolioController {

    private final OpenPortfolioUseCase openPortfolioUseCase;
    private final CommitTransactionUseCase commitTransactionUseCase;

    public PortfolioController(
            OpenPortfolioUseCase openPortfolioUseCase,
            CommitTransactionUseCase commitTransactionUseCase
    ) {
        this.openPortfolioUseCase = openPortfolioUseCase;
        this.commitTransactionUseCase = commitTransactionUseCase;
    }

    @PostMapping
    public ResponseEntity<OpenPortfolioResponse> open(@RequestBody OpenPortfolioRequest request) {
        UUID eventId = openPortfolioUseCase.handle(new OpenPortfolioCommand(
                request.idempotencyToken(),
                request.portfolioId(),
                request.accountCode(),
                request.currency(),
                request.tenantId()
        ));
        return ResponseEntity.ok(new OpenPortfolioResponse(request.portfolioId(), eventId));
    }

    @PostMapping("/{portfolioId}/transactions")
    public ResponseEntity<CommitTransactionResponse> commit(
            @PathVariable UUID portfolioId,
            @RequestBody CommitTransactionRequest request
    ) {
        CommitTransactionResult result = commitTransactionUseCase.handle(new CommitTransactionCommand(
                request.idempotencyToken(),
                portfolioId,
                request.creditMinorUnits(),
                request.debitMinorUnits(),
                request.transactionReference()
        ));
        return ResponseEntity.ok(new CommitTransactionResponse(
                result.status().name(),
                result.eventId(),
                result.resultingAccountValueMinorUnits()
        ));
    }

    public record OpenPortfolioRequest(
            String idempotencyToken,
            UUID portfolioId,
            String accountCode,
            String currency,
            String tenantId
    ) {}

    public record OpenPortfolioResponse(UUID portfolioId, UUID eventId) {}

    public record CommitTransactionRequest(
            String idempotencyToken,
            long creditMinorUnits,
            long debitMinorUnits,
            String transactionReference
    ) {}

    public record CommitTransactionResponse(
            String status,
            UUID eventId,
            long resultingAccountValueMinorUnits
    ) {}
}
