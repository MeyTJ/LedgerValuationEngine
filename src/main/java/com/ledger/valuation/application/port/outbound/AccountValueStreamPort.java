package com.ledger.valuation.application.port.outbound;

import com.ledger.valuation.application.readmodel.AccountValueDashboardView;

public interface AccountValueStreamPort {

    StreamSubscription subscribe(String tenantId);

    void publish(AccountValueDashboardView view);

    interface StreamSubscription {
        Object emitterHandle();
    }
}
