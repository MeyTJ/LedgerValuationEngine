package com.ledger.valuation.infrastructure.scheduling;

import com.ledger.valuation.application.port.outbound.EventStorePort;
import com.ledger.valuation.application.service.AccrualPostingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AccrualScheduler {

    private final AccrualPostingService accrualPostingService;
    private final EventStorePort eventStore;
    private final long defaultFeeMinorUnits;
    private final boolean enabled;

    public AccrualScheduler(
            AccrualPostingService accrualPostingService,
            EventStorePort eventStore,
            @Value("${ledger.accrual.default-fee-minor-units:100}") long defaultFeeMinorUnits,
            @Value("${ledger.accrual.enabled:false}") boolean enabled
    ) {
        this.accrualPostingService = accrualPostingService;
        this.eventStore = eventStore;
        this.defaultFeeMinorUnits = defaultFeeMinorUnits;
        this.enabled = enabled;
    }

    @Scheduled(cron = "${ledger.accrual.cron:0 0 2 * * *}")
    public void runDailyAccruals() {
        if (!enabled) {
            return;
        }
        String runId = UUID.randomUUID().toString();
        for (UUID portfolioId : eventStore.listAggregateIds()) {
            accrualPostingService.postFee(portfolioId, defaultFeeMinorUnits, "SCHEDULED_MANAGEMENT_FEE", runId);
        }
    }
}
