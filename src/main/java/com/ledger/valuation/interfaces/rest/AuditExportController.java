package com.ledger.valuation.interfaces.rest;

import com.ledger.valuation.application.port.inbound.CreateAuditExportUseCase;
import com.ledger.valuation.application.port.inbound.QueryAuditExportUseCase;
import com.ledger.valuation.domain.AuditExportJob;
import com.ledger.valuation.domain.CreateAuditExportCommand;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit/exports")
public class AuditExportController {

    private final CreateAuditExportUseCase createAuditExportUseCase;
    private final QueryAuditExportUseCase queryAuditExportUseCase;

    public AuditExportController(
            CreateAuditExportUseCase createAuditExportUseCase,
            QueryAuditExportUseCase queryAuditExportUseCase
    ) {
        this.createAuditExportUseCase = createAuditExportUseCase;
        this.queryAuditExportUseCase = queryAuditExportUseCase;
    }

    @PostMapping
    public ResponseEntity<AuditExportJob> create(@RequestBody CreateExportRequest request) {
        AuditExportJob job = createAuditExportUseCase.handle(new CreateAuditExportCommand(
                request.portfolioId(),
                request.tenantId(),
                request.fromSequence(),
                request.toSequence()
        ));
        return ResponseEntity.accepted().body(job);
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<AuditExportJob> get(@PathVariable UUID jobId) {
        return queryAuditExportUseCase.findById(jobId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public record CreateExportRequest(
            UUID portfolioId,
            String tenantId,
            long fromSequence,
            long toSequence
    ) {}
}
