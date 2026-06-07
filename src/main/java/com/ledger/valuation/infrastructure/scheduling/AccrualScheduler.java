package com.ledger.valuation.infrastructure.scheduling;

import com.ledger.valuation.application.port.outbound.PortfolioEventStorePort;
import com.ledger.valuation.application.service.AccrualPostingService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
public class AccrualScheduler {

    private final AccrualPostingService accrualPostingService;
    private final PortfolioEventStorePort portfolioEventStore;
    private final long defaultFeeMinorUnits;
    private final boolean enabled;

    public AccrualScheduler(
            AccrualPostingService accrualPostingService,
            PortfolioEventStorePort portfolioEventStore,
            com.ledger.valuation.infrastructure.config.LedgerProperties ledgerProperties
    ) {
        this.accrualPostingService = accrualPostingService;
        this.portfolioEventStore = portfolioEventStore;
        this.defaultFeeMinorUnits = ledgerProperties.accrual().defaultFeeMinorUnits();
        this.enabled = ledgerProperties.accrual().enabled();
    }

    @Scheduled(cron = "${ledger.accrual.cron:0 0 2 * * *}")
    @SchedulerLock(name = "accrualScheduler", lockAtMostFor = "PT30M", lockAtLeastFor = "PT1M")
    public void runDailyAccruals() {
        if (!enabled) {
            return;
        }
        String runId = LocalDate.now().toString();
        for (UUID portfolioId : portfolioEventStore.listPortfolioIds()) {
            accrualPostingService.postFee(portfolioId, defaultFeeMinorUnits, "SCHEDULED_MANAGEMENT_FEE", runId);
        }
    }
}
