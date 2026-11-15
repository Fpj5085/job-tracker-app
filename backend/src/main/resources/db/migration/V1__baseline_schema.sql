-- A3: baseline schema for fresh deployments (matches Hibernate naming of existing entities)
CREATE TABLE IF NOT EXISTS users (
    id uuid PRIMARY KEY,
    email varchar(255) UNIQUE NOT NULL,
    username varchar(255) UNIQUE NOT NULL,
    password varchar(255) NOT NULL,
    anthropic_api_key varchar(255),
    theme_preference varchar(255) NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone,
    deleted_at timestamp(6)
);

CREATE TABLE IF NOT EXISTS applications (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL REFERENCES users (id),
    company_name varchar(255),
    job_type varchar(255),
    location varchar(255),
    job_role varchar(255),
    applied_date date,
    status varchar(255),
    status_changed_date date,
    website_link varchar(255),
    username varchar(255),
    password varchar(255),
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone,
    deleted_at timestamp(6)
);
CREATE INDEX IF NOT EXISTS idx_applications_user ON applications (user_id);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id uuid PRIMARY KEY,
    token varchar(255) UNIQUE NOT NULL,
    family_id uuid NOT NULL,
    user_id uuid NOT NULL REFERENCES users (id),
    expires_at timestamp(6) with time zone NOT NULL,
    revoked boolean NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    replaced_by_id uuid,
    version integer NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user ON refresh_tokens (user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_family ON refresh_tokens (family_id);
