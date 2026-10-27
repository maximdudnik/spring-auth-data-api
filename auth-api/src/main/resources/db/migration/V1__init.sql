CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL
);

CREATE TABLE processing_log (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    input_text VARCHAR(10000) NOT NULL,
    output_text VARCHAR(10000) NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_processing_log_user_id ON processing_log (user_id);
