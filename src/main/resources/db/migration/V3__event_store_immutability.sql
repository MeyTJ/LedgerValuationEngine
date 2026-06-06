CREATE OR REPLACE FUNCTION prevent_event_store_mutation()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'event_store is append-only: % operations are prohibited', TG_OP;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_event_store_no_update
    BEFORE UPDATE ON event_store
    FOR EACH ROW
    EXECUTE FUNCTION prevent_event_store_mutation();

CREATE TRIGGER trg_event_store_no_delete
    BEFORE DELETE ON event_store
    FOR EACH ROW
    EXECUTE FUNCTION prevent_event_store_mutation();

REVOKE UPDATE, DELETE, TRUNCATE ON TABLE event_store FROM PUBLIC;
