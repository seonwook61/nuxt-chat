-- Chat Rooms Table
CREATE TABLE chat_rooms (
    room_id VARCHAR(255) PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Chat Messages Table
CREATE TABLE chat_messages (
    message_id UUID PRIMARY KEY,
    room_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_room FOREIGN KEY (room_id) REFERENCES chat_rooms(room_id) ON DELETE CASCADE
);

-- Message Reactions Table
CREATE TABLE message_reactions (
    reaction_id UUID PRIMARY KEY,
    message_id UUID NOT NULL,
    room_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL,
    emoji VARCHAR(20) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_message FOREIGN KEY (message_id) REFERENCES chat_messages(message_id) ON DELETE CASCADE,
    CONSTRAINT unique_reaction UNIQUE (message_id, user_id, emoji)
);

-- Indexes for better query performance
CREATE INDEX idx_messages_room_timestamp ON chat_messages(room_id, timestamp DESC);
CREATE INDEX idx_messages_user ON chat_messages(user_id);
CREATE INDEX idx_reactions_message ON message_reactions(message_id);
CREATE INDEX idx_reactions_user ON message_reactions(user_id);
