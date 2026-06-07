package com.ledger.valuation.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.valuation.application.port.outbound.OutboxPort;
import com.ledger.valuation.domain.PortfolioLedgerEvent;
import com.ledger.valuation.infrastructure.config.LedgerProperties;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class JdbcOutboxStore implements OutboxPort {

    private static final String ENQUEUE_SQL = """
            INSERT INTO outbox (id, aggregate_id, event_type, payload)
            VALUES (?, ?, ?, ?::JSONB)
            """;

    private static final String CLAIM_PENDING_SQL = """
            UPDATE outbox
            SET claimed_at = now(), claimed_by = ?
            WHERE id IN (
                SELECT id FROM outbox
                WHERE published_at IS NULL
                  AND failed_at IS NULL
                  AND (claimed_at IS NULL OR claimed_at < ?)
                ORDER BY created_at
                LIMIT ?
                FOR UPDATE SKIP LOCKED
            )
            RETURNING id, aggregate_id, event_type, payload
            """;

    private static final String MARK_PUBLISHED_SQL = """
            UPDATE outbox SET published_at = now(), claimed_at = NULL, claimed_by = NULL WHERE id = ?
            """;

    private static final String INCREMENT_RETRY_SQL = """
            UPDATE outbox SET retry_count = retry_count + 1, claimed_at = NULL, claimed_by = NULL WHERE id = ?
            """;

    private static final String MARK_FAILED_SQL = """
            UPDATE outbox SET failed_at = now(), claimed_at = NULL, claimed_by = NULL WHERE id = ?
            """;

    private static final String COUNT_PENDING_SQL = """
            SELECT COUNT(*) FROM outbox WHERE published_at IS NULL AND failed_at IS NULL
            """;

    private final DataSource dataSource;
    private final ObjectMapper objectMapper;
    private final int maxRetries;
    private final java.time.Duration claimTimeout;

    public JdbcOutboxStore(
            DataSource dataSource,
            ObjectMapper objectMapper,
            LedgerProperties ledgerProperties
    ) {
        this.dataSource = dataSource;
        this.objectMapper = objectMapper;
        this.maxRetries = ledgerProperties.outbox().maxRetries();
        this.claimTimeout = ledgerProperties.outbox().claimTimeout();
    }

    @Override
    public void enqueue(PortfolioLedgerEvent event) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(ENQUEUE_SQL)) {
            statement.setObject(1, UUID.randomUUID());
            statement.setObject(2, event.portfolioId());
            statement.setString(3, event.getClass().getSimpleName());
            statement.setString(4, serialize(event));
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to enqueue outbox event", ex);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    @Override
    public List<OutboxEntry> claimPending(int limit, String claimedBy) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(CLAIM_PENDING_SQL)) {
            statement.setString(1, claimedBy);
            statement.setTimestamp(2, Timestamp.from(Instant.now().minus(claimTimeout)));
            statement.setInt(3, limit);
            try (ResultSet rs = statement.executeQuery()) {
                var entries = new ArrayList<OutboxEntry>();
                while (rs.next()) {
                    entries.add(new OutboxEntry(
                            rs.getObject("id", UUID.class),
                            rs.getObject("aggregate_id", UUID.class),
                            rs.getString("event_type"),
                            rs.getString("payload")
                    ));
                }
                return entries;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to claim pending outbox entries", ex);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    @Override
    public void markPublished(UUID outboxId) {
        executeUpdate(MARK_PUBLISHED_SQL, outboxId);
    }

    @Override
    public void incrementRetry(UUID outboxId) {
        executeUpdate(INCREMENT_RETRY_SQL, outboxId);
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT retry_count FROM outbox WHERE id = ?"
        )) {
            statement.setObject(1, outboxId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next() && rs.getInt("retry_count") >= maxRetries) {
                    markFailed(outboxId);
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to check retry count for outbox " + outboxId, ex);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    @Override
    public void markFailed(UUID outboxId) {
        executeUpdate(MARK_FAILED_SQL, outboxId);
    }

    @Override
    public long countPending() {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(COUNT_PENDING_SQL);
             ResultSet rs = statement.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to count pending outbox entries", ex);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void executeUpdate(String sql, UUID id) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to update outbox entry " + id, ex);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private String serialize(PortfolioLedgerEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Unable to serialize outbox event", ex);
        }
    }
}
