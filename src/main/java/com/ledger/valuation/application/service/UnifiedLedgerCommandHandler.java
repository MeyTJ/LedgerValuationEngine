package com.ledger.valuation.application.service;

import com.ledger.valuation.application.port.inbound.CommitTransactionUseCase;
import com.ledger.valuation.application.port.inbound.OpenPortfolioUseCase;
import com.ledger.valuation.application.port.inbound.ProcessCommandUseCase;
import com.ledger.valuation.domain.Command;
import com.ledger.valuation.domain.CommitTransactionCommand;
import com.ledger.valuation.domain.OpenPortfolioCommand;

import java.util.UUID;

public final class UnifiedLedgerCommandHandler implements ProcessCommandUseCase {

    private final OpenPortfolioUseCase openPortfolioUseCase;
    private final CommitTransactionUseCase commitTransactionUseCase;

    public UnifiedLedgerCommandHandler(
            OpenPortfolioUseCase openPortfolioUseCase,
            CommitTransactionUseCase commitTransactionUseCase
    ) {
        this.openPortfolioUseCase = openPortfolioUseCase;
        this.commitTransactionUseCase = commitTransactionUseCase;
    }

    @Override
    public void process(Command command) {
        switch (command) {
            case Command.OpenAccount open -> openPortfolioUseCase.handle(new OpenPortfolioCommand(
                    open.correlationId().toString(),
                    UUID.nameUUIDFromBytes(open.accountCode().getBytes()),
                    open.accountCode(),
                    open.currency(),
                    "default"
            ));
            case Command.PostTransaction post -> commitTransactionUseCase.handle(new CommitTransactionCommand(
                    post.correlationId().toString(),
                    UUID.nameUUIDFromBytes(post.debitAccount().getBytes()),
                    0L,
                    post.amountMinorUnits(),
                    post.correlationId().toString()
            ));
            case Command.ValueAccount value -> { /* valuation triggered via market ticks */ }
        }
    }
}
