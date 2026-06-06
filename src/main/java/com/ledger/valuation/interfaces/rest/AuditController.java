package com.ledger.valuation.interfaces.rest;

import com.ledger.valuation.application.port.inbound.QueryAuditTrailUseCase;
import com.ledger.valuation.application.readmodel.AuditEventRecord;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {

    private final QueryAuditTrailUseCase auditTrailUseCase;

    public AuditController(QueryAuditTrailUseCase auditTrailUseCase) {
        this.auditTrailUseCase = auditTrailUseCase;
    }

    @GetMapping("/portfolios/{portfolioId}/events")
    public List<AuditEventRecord> queryEvents(
            @PathVariable UUID portfolioId,
            @RequestParam(defaultValue = "1") long fromSequence,
            @RequestParam(defaultValue = "9223372036854775807") long toSequence
    ) {
        return auditTrailUseCase.queryByPortfolio(portfolioId, fromSequence, toSequence);
    }
}
