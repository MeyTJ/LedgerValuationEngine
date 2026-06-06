package com.ledger.valuation.application.port.inbound;

import com.ledger.valuation.domain.AuditExportJob;

import java.util.Optional;
import java.util.UUID;

public interface QueryAuditExportUseCase {

    Optional<AuditExportJob> findById(UUID id);
}
