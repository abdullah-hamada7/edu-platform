-- Baseline migration for secure math platform
-- Creates initial schema for user accounts and roles

CREATE TYPE role AS ENUM ('ADMIN', 'STUDENT');
CREATE TYPE account_status AS ENUM ('ACTIVE', 'INACTIVE');

CREATE TABLE user_account (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role role NOT NULL,
    status account_status NOT NULL DEFAULT 'ACTIVE',
    must_change_password BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_user_account_email ON user_account(email);
CREATE INDEX idx_user_account_role ON user_account(role);
CREATE INDEX idx_user_account_status ON user_account(status);
