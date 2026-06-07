package com.ledger.valuation.infrastructure.readside;

import com.ledger.valuation.application.port.outbound.InstrumentPositionRegistryPort;
import com.ledger.valuation.domain.InstrumentPosition;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
@Primary
public class JdbcInstrumentPositionRegistry implements InstrumentPositionRegistryPort {

    private static final String UPSERT_SQL = """
            UPSERT INTO instrument_position (
                portfolio_id, instrument_id, quantity_minor_units,
                cost_basis_minor_units, last_mark_price_minor_units
            ) VALUES (?, ?, ?, ?, ?)
            """;

    private static final String FIND_SQL = """
            SELECT instrument_id, quantity_minor_units, cost_basis_minor_units, last_mark_price_minor_units
            FROM instrument_position WHERE portfolio_id = ? AND instrument_id = ?
            """;

    private static final String UPDATE_MARK_SQL = """
            UPDATE instrument_position SET last_mark_price_minor_units = ?
            WHERE portfolio_id = ? AND instrument_id = ?
            """;

    private static final String FIND_PORTFOLIOS_SQL = """
            SELECT portfolio_id FROM instrument_position WHERE instrument_id = ?
            """;

    private final DataSource dataSource;

    public JdbcInstrumentPositionRegistry(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void register(UUID portfolioId, InstrumentPosition position) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(UPSERT_SQL)) {
            statement.setObject(1, portfolioId);
            statement.setString(2, position.instrumentId());
            statement.setLong(3, position.quantityMinorUnits());
            statement.setLong(4, position.costBasisMinorUnits());
            statement.setLong(5, position.lastMarkPriceMinorUnits());
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to register position", ex);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    @Override
    public Optional<InstrumentPosition> find(UUID portfolioId, String instrumentId) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(FIND_SQL)) {
            statement.setObject(1, portfolioId);
            statement.setString(2, instrumentId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new InstrumentPosition(
                        rs.getString("instrument_id"),
                        rs.getLong("quantity_minor_units"),
                        rs.getLong("cost_basis_minor_units"),
                        rs.getLong("last_mark_price_minor_units")
                ));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to find position", ex);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    @Override
    public void updateMark(UUID portfolioId, String instrumentId, long markPriceMinorUnits) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_MARK_SQL)) {
            statement.setLong(1, markPriceMinorUnits);
            statement.setObject(2, portfolioId);
            statement.setString(3, instrumentId);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to update mark price", ex);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    @Override
    public Set<UUID> findPortfoliosByInstrument(String instrumentId) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(FIND_PORTFOLIOS_SQL)) {
            statement.setString(1, instrumentId);
            try (ResultSet rs = statement.executeQuery()) {
                Set<UUID> portfolioIds = new HashSet<>();
                while (rs.next()) {
                    portfolioIds.add(rs.getObject("portfolio_id", UUID.class));
                }
                return portfolioIds;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to find portfolios by instrument", ex);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }
}
