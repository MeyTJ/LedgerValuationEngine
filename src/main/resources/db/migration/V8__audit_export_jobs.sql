CREATE TABLE IF NOT EXISTS audit_export_job (
    id UUID PRIMARY KEY,
    portfolio_id UUID NOT NULL,
    tenant_id STRING NOT NULL,
    from_sequence BIGINT NOT NULL,
    to_sequence BIGINT NOT NULL,
    status STRING NOT NULL,
    storage_path STRING,
    manifest_checksum STRING,
    event_count INT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_audit_export_job_status ON audit_export_job (status);
