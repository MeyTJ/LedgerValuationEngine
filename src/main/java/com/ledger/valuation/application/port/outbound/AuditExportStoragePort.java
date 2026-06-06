package com.ledger.valuation.application.port.outbound;

import java.nio.file.Path;
import java.util.UUID;

public interface AuditExportStoragePort {

    Path writeExport(UUID jobId, String manifestJson, String eventsNdjson);
}
