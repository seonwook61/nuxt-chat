package com.example.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

/**
 * Request DTO for adding/removing reactions to messages
 */
public class ReactionRequest {

    @NotNull(message = "Message ID cannot be null")
    private UUID messageId;

    @NotBlank(message = "Emoji cannot be blank")
    @Pattern(regexp = "HEART|LAUGH|WOW|SAD|THUMBS_UP|FIRE",
             message = "Emoji must be one of: HEART, LAUGH, WOW, SAD, THUMBS_UP, FIRE")
    private String emoji;

    @NotBlank(message = "User ID cannot be blank")
    private String userId;

    @NotBlank(message = "Username cannot be blank")
    private String username;

    // Default constructor
    public ReactionRequest() {
    }

    public ReactionRequest(UUID messageId, String emoji, String userId, String username) {
        this.messageId = messageId;
        this.emoji = emoji;
        this.userId = userId;
        this.username = username;
    }

    // Getters and Setters
    public UUID getMessageId() {
        return messageId;
    }

    public void setMessageId(UUID messageId) {
        this.messageId = messageId;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
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

    @Override
    public String toString() {
        return "ReactionRequest{" +
                "messageId=" + messageId +
                ", emoji='" + emoji + '\'' +
                ", userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
