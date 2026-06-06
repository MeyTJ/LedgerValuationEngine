package com.ledger.valuation.application.port.outbound;

import com.ledger.valuation.domain.AuditExportJob;
import com.ledger.valuation.domain.AuditExportJobStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuditExportJobPort {

    void save(AuditExportJob job);

    Optional<AuditExportJob> findById(UUID id);

    List<AuditExportJob> findByStatus(AuditExportJobStatus status, int limit);

    void updateStatus(
            UUID id,
            AuditExportJobStatus status,
            String storagePath,
            String manifestChecksum,
            int eventCount
    );
}
