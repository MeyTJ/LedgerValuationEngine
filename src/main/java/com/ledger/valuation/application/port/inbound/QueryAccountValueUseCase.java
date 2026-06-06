package com.ledger.valuation.application.port.inbound;

public interface QueryAccountValueUseCase {

    Long getAccountValue(String accountCode);
}
