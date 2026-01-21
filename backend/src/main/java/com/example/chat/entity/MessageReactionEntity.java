package com.example.chat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "message_reactions",
    uniqueConstraints = @UniqueConstraint(columnNames = {"message_id", "user_id", "emoji"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageReactionEntity {

    @Id
    @Column(name = "reaction_id", nullable = false)
    private UUID reactionId;

    @Column(name = "message_id", nullable = false)
    private UUID messageId;

    @Column(name = "room_id", nullable = false)
    private String roomId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "emoji", nullable = false, length = 20)
    private String emoji;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", insertable = false, updatable = false)
    private ChatMessageEntity chatMessage;

    @PrePersist
    protected void onCreate() {
        if (reactionId == null) {
            reactionId = UUID.randomUUID();
        }
        createdAt = LocalDateTime.now();
    }
}
