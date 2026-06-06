CREATE INDEX IF NOT EXISTS idx_event_store_aggregate_occurred
    ON event_store (aggregate_id, occurred_at);

CREATE TABLE IF NOT EXISTS instrument_position (
    portfolio_id UUID NOT NULL,
    instrument_id STRING NOT NULL,
    quantity_minor_units BIGINT NOT NULL,
    cost_basis_minor_units BIGINT NOT NULL,
    last_mark_price_minor_units BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (portfolio_id, instrument_id)
);

CREATE INDEX IF NOT EXISTS idx_instrument_position_instrument
    ON instrument_position (instrument_id);
