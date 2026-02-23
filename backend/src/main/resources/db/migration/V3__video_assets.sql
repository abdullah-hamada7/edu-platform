-- Video assets table

CREATE TYPE transcode_status AS ENUM ('PENDING', 'PROCESSING', 'READY', 'FAILED');

CREATE TABLE video_asset (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    raw_object_key VARCHAR(255) NOT NULL,
    hls_manifest_key VARCHAR(255),
    encryption_key_ref VARCHAR(255),
    transcode_status transcode_status NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_video_asset_status ON video_asset(transcode_status);
