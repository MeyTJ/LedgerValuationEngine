CREATE TABLE IF NOT EXISTS tenant_policy (
    tenant_id STRING NOT NULL,
    rule_type STRING NOT NULL,
    threshold_minor_units BIGINT NOT NULL,
    effective_from TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (tenant_id, rule_type)
);
