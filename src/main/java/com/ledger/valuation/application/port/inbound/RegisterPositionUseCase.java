package com.ledger.valuation.application.port.inbound;

import com.ledger.valuation.domain.RegisterPositionCommand;

import java.util.UUID;

public interface RegisterPositionUseCase {

    UUID handle(RegisterPositionCommand command);
}
