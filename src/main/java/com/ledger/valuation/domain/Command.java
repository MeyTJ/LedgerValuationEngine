package com.ledger.valuation.domain;

import java.util.UUID;

public sealed interface Command permits
        Command.OpenAccount,
        Command.PostTransaction,
        Command.ValueAccount {

    UUID correlationId();

    record OpenAccount(UUID correlationId, String accountCode, String currency) implements Command {}

    record PostTransaction(
            UUID correlationId,
            String debitAccount,
            String creditAccount,
            long amountMinorUnits
    ) implements Command {}

    record ValueAccount(UUID correlationId, String accountCode) implements Command {}
}
