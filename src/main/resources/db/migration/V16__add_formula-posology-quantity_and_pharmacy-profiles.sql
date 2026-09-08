ALTER TABLE formulas ADD COLUMN posology TEXT;
ALTER TABLE formulas ADD COLUMN quantity VARCHAR(100);

CREATE TABLE pharmacy_profiles (
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                  UUID NOT NULL,
    pharmacy_name            VARCHAR(255),
    address                  TEXT,
    phone                    VARCHAR(30),
    email                    VARCHAR(255),
    responsible_name         VARCHAR(255),
    responsible_document     VARCHAR(20),
    responsible_registration VARCHAR(255),
    created_at               TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_pharmacy_profiles_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_pharmacy_profiles_user
        UNIQUE (user_id)
);
