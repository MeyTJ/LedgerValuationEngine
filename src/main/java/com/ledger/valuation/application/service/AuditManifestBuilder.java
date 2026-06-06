package com.ledger.valuation.application.service;

import com.ledger.valuation.application.readmodel.AuditEventRecord;
import com.ledger.valuation.domain.AuditExportJob;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.zip.CRC32;

public final class AuditManifestBuilder {

    private AuditManifestBuilder() {}

    public static String buildManifestJson(AuditExportJob job, List<AuditEventRecord> events) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"jobId\":\"").append(job.id()).append("\"");
        sb.append(",\"portfolioId\":\"").append(job.portfolioId()).append("\"");
        sb.append(",\"tenantId\":\"").append(job.tenantId()).append("\"");
        sb.append(",\"fromSequence\":").append(job.fromSequence());
        sb.append(",\"toSequence\":").append(job.toSequence());
        sb.append(",\"eventCount\":").append(events.size());
        sb.append(",\"eventsChecksum\":\"").append(eventsChecksum(events)).append("\"");
        sb.append(",\"records\":[");
        for (int i = 0; i < events.size(); i++) {
            AuditEventRecord record = events.get(i);
            if (i > 0) {
                sb.append(',');
            }
            sb.append("{\"eventId\":\"").append(record.eventId()).append("\"");
            sb.append(",\"sequenceNumber\":").append(record.sequenceNumber());
            sb.append(",\"eventType\":\"").append(record.eventType()).append("\"");
            sb.append(",\"checksum\":\"").append(record.checksum()).append("\"}");
        }
        sb.append("]}");
        return sb.toString();
    }

    public static String buildEventsNdjson(List<AuditEventRecord> events) {
        StringBuilder sb = new StringBuilder();
        for (AuditEventRecord record : events) {
            sb.append("{\"eventId\":\"").append(record.eventId()).append("\"");
            sb.append(",\"portfolioId\":\"").append(record.portfolioId()).append("\"");
            sb.append(",\"sequenceNumber\":").append(record.sequenceNumber());
            sb.append(",\"eventType\":\"").append(record.eventType()).append("\"");
            sb.append(",\"payload\":").append(escapeJson(record.payload()));
            sb.append(",\"occurredAt\":\"").append(record.occurredAt()).append("\"");
            sb.append(",\"checksum\":\"").append(record.checksum()).append("\"}\n");
        }
        return sb.toString();
    }

    public static String manifestChecksum(String manifestJson) {
        CRC32 crc = new CRC32();
        crc.update(manifestJson.getBytes(StandardCharsets.UTF_8));
        return Long.toHexString(crc.getValue());
    }

    private static String eventsChecksum(List<AuditEventRecord> events) {
        CRC32 crc = new CRC32();
        for (AuditEventRecord event : events) {
            crc.update(event.checksum().getBytes(StandardCharsets.UTF_8));
        }
        return Long.toHexString(crc.getValue());
    }

    private static String escapeJson(String payload) {
        return "\"" + payload.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
