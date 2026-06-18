CREATE TABLE resumes (
                         id              BIGSERIAL PRIMARY KEY,
                         user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                         title           VARCHAR(150) NOT NULL DEFAULT 'Meu Currículo',
                         language        VARCHAR(10) DEFAULT 'pt-BR',
                         template_id     VARCHAR(50),
                         is_primary      BOOLEAN DEFAULT FALSE,
                         created_at      TIMESTAMPTZ DEFAULT NOW(),
                         updated_at      TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE personal_info (
                               id              BIGSERIAL PRIMARY KEY,
                               resume_id       BIGINT NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
                               full_name       VARCHAR(150) NOT NULL,
                               headline        VARCHAR(150),
                               email           VARCHAR(150),
                               phone           VARCHAR(30),
                               address         VARCHAR(255),
                               city            VARCHAR(100),
                               state           VARCHAR(100),
                               country         VARCHAR(100),
                               postal_code     VARCHAR(20),
                               photo_url       VARCHAR(500),
                               summary         TEXT,
                               linkedin_url    VARCHAR(500),
                               github_url      VARCHAR(500),
                               portfolio_url   VARCHAR(500),
                               UNIQUE (resume_id)
);


CREATE TABLE work_experience (
                                 id              BIGSERIAL PRIMARY KEY,
                                 resume_id       BIGINT NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
                                 company         VARCHAR(150) NOT NULL,
                                 position        VARCHAR(150) NOT NULL,
                                 location        VARCHAR(150),
                                 employment_type VARCHAR(50),
                                 start_date      DATE NOT NULL,
                                 end_date        DATE,
                                 is_current      BOOLEAN DEFAULT FALSE,
                                 description     TEXT,
                                 order_index     SMALLINT DEFAULT 0
);


CREATE TABLE education (
                           id              BIGSERIAL PRIMARY KEY,
                           resume_id       BIGINT NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
                           institution     VARCHAR(150) NOT NULL,
                           degree          VARCHAR(100),
                           field_of_study  VARCHAR(150),
                           start_date      DATE,
                           end_date        DATE,
                           is_current      BOOLEAN DEFAULT FALSE,
                           description     TEXT,
                           order_index     SMALLINT DEFAULT 0
);


CREATE TABLE skills (
                        id              BIGSERIAL PRIMARY KEY,
                        resume_id       BIGINT NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
                        name            VARCHAR(100) NOT NULL,
                        category        VARCHAR(50),
                        proficiency     SMALLINT CHECK (proficiency BETWEEN 1 AND 5),
                        order_index     SMALLINT DEFAULT 0
);


CREATE TABLE languages (
                           id              BIGSERIAL PRIMARY KEY,
                           resume_id       BIGINT NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
                           name            VARCHAR(50) NOT NULL,
                           proficiency     VARCHAR(20) CHECK (proficiency IN ('BASICO','INTERMEDIARIO','AVANCADO','FLUENTE','NATIVO')),
                           order_index     SMALLINT DEFAULT 0
);

CREATE TABLE certifications (
                                id                   BIGSERIAL PRIMARY KEY,
                                resume_id            BIGINT NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
                                name                 VARCHAR(150) NOT NULL,
                                issuing_organization VARCHAR(150),
                                issue_date           DATE,
                                expiration_date      DATE,
                                credential_url       VARCHAR(500),
                                order_index          SMALLINT DEFAULT 0
);


CREATE TABLE projects (
                          id              BIGSERIAL PRIMARY KEY,
                          resume_id       BIGINT NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
                          name            VARCHAR(150) NOT NULL,
                          description     TEXT,
                          url             VARCHAR(500),
                          technologies    VARCHAR(500),
                          start_date      DATE,
                          end_date        DATE,
                          order_index     SMALLINT DEFAULT 0
);