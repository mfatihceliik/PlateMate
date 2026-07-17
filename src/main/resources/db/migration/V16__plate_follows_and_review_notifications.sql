-- Plate follow system: a user follows a plate to receive review notifications
CREATE TABLE plate_follows (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    plate_id BIGINT NOT NULL REFERENCES plates(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_plate_follows_user_plate UNIQUE (user_id, plate_id)
);

CREATE INDEX idx_plate_follows_user_id ON plate_follows(user_id);
CREATE INDEX idx_plate_follows_plate_id ON plate_follows(plate_id);

-- user_settings predates Flyway on some environments and was never created by a
-- migration; recreate its pre-this-migration shape here.
CREATE TABLE IF NOT EXISTS user_settings (
    user_id                        BIGINT  PRIMARY KEY REFERENCES users(id),
    messaging_enabled              BOOLEAN NOT NULL DEFAULT TRUE,
    message_notifications_enabled  BOOLEAN NOT NULL DEFAULT TRUE,
    friend_notifications_enabled   BOOLEAN NOT NULL DEFAULT TRUE
);

-- Per-user toggle for plate review notifications (default on)
ALTER TABLE user_settings ADD COLUMN plate_review_notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE;
