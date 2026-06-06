package com.ledger.valuation.application.port.inbound;

import com.ledger.valuation.application.model.CommitTransactionResult;
import com.ledger.valuation.domain.CommitTransactionCommand;

public interface CommitTransactionUseCase {

    CommitTransactionResult handle(CommitTransactionCommand command);
}
