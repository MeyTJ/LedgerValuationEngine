package com.ledger.valuation.infrastructure.audit;

import com.ledger.valuation.application.port.outbound.AuditExportStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
public class LocalFileAuditExportStorage implements AuditExportStoragePort {

    private final Path exportRoot;

    public LocalFileAuditExportStorage(@Value("${ledger.audit.export.storage-path:./data/audit-exports}") String exportRoot) {
        this.exportRoot = Path.of(exportRoot);
    }

    @Override
    public Path writeExport(UUID jobId, String manifestJson, String eventsNdjson) {
        try {
            Path jobDir = exportRoot.resolve(jobId.toString());
            Files.createDirectories(jobDir);
            Path manifestPath = jobDir.resolve("manifest.json");
            Path eventsPath = jobDir.resolve("events.ndjson");
            Files.writeString(manifestPath, manifestJson);
            Files.writeString(eventsPath, eventsNdjson);
            return jobDir;
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to write audit export for job " + jobId, ex);
        }
    }
}
