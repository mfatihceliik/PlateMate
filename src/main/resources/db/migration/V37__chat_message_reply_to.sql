-- Quote/reply-to-message: nullable self-referencing FK. ON DELETE SET NULL so a deleted
-- (tombstoned) or hard-removed parent message just drops the quote instead of blocking deletes.
ALTER TABLE chat_messages ADD COLUMN IF NOT EXISTS reply_to_message_id BIGINT;

ALTER TABLE chat_messages
    ADD CONSTRAINT fk_chat_messages_reply_to
    FOREIGN KEY (reply_to_message_id) REFERENCES chat_messages(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_chat_messages_reply_to ON chat_messages(reply_to_message_id);
