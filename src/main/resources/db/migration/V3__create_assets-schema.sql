CREATE TABLE assets (
                        id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        code        VARCHAR(20)  NOT NULL UNIQUE,
                        name        VARCHAR(255) NOT NULL,
                        supplier    VARCHAR(255),
                        unit        VARCHAR(20),
                        created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
                        updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE asset_batches (
                               id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               asset_id    UUID NOT NULL,
                               batch       VARCHAR(100),
                               expires_at  DATE,
                               quantity    NUMERIC(15, 4),
                               unit_cost   NUMERIC(15, 8),
                               total       NUMERIC(15, 4),
                               created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
                               CONSTRAINT fk_asset_batches_asset
                                   FOREIGN KEY (asset_id) REFERENCES assets (id)
                                       ON DELETE CASCADE
);

CREATE UNIQUE INDEX idx_assets_code         ON assets (code);
CREATE INDEX idx_assets_name                ON assets (name);
CREATE INDEX idx_asset_batches_asset_id     ON asset_batches (asset_id);
CREATE INDEX idx_asset_batches_expires_at   ON asset_batches (expires_at);