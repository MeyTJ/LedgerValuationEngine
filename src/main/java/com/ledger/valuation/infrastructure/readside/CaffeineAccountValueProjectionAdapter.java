package com.ledger.valuation.infrastructure.readside;

import com.github.benmanes.caffeine.cache.Cache;
import com.ledger.valuation.application.port.outbound.AccountValueProjectionPort;
import com.ledger.valuation.domain.DomainEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class CaffeineAccountValueProjectionAdapter implements AccountValueProjectionPort {

    private final Cache<String, Long> accountValueCache;
    private final AtomicLong valuationSequence = new AtomicLong();

    public CaffeineAccountValueProjectionAdapter(Cache<String, Long> accountValueCache) {
        this.accountValueCache = accountValueCache;
    }

    @Override
    public void onAccountOpened(DomainEvent.AccountOpened event) {
        accountValueCache.put(event.accountCode(), 0L);
    }

    @Override
    public void onTransactionPosted(DomainEvent.TransactionPosted event) {
        accountValueCache.asMap().compute(event.debitAccount(), (_, balance) ->
                (balance == null ? 0L : balance) - event.amountMinorUnits());
        accountValueCache.asMap().compute(event.creditAccount(), (_, balance) ->
                (balance == null ? 0L : balance) + event.amountMinorUnits());
    }

    @Override
    public void onAccountValued(DomainEvent.AccountValued event) {
        valuationSequence.set(event.sequenceNumber());
    }

    @Override
    public Long getAccountValue(String accountCode) {
        return accountValueCache.getIfPresent(accountCode);
    }
}
