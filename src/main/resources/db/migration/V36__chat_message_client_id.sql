-- Client-generated correlation id, echoed back in the send ack so Android can reconcile
-- an optimistic local row against the server-persisted one (and detect/skip duplicate
-- retries when an ack packet itself gets lost after the message already committed).
ALTER TABLE chat_messages ADD COLUMN IF NOT EXISTS client_message_id VARCHAR(64);

CREATE INDEX IF NOT EXISTS idx_chat_messages_sender_client_id ON chat_messages(sender_id, client_message_id);
