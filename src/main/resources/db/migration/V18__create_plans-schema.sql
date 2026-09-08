CREATE TABLE plans (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type           VARCHAR(30) NOT NULL,
    name           VARCHAR(255) NOT NULL,
    max_formulas   INTEGER,
    max_assets     INTEGER,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_plans_type UNIQUE (type)
);

CREATE TABLE user_plans (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id        UUID NOT NULL,
    plan_id        UUID NOT NULL,
    status         VARCHAR(20) NOT NULL,
    started_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at     TIMESTAMPTZ,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_user_plans_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_plans_plan
        FOREIGN KEY (plan_id) REFERENCES plans (id),
    CONSTRAINT uq_user_plans_user
        UNIQUE (user_id)
);

INSERT INTO plans (type, name, max_formulas, max_assets) VALUES
    ('FREE', 'Gratuito', 5, 20),
    ('PRO', 'Profissional', 100, 500),
    ('ENTERPRISE', 'Empresarial', NULL, NULL);
