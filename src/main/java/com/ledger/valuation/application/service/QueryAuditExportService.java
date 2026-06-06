package com.ledger.valuation.application.service;

import com.ledger.valuation.application.port.inbound.QueryAuditExportUseCase;
import com.ledger.valuation.application.port.outbound.AuditExportJobPort;
import com.ledger.valuation.domain.AuditExportJob;

import java.util.Optional;
import java.util.UUID;

public final class QueryAuditExportService implements QueryAuditExportUseCase {

    private final AuditExportJobPort jobPort;

    public QueryAuditExportService(AuditExportJobPort jobPort) {
        this.jobPort = jobPort;
    }

    @Override
    public Optional<AuditExportJob> findById(UUID id) {
        return jobPort.findById(id);
    }
}
