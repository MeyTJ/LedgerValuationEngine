package com.ledger.valuation.infrastructure.readside;

import com.ledger.valuation.application.port.outbound.AccountValueStreamPort;
import com.ledger.valuation.application.readmodel.AccountValueDashboardView;
import com.ledger.valuation.infrastructure.config.LedgerProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SseAccountValueStreamAdapter implements AccountValueStreamPort {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final ConcurrentHashMap<SseEmitter, String> emitterTenants = new ConcurrentHashMap<>();
    private final int maxConnections;

    public SseAccountValueStreamAdapter(LedgerProperties ledgerProperties) {
        this.maxConnections = ledgerProperties.stream().maxConnections();
    }

    @Override
    public StreamSubscription subscribe(String tenantId) {
        if (emitters.size() >= maxConnections) {
            throw new IllegalStateException("Maximum SSE connections reached");
        }
        SseEmitter emitter = new SseEmitter(0L);
        emitterTenants.put(emitter, tenantId);
        emitter.onCompletion(() -> removeEmitter(emitter));
        emitter.onTimeout(() -> removeEmitter(emitter));
        emitter.onError(_ -> removeEmitter(emitter));
        emitters.add(emitter);
        return () -> emitter;
    }

    @Override
    public void publish(AccountValueDashboardView view) {
        for (SseEmitter emitter : emitters) {
            String subscribedTenant = emitterTenants.get(emitter);
            if (subscribedTenant != null
                    && !"*".equals(subscribedTenant)
                    && !subscribedTenant.equals(view.tenantId())) {
                continue;
            }
            try {
                emitter.send(SseEmitter.event()
                        .name("account-value")
                        .data(new AccountValueStreamPayload(
                                view.portfolioId(),
                                view.accountCode(),
                                view.tenantId(),
                                view.currency(),
                                view.accountValueMinorUnits(),
                                view.lastSequenceNumber(),
                                view.lastUpdatedAt()
                        )));
            } catch (IOException ex) {
                removeEmitter(emitter);
            }
        }
    }

    private void removeEmitter(SseEmitter emitter) {
        emitters.remove(emitter);
        emitterTenants.remove(emitter);
    }

    public record AccountValueStreamPayload(
            java.util.UUID portfolioId,
            String accountCode,
            String tenantId,
            String currency,
            long accountValueMinorUnits,
            long lastSequenceNumber,
            java.time.Instant lastUpdatedAt
    ) {}
}
