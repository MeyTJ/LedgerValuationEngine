CREATE TABLE IF NOT EXISTS account_value_read_model (
    portfolio_id UUID PRIMARY KEY,
    account_code STRING NOT NULL,
    currency STRING NOT NULL,
    account_value_minor_units BIGINT NOT NULL,
    last_sequence_number BIGINT NOT NULL,
    last_updated_at TIMESTAMPTZ NOT NULL,
    shard_id INT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_read_model_account_code ON account_value_read_model (account_code);
CREATE INDEX IF NOT EXISTS idx_read_model_shard ON account_value_read_model (shard_id);

CREATE TABLE IF NOT EXISTS audit_export_manifest (
    id UUID PRIMARY KEY,
    portfolio_id UUID NOT NULL,
    from_sequence BIGINT NOT NULL,
    to_sequence BIGINT NOT NULL,
    event_count INT NOT NULL,
    checksum STRING NOT NULL,
    exported_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
