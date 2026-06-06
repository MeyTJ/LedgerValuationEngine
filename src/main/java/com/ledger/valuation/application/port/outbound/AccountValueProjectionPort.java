package com.ledger.valuation.application.port.outbound;

import com.ledger.valuation.domain.DomainEvent;

public interface AccountValueProjectionPort {

    void onAccountOpened(DomainEvent.AccountOpened event);

    void onTransactionPosted(DomainEvent.TransactionPosted event);

    void onAccountValued(DomainEvent.AccountValued event);

    Long getAccountValue(String accountCode);
}
