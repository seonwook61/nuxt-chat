package com.example.chat.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for message reactions (Instagram-style emoji reactions)
 * Supports 6 emoji types: HEART, LAUGH, WOW, SAD, THUMBS_UP, FIRE
 */
public class MessageReaction {

    @NotNull(message = "Reaction ID cannot be null")
    private UUID reactionId;

    @NotNull(message = "Message ID cannot be null")
    private UUID messageId;

    @NotBlank(message = "Room ID cannot be blank")
    private String roomId;

    @NotBlank(message = "User ID cannot be blank")
    private String userId;

    @NotBlank(message = "Username cannot be blank")
    private String username;

    @NotBlank(message = "Emoji cannot be blank")
    @Pattern(regexp = "HEART|LAUGH|WOW|SAD|THUMBS_UP|FIRE",
             message = "Emoji must be one of: HEART, LAUGH, WOW, SAD, THUMBS_UP, FIRE")
    private String emoji;

    @NotNull(message = "Timestamp cannot be null")
    private LocalDateTime timestamp;

    @NotBlank(message = "Action cannot be blank")
    @Pattern(regexp = "ADD|REMOVE", message = "Action must be ADD or REMOVE")
    private String action;

    // Default constructor for Jackson
    public MessageReaction() {
    }

    @JsonCreator
    public MessageReaction(
            @JsonProperty("reactionId") UUID reactionId,
            @JsonProperty("messageId") UUID messageId,
            @JsonProperty("roomId") String roomId,
            @JsonProperty("userId") String userId,
            @JsonProperty("username") String username,
            @JsonProperty("emoji") String emoji,
            @JsonProperty("timestamp") LocalDateTime timestamp,
            @JsonProperty("action") String action) {
        this.reactionId = reactionId;
        this.messageId = messageId;
        this.roomId = roomId;
        this.userId = userId;
        this.username = username;
        this.emoji = emoji;
        this.timestamp = timestamp;
        this.action = action;
    }

    // Getters and Setters
    public UUID getReactionId() {
        return reactionId;
    }

    public void setReactionId(UUID reactionId) {
        this.reactionId = reactionId;
    }

    public UUID getMessageId() {
        return messageId;
    }

    public void setMessageId(UUID messageId) {
        this.messageId = messageId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return "MessageReaction{" +
                "reactionId=" + reactionId +
                ", messageId=" + messageId +
                ", roomId='" + roomId + '\'' +
                ", userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", emoji='" + emoji + '\'' +
                ", timestamp=" + timestamp +
                ", action='" + action + '\'' +
                '}';
    }
}
