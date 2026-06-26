CREATE EXTENSION IF NOT EXISTS unaccent;

ALTER TABLE assets ADD COLUMN manufacturer   VARCHAR(255);
ALTER TABLE assets ADD COLUMN composition    TEXT;
ALTER TABLE assets ADD COLUMN dosage         VARCHAR(255);
ALTER TABLE assets ADD COLUMN mechanism      TEXT;
ALTER TABLE assets ADD COLUMN associations   TEXT;
ALTER TABLE assets ADD COLUMN pharma_forms   TEXT;
ALTER TABLE assets ADD COLUMN literature_url VARCHAR(500);
ALTER TABLE assets ADD COLUMN category       VARCHAR(100);
ALTER TABLE assets ADD COLUMN is_exclusive   BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE asset_indications (
                                   id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                   asset_id   UUID NOT NULL,
                                   indication VARCHAR(255) NOT NULL,
                                   created_at TIMESTAMP NOT NULL DEFAULT NOW(),

                                   CONSTRAINT fk_asset_indications_asset
                                       FOREIGN KEY (asset_id) REFERENCES assets (id) ON DELETE CASCADE
);

CREATE INDEX idx_asset_indications_asset_id   ON asset_indications (asset_id);
CREATE INDEX idx_asset_indications_indication ON asset_indications (indication);
CREATE INDEX idx_assets_category              ON assets (category);