package com.ledger.valuation.infrastructure.readside;

import com.ledger.valuation.application.port.outbound.AccountValueReadModelPort;
import com.ledger.valuation.application.readmodel.AccountValueDashboardView;
import com.ledger.valuation.domain.PortfolioLedgerEvent;
import com.ledger.valuation.infrastructure.platform.ShardRoutingService;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class JdbcAccountValueReadModelStore implements AccountValueReadModelPort {

    private static final String UPSERT_SQL = """
            UPSERT INTO account_value_read_model (
                portfolio_id, account_code, tenant_id, currency, account_value_minor_units,
                last_sequence_number, last_updated_at, shard_id
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String FIND_BY_PORTFOLIO_SQL = """
            SELECT portfolio_id, account_code, tenant_id, currency, account_value_minor_units,
                   last_sequence_number, last_updated_at
            FROM account_value_read_model WHERE portfolio_id = ?
            """;

    private static final String FIND_BY_ACCOUNT_SQL = """
            SELECT portfolio_id, account_code, tenant_id, currency, account_value_minor_units,
                   last_sequence_number, last_updated_at
            FROM account_value_read_model WHERE account_code = ?
            """;

    private static final String FIND_ALL_SQL = """
            SELECT portfolio_id, account_code, tenant_id, currency, account_value_minor_units,
                   last_sequence_number, last_updated_at
            FROM account_value_read_model ORDER BY account_code
            """;

    private final DataSource dataSource;
    private final ShardRoutingService shardRoutingService;

    public JdbcAccountValueReadModelStore(DataSource dataSource, ShardRoutingService shardRoutingService) {
        this.dataSource = dataSource;
        this.shardRoutingService = shardRoutingService;
    }

    @Override
    public void project(PortfolioLedgerEvent event) {
        throw new UnsupportedOperationException("JdbcAccountValueReadModelStore is read-only; use L1 projector");
    }

    @Override
    public Optional<AccountValueDashboardView> findByAccountCode(String accountCode) {
        return querySingle(FIND_BY_ACCOUNT_SQL, accountCode);
    }

    @Override
    public Optional<AccountValueDashboardView> findByPortfolioId(UUID portfolioId) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_PORTFOLIO_SQL)) {
            statement.setObject(1, portfolioId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to find read model by portfolio " + portfolioId, ex);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    @Override
    public Collection<AccountValueDashboardView> findAll() {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = statement.executeQuery()) {
            List<AccountValueDashboardView> views = new ArrayList<>();
            while (rs.next()) {
                views.add(mapRow(rs));
            }
            return views;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to list federated read model", ex);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    public void upsert(AccountValueDashboardView view) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(UPSERT_SQL)) {
            statement.setObject(1, view.portfolioId());
            statement.setString(2, view.accountCode());
            statement.setString(3, view.tenantId());
            statement.setString(4, view.currency());
            statement.setLong(5, view.accountValueMinorUnits());
            statement.setLong(6, view.lastSequenceNumber());
            statement.setTimestamp(7, Timestamp.from(view.lastUpdatedAt()));
            statement.setInt(8, shardRoutingService.resolveShard(view.portfolioId()));
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to upsert read model for " + view.portfolioId(), ex);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private Optional<AccountValueDashboardView> querySingle(String sql, String accountCode) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, accountCode);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to find read model by account " + accountCode, ex);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private static AccountValueDashboardView mapRow(ResultSet rs) throws SQLException {
        return new AccountValueDashboardView(
                rs.getObject("portfolio_id", UUID.class),
                rs.getString("account_code"),
                rs.getString("tenant_id"),
                rs.getString("currency"),
                rs.getLong("account_value_minor_units"),
                rs.getLong("last_sequence_number"),
                rs.getTimestamp("last_updated_at").toInstant()
        );
    }
}
