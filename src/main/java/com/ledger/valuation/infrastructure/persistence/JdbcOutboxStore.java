package com.ledger.valuation.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.valuation.application.port.outbound.OutboxPort;
import com.ledger.valuation.domain.PortfolioLedgerEvent;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class JdbcOutboxStore implements OutboxPort {

    private static final String ENQUEUE_SQL = """
            INSERT INTO outbox (id, aggregate_id, event_type, payload)
            VALUES (?, ?, ?, ?::JSONB)
            """;

    private static final String FETCH_PENDING_SQL = """
            SELECT id, aggregate_id, event_type, payload
            FROM outbox
            WHERE published_at IS NULL
            ORDER BY created_at
            LIMIT ?
            """;

    private static final String MARK_PUBLISHED_SQL = """
            UPDATE outbox SET published_at = now() WHERE id = ?
            """;

    private static final String INCREMENT_RETRY_SQL = """
            UPDATE outbox SET retry_count = retry_count + 1 WHERE id = ?
            """;

    private final DataSource dataSource;
    private final ObjectMapper objectMapper;

    public JdbcOutboxStore(DataSource dataSource, ObjectMapper objectMapper) {
        this.dataSource = dataSource;
        this.objectMapper = objectMapper;
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
    public List<OutboxEntry> fetchPending(int limit) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(FETCH_PENDING_SQL)) {
            statement.setInt(1, limit);
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
            throw new IllegalStateException("Failed to fetch pending outbox entries", ex);
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
