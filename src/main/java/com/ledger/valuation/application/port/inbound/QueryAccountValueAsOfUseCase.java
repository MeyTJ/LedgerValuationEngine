package com.ledger.valuation.application.port.inbound;

import com.ledger.valuation.application.readmodel.AccountValueAsOfView;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface QueryAccountValueAsOfUseCase {

    Optional<AccountValueAsOfView> getAsOf(UUID portfolioId, Instant asOf);
}
