-- User profile enhancements: bio, profile photo, verified badge
CREATE TABLE IF NOT EXISTS user_profiles (
    user_id BIGINT PRIMARY KEY REFERENCES users(id)
);

ALTER TABLE user_profiles ADD COLUMN bio VARCHAR(300);
ALTER TABLE user_profiles ADD COLUMN profile_photo_url VARCHAR(500);
ALTER TABLE user_profiles ADD COLUMN verified BOOLEAN NOT NULL DEFAULT FALSE;

-- Follow system (unidirectional)
CREATE TABLE follows (
    id BIGSERIAL PRIMARY KEY,
    follower_id BIGINT NOT NULL REFERENCES users(id),
    following_id BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_follows_follower_following UNIQUE (follower_id, following_id),
    CONSTRAINT chk_follows_no_self CHECK (follower_id <> following_id)
);

CREATE INDEX idx_follows_follower_id ON follows(follower_id);
CREATE INDEX idx_follows_following_id ON follows(following_id);
