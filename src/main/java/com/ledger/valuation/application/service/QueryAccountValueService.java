package com.ledger.valuation.application.service;

import com.ledger.valuation.application.port.inbound.QueryAccountValueUseCase;
import com.ledger.valuation.application.port.outbound.AccountValueReadModelPort;

public final class QueryAccountValueService implements QueryAccountValueUseCase {

    private final AccountValueReadModelPort readModel;

    public QueryAccountValueService(AccountValueReadModelPort readModel) {
        this.readModel = readModel;
    }

    @Override
    public Long getAccountValue(String accountCode) {
        return readModel.findByAccountCode(accountCode)
                .map(view -> view.accountValueMinorUnits())
                .orElse(null);
    }
}
