CREATE TABLE doctors (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL,
    name       VARCHAR(255) NOT NULL,
    crm        VARCHAR(50) NOT NULL,
    specialty  VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_doctors_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_doctors_user_crm
        UNIQUE (user_id, crm)
);

CREATE INDEX idx_doctors_user_id ON doctors (user_id);

ALTER TABLE formulas ADD COLUMN doctor_id UUID;

ALTER TABLE formulas
    ADD CONSTRAINT fk_formulas_doctor
        FOREIGN KEY (doctor_id) REFERENCES doctors (id) ON DELETE RESTRICT;

CREATE INDEX idx_formulas_doctor_id ON formulas (doctor_id);
