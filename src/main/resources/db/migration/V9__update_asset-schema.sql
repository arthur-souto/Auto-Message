ALTER TABLE assets DROP COLUMN  associations;

CREATE TABLE asset_associations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    asset_id UUID NOT NULL,
    association VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_asset_associations_asset
                                FOREIGN KEY (asset_id) REFERENCES assets(id) ON DELETE CASCADE
);


CREATE INDEX idx_asset_associations_asset_id on asset_associations (asset_id);
CREATE INDEX idx_asset_associations_association on asset_associations (association);