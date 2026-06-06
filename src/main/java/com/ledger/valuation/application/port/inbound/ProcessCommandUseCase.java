package com.ledger.valuation.application.port.inbound;

import com.ledger.valuation.domain.Command;

public interface ProcessCommandUseCase {

    void process(Command command);
}
