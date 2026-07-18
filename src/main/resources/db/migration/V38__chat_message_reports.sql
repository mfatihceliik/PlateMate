-- Report a chat message. Mirrors user_reports' shape (V18): no status/lookup column, no
-- duplicate-guard — reason is a free client-driven string, validated only at the DTO level.
CREATE TABLE chat_message_reports (
    id BIGSERIAL PRIMARY KEY,
    reporter_id BIGINT NOT NULL REFERENCES users(id),
    message_id BIGINT NOT NULL REFERENCES chat_messages(id),
    reason VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_chat_message_reports_reporter_id ON chat_message_reports(reporter_id);
CREATE INDEX idx_chat_message_reports_message_id ON chat_message_reports(message_id);
