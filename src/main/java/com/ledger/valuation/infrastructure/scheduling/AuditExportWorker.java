package com.ledger.valuation.infrastructure.scheduling;

import com.ledger.valuation.application.port.inbound.QueryAuditTrailUseCase;
import com.ledger.valuation.application.port.outbound.AuditExportJobPort;
import com.ledger.valuation.application.port.outbound.AuditExportStoragePort;
import com.ledger.valuation.application.readmodel.AuditEventRecord;
import com.ledger.valuation.application.service.AuditManifestBuilder;
import com.ledger.valuation.domain.AuditExportJob;
import com.ledger.valuation.domain.AuditExportJobStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

@Component
public class AuditExportWorker {

    private static final Logger log = LoggerFactory.getLogger(AuditExportWorker.class);

    private final AuditExportJobPort jobPort;
    private final QueryAuditTrailUseCase auditTrailUseCase;
    private final AuditExportStoragePort storagePort;
    private final int batchSize;

    public AuditExportWorker(
            AuditExportJobPort jobPort,
            QueryAuditTrailUseCase auditTrailUseCase,
            AuditExportStoragePort storagePort,
            @Value("${ledger.audit.export.batch-size:10}") int batchSize
    ) {
        this.jobPort = jobPort;
        this.auditTrailUseCase = auditTrailUseCase;
        this.storagePort = storagePort;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${ledger.audit.export.worker-interval-ms:5000}")
    public void processPendingExports() {
        for (AuditExportJob job : jobPort.findByStatus(AuditExportJobStatus.PENDING, batchSize)) {
            try {
                List<AuditEventRecord> events = auditTrailUseCase.queryByPortfolio(
                        job.portfolioId(),
                        job.fromSequence(),
                        job.toSequence()
                );
                String manifestJson = AuditManifestBuilder.buildManifestJson(job, events);
                String eventsNdjson = AuditManifestBuilder.buildEventsNdjson(events);
                Path storagePath = storagePort.writeExport(job.id(), manifestJson, eventsNdjson);
                jobPort.updateStatus(
                        job.id(),
                        AuditExportJobStatus.COMPLETED,
                        storagePath.toString(),
                        AuditManifestBuilder.manifestChecksum(manifestJson),
                        events.size()
                );
                log.info("Audit export completed jobId={} eventCount={}", job.id(), events.size());
            } catch (Exception ex) {
                log.error("Audit export failed jobId={}", job.id(), ex);
                jobPort.updateStatus(job.id(), AuditExportJobStatus.FAILED, null, null, 0);
            }
        }
    }
}
