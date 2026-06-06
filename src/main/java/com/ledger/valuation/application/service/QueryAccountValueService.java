package com.ledger.valuation.application.service;

import com.ledger.valuation.application.port.inbound.QueryAccountValueUseCase;
import com.ledger.valuation.application.port.outbound.AccountValueProjectionPort;

public final class QueryAccountValueService implements QueryAccountValueUseCase {

    private final AccountValueProjectionPort projectionPort;

    public QueryAccountValueService(AccountValueProjectionPort projectionPort) {
        this.projectionPort = projectionPort;
    }

    @Override
    public Long getAccountValue(String accountCode) {
        return projectionPort.getAccountValue(accountCode);
    }
}
