package com.ledger.valuation.application.service;

import com.ledger.valuation.application.port.inbound.QueryAuditTrailUseCase;
import com.ledger.valuation.application.port.outbound.PortfolioEventStorePort;
import com.ledger.valuation.application.readmodel.AuditEventRecord;
import com.ledger.valuation.domain.PortfolioLedgerEvent;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.CRC32;

public final class AuditTrailQueryService implements QueryAuditTrailUseCase {

    private final PortfolioEventStorePort eventStore;

    public AuditTrailQueryService(PortfolioEventStorePort eventStore) {
        this.eventStore = eventStore;
    }

    @Override
    public List<AuditEventRecord> queryByPortfolio(UUID portfolioId, long fromSequence, long toSequence) {
        var records = new ArrayList<AuditEventRecord>();
        for (PortfolioLedgerEvent event : eventStore.loadStream(portfolioId).eventRecords()) {
            if (event.sequenceNumber() < fromSequence || event.sequenceNumber() > toSequence) {
                continue;
            }
            String payload = event.toString();
            records.add(new AuditEventRecord(
                    event.eventId(),
                    event.portfolioId(),
                    event.sequenceNumber(),
                    event.getClass().getSimpleName(),
                    payload,
                    event.occurredAt(),
                    checksum(payload)
            ));
        }
        return records;
    }

    private static String checksum(String payload) {
        CRC32 crc = new CRC32();
        crc.update(payload.getBytes(StandardCharsets.UTF_8));
        return Long.toHexString(crc.getValue());
    }
}
