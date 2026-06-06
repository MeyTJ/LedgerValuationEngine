package com.ledger.valuation.infrastructure.persistence;

import com.ledger.valuation.application.port.outbound.AuditExportJobPort;
import com.ledger.valuation.domain.AuditExportJob;
import com.ledger.valuation.domain.AuditExportJobStatus;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class JdbcAuditExportJobStore implements AuditExportJobPort {

    private static final String INSERT_SQL = """
            INSERT INTO audit_export_job (
                id, portfolio_id, tenant_id, from_sequence, to_sequence, status, created_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String FIND_BY_ID_SQL = """
            SELECT id, portfolio_id, tenant_id, from_sequence, to_sequence, status,
                   storage_path, manifest_checksum, event_count, created_at, completed_at
            FROM audit_export_job WHERE id = ?
            """;

    private static final String FIND_BY_STATUS_SQL = """
            SELECT id, portfolio_id, tenant_id, from_sequence, to_sequence, status,
                   storage_path, manifest_checksum, event_count, created_at, completed_at
            FROM audit_export_job WHERE status = ? ORDER BY created_at LIMIT ?
            """;

    private static final String UPDATE_STATUS_SQL = """
            UPDATE audit_export_job
            SET status = ?, storage_path = ?, manifest_checksum = ?, event_count = ?, completed_at = now()
            WHERE id = ?
            """;

    private final DataSource dataSource;

    public JdbcAuditExportJobStore(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(AuditExportJob job) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setObject(1, job.id());
            statement.setObject(2, job.portfolioId());
            statement.setString(3, job.tenantId());
            statement.setLong(4, job.fromSequence());
            statement.setLong(5, job.toSequence());
            statement.setString(6, job.status().name());
            statement.setTimestamp(7, Timestamp.from(job.createdAt()));
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to save audit export job", ex);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    @Override
    public Optional<AuditExportJob> findById(UUID id) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setObject(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to find audit export job", ex);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    @Override
    public List<AuditExportJob> findByStatus(AuditExportJobStatus status, int limit) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_STATUS_SQL)) {
            statement.setString(1, status.name());
            statement.setInt(2, limit);
            try (ResultSet rs = statement.executeQuery()) {
                var jobs = new ArrayList<AuditExportJob>();
                while (rs.next()) {
                    jobs.add(mapRow(rs));
                }
                return jobs;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to find audit export jobs", ex);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    @Override
    public void updateStatus(
            UUID id,
            AuditExportJobStatus status,
            String storagePath,
            String manifestChecksum,
            int eventCount
    ) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_STATUS_SQL)) {
            statement.setString(1, status.name());
            statement.setString(2, storagePath);
            statement.setString(3, manifestChecksum);
            statement.setInt(4, eventCount);
            statement.setObject(5, id);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to update audit export job", ex);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private static AuditExportJob mapRow(ResultSet rs) throws SQLException {
        Timestamp completedAt = rs.getTimestamp("completed_at");
        return new AuditExportJob(
                rs.getObject("id", UUID.class),
                rs.getObject("portfolio_id", UUID.class),
                rs.getString("tenant_id"),
                rs.getLong("from_sequence"),
                rs.getLong("to_sequence"),
                AuditExportJobStatus.valueOf(rs.getString("status")),
                rs.getString("storage_path"),
                rs.getString("manifest_checksum"),
                rs.getInt("event_count"),
                rs.getTimestamp("created_at").toInstant(),
                completedAt == null ? null : completedAt.toInstant()
        );
    }
}
