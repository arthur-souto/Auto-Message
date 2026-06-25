
CREATE TABLE asset_favorites (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    asset_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_asset_favorites_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_asset_favorites_asset
        FOREIGN KEY (asset_id) REFERENCES assets (id) ON DELETE CASCADE,

    CONSTRAINT uq_asset_favorites UNIQUE (user_id, asset_id)

);

CREATE INDEX idx_asset_favorites_user_id ON asset_favorites (user_id);