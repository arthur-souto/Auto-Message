
CREATE TABLE refresh_tokens (
                                id               UUID PRIMARY KEY,
                                token_hash       VARCHAR(255) NOT NULL,
                                user_id          UUID NOT NULL,
                                created_at       TIMESTAMP NOT NULL,
                                expires_at       TIMESTAMP NOT NULL,
                                revoked          BOOLEAN NOT NULL DEFAULT FALSE,
                                user_agent       VARCHAR(255),
                                ip_address       VARCHAR(45),

                                CONSTRAINT fk_refresh_tokens_user
                                    FOREIGN KEY (user_id) REFERENCES users (id)
                                        ON DELETE CASCADE
);

CREATE UNIQUE INDEX idx_refresh_token_hash ON refresh_tokens (token_hash);

CREATE INDEX idx_refresh_token_user ON refresh_tokens (user_id);

CREATE INDEX idx_refresh_token_expires_at ON refresh_tokens (expires_at);

