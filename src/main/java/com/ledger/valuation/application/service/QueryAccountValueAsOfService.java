package com.ledger.valuation.application.service;

import com.ledger.valuation.application.port.inbound.QueryAccountValueAsOfUseCase;
import com.ledger.valuation.application.port.outbound.AsOfReplayCachePort;
import com.ledger.valuation.application.port.outbound.PortfolioEventStorePort;
import com.ledger.valuation.application.readmodel.AccountValueAsOfView;
import com.ledger.valuation.domain.Portfolio;
import com.ledger.valuation.domain.PortfolioLedgerEvent;
import com.ledger.valuation.domain.PortfolioLedgerEventStream;
import com.ledger.valuation.domain.PortfolioNotFoundException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class QueryAccountValueAsOfService implements QueryAccountValueAsOfUseCase {

    private final PortfolioEventStorePort eventStore;
    private final AsOfReplayCachePort asOfReplayCache;
    private final Clock clock;
    private final Timer replayTimer;
    private final Counter replayCounter;

    public QueryAccountValueAsOfService(
            PortfolioEventStorePort eventStore,
            AsOfReplayCachePort asOfReplayCache,
            Clock clock,
            MeterRegistry meterRegistry
    ) {
        this.eventStore = eventStore;
        this.asOfReplayCache = asOfReplayCache;
        this.clock = clock;
        this.replayTimer = Timer.builder("ledger.asof.replay.duration")
                .description("Account Value as-of replay duration")
                .register(meterRegistry);
        this.replayCounter = meterRegistry.counter("ledger.asof.replay.requests");
    }

    @Override
    public Optional<AccountValueAsOfView> getAsOf(UUID portfolioId, Instant asOf) {
        replayCounter.increment();
        String cacheKey = portfolioId + ":" + asOf;
        return asOfReplayCache.get(cacheKey).or(() -> {
            AccountValueAsOfView computed = replayTimer.record(() -> compute(portfolioId, asOf));
            asOfReplayCache.put(cacheKey, computed);
            return Optional.of(computed);
        });
    }

    private AccountValueAsOfView compute(UUID portfolioId, Instant asOf) {
        PortfolioLedgerEventStream stream = eventStore.loadStream(portfolioId);
        if (stream.isEmpty()) {
            throw new PortfolioNotFoundException(portfolioId);
        }
        List<PortfolioLedgerEvent> filtered = filterEventsAtOrBefore(stream.eventRecords(), asOf);
        if (filtered.isEmpty()) {
            throw new IllegalArgumentException("No events exist at or before asOf=" + asOf);
        }
        Portfolio portfolio = Portfolio.rehydrateFromEventRecords(filtered);
        return new AccountValueAsOfView(
                portfolio.portfolioId(),
                portfolio.accountCode(),
                portfolio.accountValueCurrency(),
                portfolio.accountValueMinorUnits(),
                portfolio.lastSequenceNumber(),
                asOf,
                clock.instant(),
                filtered.size()
        );
    }

    private static List<PortfolioLedgerEvent> filterEventsAtOrBefore(
            List<PortfolioLedgerEvent> events,
            Instant asOf
    ) {
        var upToAsOf = new ArrayList<PortfolioLedgerEvent>();
        PortfolioLedgerEvent.AccountValueSnapshot latestSnapshot = null;
        for (PortfolioLedgerEvent event : events) {
            if (event.occurredAt().isAfter(asOf)) {
                break;
            }
            upToAsOf.add(event);
            if (event instanceof PortfolioLedgerEvent.AccountValueSnapshot snapshot) {
                latestSnapshot = snapshot;
            }
        }
        if (latestSnapshot == null) {
            return upToAsOf;
        }
        var optimized = new ArrayList<PortfolioLedgerEvent>();
        for (PortfolioLedgerEvent event : upToAsOf) {
            if (event instanceof PortfolioLedgerEvent.PortfolioAccountOpened
                    || event.sequenceNumber() >= latestSnapshot.sequenceNumber()) {
                optimized.add(event);
            }
        }
        return optimized;
    }
}
