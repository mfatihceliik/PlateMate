-- Admin-managed lookup table for Discover screen tab chips (code/label/sort/active).
-- Rows are seeded via DiscoveryTabOptionSeedConfig (ApplicationRunner), not here, matching
-- the plate_report_types precedent for IDENTITY-PK lookup tables.
CREATE TABLE IF NOT EXISTS discovery_tab_options (
    id          BIGSERIAL    PRIMARY KEY,
    code        VARCHAR(64)  NOT NULL UNIQUE,
    label       VARCHAR(128) NOT NULL,
    sort_order  INTEGER      NOT NULL,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_discovery_tab_options_active_sort ON discovery_tab_options(active, sort_order);
