package com.ledger.valuation.infrastructure.persistence;

import com.ledger.valuation.application.port.outbound.CommandIdempotencyPort;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

@Component
public class JdbcCommandIdempotencyStore implements CommandIdempotencyPort {

    private static final String FIND_SQL = """
            SELECT idempotency_token, command_type, aggregate_id, result_event_id
            FROM command_idempotency WHERE idempotency_token = ?
            """;

    private static final String INSERT_SQL = """
            INSERT INTO command_idempotency (idempotency_token, command_type, aggregate_id, result_event_id)
            VALUES (?, ?, ?, ?)
            """;

    private final DataSource dataSource;

    public JdbcCommandIdempotencyStore(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<IdempotencyRecord> findByToken(String idempotencyToken) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(FIND_SQL)) {
            statement.setString(1, idempotencyToken);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new IdempotencyRecord(
                        rs.getString("idempotency_token"),
                        rs.getString("command_type"),
                        rs.getObject("aggregate_id", UUID.class),
                        rs.getObject("result_event_id", UUID.class)
                ));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to resolve command idempotency token", ex);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    @Override
    public void record(String idempotencyToken, String commandType, UUID aggregateId, UUID resultEventId) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setString(1, idempotencyToken);
            statement.setString(2, commandType);
            statement.setObject(3, aggregateId);
            statement.setObject(4, resultEventId);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to record command idempotency", ex);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }
}
