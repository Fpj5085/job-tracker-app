-- A2: offers table (equivalent migration; ddl-auto=update also creates this schema)
CREATE TABLE IF NOT EXISTS offers (
    id UUID PRIMARY KEY,
    application_id UUID NOT NULL REFERENCES applications (id),
    user_id UUID NOT NULL REFERENCES users (id),
    base_salary NUMERIC(14,2) NOT NULL,
    bonus NUMERIC(14,2),
    equity NUMERIC(14,2),
    currency VARCHAR(3) NOT NULL,
    location VARCHAR(255) NOT NULL,
    remote_policy VARCHAR(255),
    insurance VARCHAR(255),
    pto_days INTEGER,
    start_date DATE,
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_offers_application ON offers (application_id);
CREATE INDEX IF NOT EXISTS idx_offers_user ON offers (user_id);
