package com.ledger.valuation.infrastructure.persistence.eventstore;

import java.util.regex.Pattern;

public final class EventStoreSqlGuard {

    static final String TABLE_NAME = "event_store";

    private static final Pattern FORBIDDEN_MUTATION = Pattern.compile(
            "\\b(UPDATE|DELETE|TRUNCATE|MERGE|REPLACE)\\s+" + TABLE_NAME + "\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern FORBIDDEN_UPSERT = Pattern.compile(
            "\\bINSERT\\s+INTO\\s+" + TABLE_NAME + "\\b[\\s\\S]*\\bON\\s+CONFLICT\\b[\\s\\S]*\\bDO\\s+(UPDATE|NOTHING)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private EventStoreSqlGuard() {}

    public static void assertAppendOnlyInsert(String sql) {
        String normalized = normalize(sql);
        if (!normalized.startsWith("INSERT INTO " + TABLE_NAME)) {
            throw new ImmutableLedgerViolationException(
                    "event_store accepts append-only INSERT operations; rejected SQL: " + sql
            );
        }
        assertNoHistoricalModification(normalized);
    }

    public static void assertReadOnlySelect(String sql) {
        String normalized = normalize(sql);
        if (!normalized.startsWith("SELECT ")) {
            throw new ImmutableLedgerViolationException(
                    "event_store read path accepts SELECT statements only; rejected SQL: " + sql
            );
        }
        assertNoHistoricalModification(normalized);
    }

    public static void assertNoHistoricalModification(String sql) {
        String normalized = normalize(sql);
        if (FORBIDDEN_MUTATION.matcher(normalized).find()) {
            throw new ImmutableLedgerViolationException(
                    "Historical modification of event_store is prohibited; rejected SQL: " + sql
            );
        }
        if (FORBIDDEN_UPSERT.matcher(normalized).find()) {
            throw new ImmutableLedgerViolationException(
                    "Upsert operations on event_store are prohibited; rejected SQL: " + sql
            );
        }
    }

    private static String normalize(String sql) {
        return sql.strip().replaceAll("\\s+", " ");
    }
}
