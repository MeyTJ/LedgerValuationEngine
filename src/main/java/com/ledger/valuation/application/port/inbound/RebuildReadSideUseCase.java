package com.ledger.valuation.application.port.inbound;

import java.util.UUID;

public interface RebuildReadSideUseCase {

    void rebuild(UUID aggregateId);
}
