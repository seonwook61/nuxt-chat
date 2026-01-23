-- Phase 6: Read Receipts
-- Create message_read_status table to track which users have read which messages

CREATE TABLE message_read_status (
    id BIGSERIAL PRIMARY KEY,
    message_id UUID NOT NULL,
    room_id VARCHAR(100) NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    read_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Prevent duplicate read receipts for same message + user
    CONSTRAINT uk_message_user UNIQUE (message_id, user_id)
);

-- Index for querying read status by room and user
-- Used to find last read position for a user in a room
CREATE INDEX idx_message_read_room_user ON message_read_status(room_id, user_id);

-- Index for querying who read a specific message
-- Used to display "Read by X people" or list of readers
CREATE INDEX idx_message_read_message ON message_read_status(message_id);

-- Index for timestamp-based queries
CREATE INDEX idx_message_read_timestamp ON message_read_status(read_at);

-- Add comment for documentation
COMMENT ON TABLE message_read_status IS 'Tracks read receipts for messages - which users have read which messages';
COMMENT ON COLUMN message_read_status.message_id IS 'Foreign key to chat_message.message_id (logical relationship)';
COMMENT ON COLUMN message_read_status.room_id IS 'Room where the message was sent';
COMMENT ON COLUMN message_read_status.user_id IS 'User who read the message';
COMMENT ON COLUMN message_read_status.read_at IS 'Timestamp when the user read the message';
COMMENT ON COLUMN message_read_status.created_at IS 'Record creation timestamp (audit)';
