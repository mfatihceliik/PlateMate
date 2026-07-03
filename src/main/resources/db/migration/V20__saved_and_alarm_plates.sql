-- User saved plates (bookmarks). Unlimited per user.
CREATE TABLE saved_plates (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    plate_id BIGINT NOT NULL REFERENCES plates(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_saved_plates_user_plate UNIQUE (user_id, plate_id)
);

CREATE INDEX idx_saved_plates_user_id ON saved_plates(user_id);
CREATE INDEX idx_saved_plates_plate_id ON saved_plates(plate_id);

-- User plate alarms. Limited for non-premium users (see app_settings).
CREATE TABLE plate_alarms (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    plate_id BIGINT NOT NULL REFERENCES plates(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_plate_alarms_user_plate UNIQUE (user_id, plate_id)
);

CREATE INDEX idx_plate_alarms_user_id ON plate_alarms(user_id);
CREATE INDEX idx_plate_alarms_plate_id ON plate_alarms(plate_id);
