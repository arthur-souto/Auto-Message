CREATE TABLE patients (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL,
    name       VARCHAR(255) NOT NULL,
    document   VARCHAR(20) NOT NULL,
    birth_date DATE,
    phone      VARCHAR(30),
    email      VARCHAR(255),
    address    TEXT,
    notes      TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_patients_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_patients_user_document
        UNIQUE (user_id, document)
);

CREATE INDEX idx_patients_user_id ON patients (user_id);

ALTER TABLE formulas ADD COLUMN patient_id UUID;

ALTER TABLE formulas
    ADD CONSTRAINT fk_formulas_patient
        FOREIGN KEY (patient_id) REFERENCES patients (id) ON DELETE RESTRICT;

CREATE INDEX idx_formulas_patient_id ON formulas (patient_id);
