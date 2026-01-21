package com.example.chat.service.impl;

import com.example.chat.dto.ChatMessage;
import com.example.chat.dto.MessageReaction;
import com.example.chat.entity.ChatMessageEntity;
import com.example.chat.entity.ChatRoomEntity;
import com.example.chat.entity.MessageReactionEntity;
import com.example.chat.repository.ChatMessageRepository;
import com.example.chat.repository.ChatRoomRepository;
import com.example.chat.repository.MessageReactionRepository;
import com.example.chat.service.MessagePersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessagePersistenceServiceImpl implements MessagePersistenceService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MessageReactionRepository messageReactionRepository;

    @Override
    @Transactional
    public void saveMessage(ChatMessage message) {
        try {
            ensureRoomExists(message.getRoomId());

            ChatMessageEntity entity = ChatMessageEntity.builder()
                .messageId(message.getMessageId())
                .roomId(message.getRoomId())
                .userId(message.getUserId())
                .username(message.getUsername())
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .build();

            chatMessageRepository.save(entity);
            log.debug("Saved message to database: {}", message.getMessageId());
        } catch (Exception e) {
            log.error("Failed to save message: {}", message.getMessageId(), e);
            throw new RuntimeException("Failed to persist message", e);
        }
    }

    @Override
    @Transactional
    public void saveReaction(MessageReaction reaction) {
        try {
            // Check if reaction already exists
            var existing = messageReactionRepository.findByMessageIdAndUserIdAndEmoji(
                reaction.getMessageId(),
                reaction.getUserId(),
                reaction.getEmoji()
            );

            if (existing.isPresent()) {
                log.debug("Reaction already exists: {} by {} on {}",
                    reaction.getEmoji(), reaction.getUserId(), reaction.getMessageId());
                return;
            }

            MessageReactionEntity entity = MessageReactionEntity.builder()
                .reactionId(reaction.getReactionId())
                .messageId(reaction.getMessageId())
                .roomId(reaction.getRoomId())
                .userId(reaction.getUserId())
                .username(reaction.getUsername())
                .emoji(reaction.getEmoji())
                .timestamp(reaction.getTimestamp())
                .build();

            messageReactionRepository.save(entity);
            log.debug("Saved reaction to database: {} on {}", reaction.getEmoji(), reaction.getMessageId());
        } catch (Exception e) {
            log.error("Failed to save reaction: {}", reaction.getReactionId(), e);
            // Don't throw - reactions are non-critical
        }
    }

    @Override
    @Transactional
    public void removeReaction(MessageReaction reaction) {
        try {
            messageReactionRepository.deleteByMessageIdAndUserIdAndEmoji(
                reaction.getMessageId(),
                reaction.getUserId(),
                reaction.getEmoji()
            );
            log.debug("Removed reaction from database: {} by {} on {}",
                reaction.getEmoji(), reaction.getUserId(), reaction.getMessageId());
        } catch (Exception e) {
            log.error("Failed to remove reaction: {}", reaction.getReactionId(), e);
            // Don't throw - reactions are non-critical
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageEntity> getMessageHistory(String roomId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return chatMessageRepository.findByRoomIdOrderByTimestampDesc(roomId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageEntity> getMessageHistoryBefore(String roomId, LocalDateTime before, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return chatMessageRepository.findByRoomIdAndTimestampBeforeOrderByTimestampDesc(
            roomId, before, pageable
        );
    }

    @Override
    @Transactional
    public void ensureRoomExists(String roomId) {
        if (!chatRoomRepository.existsById(roomId)) {
            ChatRoomEntity room = ChatRoomEntity.builder()
                .roomId(roomId)
                .build();
            chatRoomRepository.save(room);
            log.info("Created new chat room in database: {}", roomId);
        }
    }

    @Override
    @Transactional
    public void deleteOldMessages(LocalDateTime before) {
        try {
            chatMessageRepository.deleteByTimestampBefore(before);
            log.info("Deleted messages older than: {}", before);
        } catch (Exception e) {
            log.error("Failed to delete old messages", e);
        }
    }
}
