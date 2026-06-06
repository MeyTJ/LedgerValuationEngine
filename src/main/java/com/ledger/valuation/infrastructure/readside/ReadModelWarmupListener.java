package com.ledger.valuation.infrastructure.readside;

import com.ledger.valuation.application.port.inbound.RebuildPortfolioReadSideUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ReadModelWarmupListener {

    private static final Logger log = LoggerFactory.getLogger(ReadModelWarmupListener.class);

    private final RebuildPortfolioReadSideUseCase rebuildUseCase;
    private final boolean warmupEnabled;

    public ReadModelWarmupListener(
            RebuildPortfolioReadSideUseCase rebuildUseCase,
            @Value("${ledger.readside.warmup-on-startup:true}") boolean warmupEnabled
    ) {
        this.rebuildUseCase = rebuildUseCase;
        this.warmupEnabled = warmupEnabled;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void warmupReadModel() {
        if (!warmupEnabled) {
            return;
        }
        log.info("Warming up L1 Account Value read model from event store");
        rebuildUseCase.rebuildAll();
    }
}
