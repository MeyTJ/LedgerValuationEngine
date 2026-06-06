package com.ledger.valuation.application.service;

import com.ledger.valuation.application.port.inbound.CreateAuditExportUseCase;
import com.ledger.valuation.application.port.outbound.AuditExportJobPort;
import com.ledger.valuation.domain.AuditExportJob;
import com.ledger.valuation.domain.AuditExportJobStatus;
import com.ledger.valuation.domain.CreateAuditExportCommand;

import java.time.Clock;
import java.util.UUID;
import java.util.function.Supplier;

public final class CreateAuditExportService implements CreateAuditExportUseCase {

    private final AuditExportJobPort jobPort;
    private final TenantAccessService tenantAccessService;
    private final Clock clock;
    private final Supplier<UUID> idSupplier;

    public CreateAuditExportService(
            AuditExportJobPort jobPort,
            TenantAccessService tenantAccessService,
            Clock clock,
            Supplier<UUID> idSupplier
    ) {
        this.jobPort = jobPort;
        this.tenantAccessService = tenantAccessService;
        this.clock = clock;
        this.idSupplier = idSupplier;
    }

    @Override
    public AuditExportJob handle(CreateAuditExportCommand command) {
        tenantAccessService.assertPortfolioBelongsToTenant(command.portfolioId(), command.tenantId());
        AuditExportJob job = new AuditExportJob(
                idSupplier.get(),
                command.portfolioId(),
                command.tenantId(),
                command.fromSequence(),
                command.toSequence(),
                AuditExportJobStatus.PENDING,
                null,
                null,
                0,
                clock.instant(),
                null
        );
        jobPort.save(job);
        return job;
    }
}
