package com.ledger.valuation.application.port.outbound;

import com.ledger.valuation.application.readmodel.AccountValueAsOfView;

import java.util.Optional;

public interface AsOfReplayCachePort {

    Optional<AccountValueAsOfView> get(String cacheKey);

    void put(String cacheKey, AccountValueAsOfView view);
}
