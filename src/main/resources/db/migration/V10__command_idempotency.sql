CREATE TABLE IF NOT EXISTS command_idempotency (
    idempotency_token STRING PRIMARY KEY,
    command_type STRING NOT NULL,
    aggregate_id UUID NOT NULL,
    result_event_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_command_idempotency_aggregate ON command_idempotency (aggregate_id);
