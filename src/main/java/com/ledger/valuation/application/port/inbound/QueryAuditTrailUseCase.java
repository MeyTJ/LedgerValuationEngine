package com.ledger.valuation.application.port.inbound;

import com.ledger.valuation.application.readmodel.AuditEventRecord;

import java.util.List;
import java.util.UUID;

public interface QueryAuditTrailUseCase {

    List<AuditEventRecord> queryByPortfolio(UUID portfolioId, long fromSequence, long toSequence);
}
