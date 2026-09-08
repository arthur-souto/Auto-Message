CREATE TABLE formulas (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_formulas_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_formulas_user_id ON formulas (user_id);

CREATE TABLE formula_items (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    formula_id    UUID NOT NULL,
    asset_id      UUID NOT NULL,
    quantity      NUMERIC(15, 4) NOT NULL,
    unit          VARCHAR(20) NOT NULL,
    concentration NUMERIC(10, 4),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_formula_items_formula
        FOREIGN KEY (formula_id) REFERENCES formulas (id) ON DELETE CASCADE,
    CONSTRAINT fk_formula_items_asset
        FOREIGN KEY (asset_id) REFERENCES assets (id) ON DELETE RESTRICT
);

CREATE INDEX idx_formula_items_formula_id ON formula_items (formula_id);
CREATE INDEX idx_formula_items_asset_id   ON formula_items (asset_id);

CREATE TABLE asset_incompatibilities (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    asset_a_id UUID NOT NULL,
    asset_b_id UUID NOT NULL,
    reason     TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_asset_incompatibilities_a
        FOREIGN KEY (asset_a_id) REFERENCES assets (id) ON DELETE CASCADE,
    CONSTRAINT fk_asset_incompatibilities_b
        FOREIGN KEY (asset_b_id) REFERENCES assets (id) ON DELETE CASCADE,
    CONSTRAINT chk_asset_incompatibilities_distinct
        CHECK (asset_a_id <> asset_b_id),
    CONSTRAINT uq_asset_incompatibilities_pair
        UNIQUE (asset_a_id, asset_b_id)
);

CREATE INDEX idx_asset_incompatibilities_a ON asset_incompatibilities (asset_a_id);
CREATE INDEX idx_asset_incompatibilities_b ON asset_incompatibilities (asset_b_id);
