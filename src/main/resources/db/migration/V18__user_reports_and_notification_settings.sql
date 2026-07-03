-- User reporting
CREATE TABLE user_reports (
    id BIGSERIAL PRIMARY KEY,
    reporter_id BIGINT NOT NULL REFERENCES users(id),
    reported_id BIGINT NOT NULL REFERENCES users(id),
    reason VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_user_reports_no_self CHECK (reporter_id <> reported_id)
);

CREATE INDEX idx_user_reports_reporter_id ON user_reports(reporter_id);
CREATE INDEX idx_user_reports_reported_id ON user_reports(reported_id);

-- Additional notification preferences
ALTER TABLE user_settings ADD COLUMN new_follower_notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE user_settings ADD COLUMN review_reply_notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE;
