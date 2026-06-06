package com.ledger.valuation.infrastructure.readside;

import com.ledger.valuation.application.readmodel.AccountValueDashboardView;
import com.ledger.valuation.infrastructure.platform.ShardRoutingService;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

@Component
public class ReadModelFederationWriter {

    private static final String UPSERT_SQL = """
            UPSERT INTO account_value_read_model (
                portfolio_id, account_code, currency, account_value_minor_units,
                last_sequence_number, last_updated_at, shard_id
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    private final DataSource dataSource;
    private final ShardRoutingService shardRoutingService;

    public ReadModelFederationWriter(DataSource dataSource, ShardRoutingService shardRoutingService) {
        this.dataSource = dataSource;
        this.shardRoutingService = shardRoutingService;
    }

    public void federate(AccountValueDashboardView view) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(UPSERT_SQL)) {
            statement.setObject(1, view.portfolioId());
            statement.setString(2, view.accountCode());
            statement.setString(3, view.currency());
            statement.setLong(4, view.accountValueMinorUnits());
            statement.setLong(5, view.lastSequenceNumber());
            statement.setTimestamp(6, Timestamp.from(view.lastUpdatedAt()));
            statement.setInt(7, shardRoutingService.resolveShard(view.portfolioId()));
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to federate read model for " + view.portfolioId(), ex);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }
}
