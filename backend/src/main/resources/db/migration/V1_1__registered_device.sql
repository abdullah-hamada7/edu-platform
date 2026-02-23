-- Add registered devices table

CREATE TABLE registered_device (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,
    fingerprint_hash VARCHAR(255) NOT NULL UNIQUE,
    last_seen_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_registered_device_student ON registered_device(student_id);
CREATE INDEX idx_registered_device_fingerprint ON registered_device(fingerprint_hash);
