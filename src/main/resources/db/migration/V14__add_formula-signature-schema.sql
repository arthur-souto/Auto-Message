ALTER TABLE formulas ADD COLUMN signed_pdf BYTEA;
ALTER TABLE formulas ADD COLUMN signed_at TIMESTAMPTZ;
ALTER TABLE formulas ADD COLUMN signed_by_certificate_subject TEXT;
