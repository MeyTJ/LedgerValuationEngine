ALTER TABLE account_value_read_model ADD COLUMN IF NOT EXISTS tenant_id STRING NOT NULL DEFAULT 'default';

CREATE INDEX IF NOT EXISTS idx_read_model_tenant ON account_value_read_model (tenant_id);
