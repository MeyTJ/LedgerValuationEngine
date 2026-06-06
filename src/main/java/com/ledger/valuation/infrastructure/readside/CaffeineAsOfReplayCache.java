package com.ledger.valuation.infrastructure.readside;

import com.github.benmanes.caffeine.cache.Cache;
import com.ledger.valuation.application.port.outbound.AsOfReplayCachePort;
import com.ledger.valuation.application.readmodel.AccountValueAsOfView;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CaffeineAsOfReplayCache implements AsOfReplayCachePort {

    private final Cache<String, AccountValueAsOfView> cache;

    public CaffeineAsOfReplayCache(Cache<String, AccountValueAsOfView> asOfReplayCache) {
        this.cache = asOfReplayCache;
    }

    @Override
    public Optional<AccountValueAsOfView> get(String cacheKey) {
        return Optional.ofNullable(cache.getIfPresent(cacheKey));
    }

    @Override
    public void put(String cacheKey, AccountValueAsOfView view) {
        cache.put(cacheKey, view);
    }
}
