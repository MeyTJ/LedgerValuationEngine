package com.ledger.valuation.infrastructure.persistence;

import com.ledger.valuation.application.port.outbound.TenantPolicyPort;
import com.ledger.valuation.domain.PolicyRuleType;
import com.ledger.valuation.domain.TenantPolicy;
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

@Component
public class JdbcTenantPolicyStore implements TenantPolicyPort {

    private static final String FIND_BY_TENANT_SQL = """
            SELECT tenant_id, rule_type, threshold_minor_units, effective_from
            FROM tenant_policy
            WHERE tenant_id = ?
            """;

    private static final String UPSERT_SQL = """
            UPSERT INTO tenant_policy (tenant_id, rule_type, threshold_minor_units, effective_from)
            VALUES (?, ?, ?, ?)
            """;

    private static final String DELETE_SQL = """
            DELETE FROM tenant_policy WHERE tenant_id = ? AND rule_type = ?
            """;

    private final DataSource dataSource;

    public JdbcTenantPolicyStore(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<TenantPolicy> findByTenant(String tenantId) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_TENANT_SQL)) {
            statement.setString(1, tenantId);
            try (ResultSet rs = statement.executeQuery()) {
                var policies = new ArrayList<TenantPolicy>();
                while (rs.next()) {
                    policies.add(mapRow(rs));
                }
                return policies;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load tenant policies", ex);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    @Override
    public void upsert(TenantPolicy policy) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(UPSERT_SQL)) {
            statement.setString(1, policy.tenantId());
            statement.setString(2, policy.ruleType().name());
            statement.setLong(3, policy.thresholdMinorUnits());
            statement.setTimestamp(4, Timestamp.from(policy.effectiveFrom()));
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to upsert tenant policy", ex);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    @Override
    public void delete(String tenantId, PolicyRuleType ruleType) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setString(1, tenantId);
            statement.setString(2, ruleType.name());
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to delete tenant policy", ex);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private static TenantPolicy mapRow(ResultSet rs) throws SQLException {
        return new TenantPolicy(
                rs.getString("tenant_id"),
                PolicyRuleType.valueOf(rs.getString("rule_type")),
                rs.getLong("threshold_minor_units"),
                rs.getTimestamp("effective_from").toInstant()
        );
    }
}
