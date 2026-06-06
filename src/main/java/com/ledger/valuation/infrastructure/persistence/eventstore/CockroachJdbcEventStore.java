package com.ledger.valuation.infrastructure.persistence.eventstore;

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
import java.util.Optional;
import java.util.UUID;

@Component
public class CockroachJdbcEventStore implements EventStore {

    static final String APPEND_EVENT_SQL = """
            INSERT INTO event_store (
                id,
                aggregate_id,
                sequence_number,
                event_type,
                payload,
                occurred_at,
                idempotency_token
            ) VALUES (?, ?, ?, ?, ?::JSONB, ?, ?)
            """;

    static final String LOAD_BY_AGGREGATE_SQL = """
            SELECT id, aggregate_id, sequence_number, event_type, payload, occurred_at, idempotency_token
            FROM event_store
            WHERE aggregate_id = ?
            ORDER BY sequence_number ASC
            """;

    static final String FIND_BY_IDEMPOTENCY_TOKEN_SQL = """
            SELECT id, aggregate_id, sequence_number, event_type, payload, occurred_at, idempotency_token
            FROM event_store
            WHERE idempotency_token = ?
            """;

    static final String EXISTS_BY_AGGREGATE_AND_SEQUENCE_SQL = """
            SELECT 1
            FROM event_store
            WHERE aggregate_id = ? AND sequence_number = ?
            LIMIT 1
            """;

    static final String LIST_AGGREGATE_IDS_SQL = """
            SELECT DISTINCT aggregate_id
            FROM event_store
            ORDER BY aggregate_id
            """;

    private final DataSource dataSource;

    public CockroachJdbcEventStore(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void append(StoredEventRecord record) {
        EventStoreSqlGuard.assertAppendOnlyInsert(APPEND_EVENT_SQL);

        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            enforceSerializableIsolation(connection);
            try (PreparedStatement statement = connection.prepareStatement(APPEND_EVENT_SQL)) {
                bindAppend(statement, record);
                statement.executeUpdate();
            }
        } catch (SQLException ex) {
            throw mapSqlException(ex, "Failed to append event " + record.id());
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    @Override
    public List<StoredEventRecord> loadByAggregateId(UUID aggregateId) {
        EventStoreSqlGuard.assertReadOnlySelect(LOAD_BY_AGGREGATE_SQL);

        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            enforceSerializableIsolation(connection);
            try (PreparedStatement statement = connection.prepareStatement(LOAD_BY_AGGREGATE_SQL)) {
                statement.setObject(1, aggregateId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    var records = new ArrayList<StoredEventRecord>();
                    while (resultSet.next()) {
                        records.add(mapRow(resultSet));
                    }
                    return records;
                }
            }
        } catch (SQLException ex) {
            throw mapSqlException(ex, "Failed to load event stream for aggregate " + aggregateId);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    @Override
    public Optional<StoredEventRecord> findByIdempotencyToken(String idempotencyToken) {
        EventStoreSqlGuard.assertReadOnlySelect(FIND_BY_IDEMPOTENCY_TOKEN_SQL);

        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            enforceSerializableIsolation(connection);
            try (PreparedStatement statement = connection.prepareStatement(FIND_BY_IDEMPOTENCY_TOKEN_SQL)) {
                statement.setString(1, idempotencyToken);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        return Optional.empty();
                    }
                    return Optional.of(mapRow(resultSet));
                }
            }
        } catch (SQLException ex) {
            throw mapSqlException(ex, "Failed to resolve idempotency token");
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    @Override
    public List<UUID> listAggregateIds() {
        EventStoreSqlGuard.assertReadOnlySelect(LIST_AGGREGATE_IDS_SQL);
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            enforceSerializableIsolation(connection);
            try (PreparedStatement statement = connection.prepareStatement(LIST_AGGREGATE_IDS_SQL);
                 ResultSet resultSet = statement.executeQuery()) {
                var ids = new ArrayList<UUID>();
                while (resultSet.next()) {
                    ids.add(resultSet.getObject("aggregate_id", UUID.class));
                }
                return ids;
            }
        } catch (SQLException ex) {
            throw mapSqlException(ex, "Failed to list aggregate ids");
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    @Override
    public boolean existsByAggregateIdAndSequenceNumber(UUID aggregateId, long sequenceNumber) {
        EventStoreSqlGuard.assertReadOnlySelect(EXISTS_BY_AGGREGATE_AND_SEQUENCE_SQL);

        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            enforceSerializableIsolation(connection);
            try (PreparedStatement statement = connection.prepareStatement(EXISTS_BY_AGGREGATE_AND_SEQUENCE_SQL)) {
                statement.setObject(1, aggregateId);
                statement.setLong(2, sequenceNumber);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (SQLException ex) {
            throw mapSqlException(
                    ex,
                    "Failed to verify sequence for aggregate " + aggregateId + " at " + sequenceNumber
            );
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private static void enforceSerializableIsolation(Connection connection) throws SQLException {
        if (connection.getTransactionIsolation() != Connection.TRANSACTION_SERIALIZABLE) {
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        }
    }

    private static void bindAppend(PreparedStatement statement, StoredEventRecord record) throws SQLException {
        statement.setObject(1, record.id());
        statement.setObject(2, record.aggregateId());
        statement.setLong(3, record.sequenceNumber());
        statement.setString(4, record.eventType());
        statement.setString(5, record.payload());
        statement.setTimestamp(6, Timestamp.from(record.occurredAt()));
        if (record.idempotencyToken() == null) {
            statement.setNull(7, java.sql.Types.VARCHAR);
        } else {
            statement.setString(7, record.idempotencyToken());
        }
    }

    private static StoredEventRecord mapRow(ResultSet resultSet) throws SQLException {
        return new StoredEventRecord(
                resultSet.getObject("id", UUID.class),
                resultSet.getObject("aggregate_id", UUID.class),
                resultSet.getLong("sequence_number"),
                resultSet.getString("event_type"),
                resultSet.getString("payload"),
                resultSet.getTimestamp("occurred_at").toInstant(),
                resultSet.getString("idempotency_token")
        );
    }

    static boolean isSerializationConflict(SQLException ex) {
        for (SQLException current = ex; current != null; current = current.getNextException()) {
            if ("40001".equals(current.getSQLState())) {
                return true;
            }
        }
        return false;
    }

    private static RuntimeException mapSqlException(SQLException ex, String message) {
        if (isImmutabilityViolation(ex)) {
            return new ImmutableLedgerViolationException(
                    "event_store immutability constraint violated: " + ex.getMessage(),
                    ex
            );
        }
        if (isSerializationConflict(ex)) {
            return new CockroachSerializationConflictException(message, ex);
        }
        return new IllegalStateException(message, ex);
    }

    private static boolean isImmutabilityViolation(SQLException ex) {
        for (SQLException current = ex; current != null; current = current.getNextException()) {
            if (current.getMessage() != null && current.getMessage().contains("event_store is append-only")) {
                return true;
            }
        }
        return false;
    }
}
