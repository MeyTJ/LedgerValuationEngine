ALTER TABLE event_store ADD COLUMN IF NOT EXISTS idempotency_token STRING;

CREATE UNIQUE INDEX IF NOT EXISTS idx_event_store_idempotency_token
    ON event_store (idempotency_token)
    WHERE idempotency_token IS NOT NULL;
