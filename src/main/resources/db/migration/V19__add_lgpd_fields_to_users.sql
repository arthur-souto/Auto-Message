ALTER TABLE users ADD COLUMN accepted_terms_at TIMESTAMPTZ;
ALTER TABLE users ADD COLUMN accepted_terms_version VARCHAR(20);
ALTER TABLE users ADD COLUMN deleted_at TIMESTAMPTZ;
