-- A2: interviews table (equivalent migration; ddl-auto=update also creates this schema)
CREATE TABLE IF NOT EXISTS interviews (
    id UUID PRIMARY KEY,
    application_id UUID NOT NULL REFERENCES applications (id),
    user_id UUID NOT NULL REFERENCES users (id),
    stage VARCHAR(20) NOT NULL,
    scheduled_at TIMESTAMP NOT NULL,
    timezone VARCHAR(64),
    mode VARCHAR(10) NOT NULL,
    interviewer_notes TEXT,
    feedback TEXT,
    result VARCHAR(10) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_interviews_application ON interviews (application_id);
CREATE INDEX IF NOT EXISTS idx_interviews_user_schedule ON interviews (user_id, scheduled_at);
