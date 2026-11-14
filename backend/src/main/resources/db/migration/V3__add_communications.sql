-- A2: communications table (equivalent migration; ddl-auto=update also creates this schema)
CREATE TABLE IF NOT EXISTS communications (
    id UUID PRIMARY KEY,
    application_id UUID NOT NULL REFERENCES applications (id),
    user_id UUID NOT NULL REFERENCES users (id),
    channel VARCHAR(20) NOT NULL,
    direction VARCHAR(4) NOT NULL,
    occurred_at TIMESTAMP NOT NULL,
    summary TEXT,
    next_action TEXT,
    next_action_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_communications_application ON communications (application_id);
CREATE INDEX IF NOT EXISTS idx_communications_next_action ON communications (user_id, next_action_at);
