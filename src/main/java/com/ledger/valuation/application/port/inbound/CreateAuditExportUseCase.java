package com.ledger.valuation.application.port.inbound;

import com.ledger.valuation.domain.AuditExportJob;
import com.ledger.valuation.domain.CreateAuditExportCommand;

public interface CreateAuditExportUseCase {

    AuditExportJob handle(CreateAuditExportCommand command);
}
