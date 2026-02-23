-- Playback access grants table

CREATE TABLE playback_access_grant (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,
    lesson_id UUID NOT NULL REFERENCES lesson(id) ON DELETE CASCADE,
    device_id UUID REFERENCES registered_device(id) ON DELETE SET NULL,
    signed_url_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    issued_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_playback_grant_student ON playback_access_grant(student_id);
CREATE INDEX idx_playback_grant_lesson ON playback_access_grant(lesson_id);
CREATE INDEX idx_playback_grant_expires ON playback_access_grant(expires_at);
