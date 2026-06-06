package com.ledger.valuation.infrastructure.platform;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ShardRoutingService {

    private final int shardCount;

    public ShardRoutingService(@Value("${ledger.sharding.shard-count:16}") int shardCount) {
        this.shardCount = shardCount;
    }

    public int resolveShard(UUID portfolioId) {
        return Math.floorMod(portfolioId.hashCode(), shardCount);
    }

    public int shardCount() {
        return shardCount;
    }
}
