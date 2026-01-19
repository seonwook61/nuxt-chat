package com.example.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Room Information DTO
 *
 * Represents chat room metadata and statistics.
 * - Used for room listing and room details API
 * - Cached in Redis: room:{roomId}:info
 *
 * @see com.example.chat.service.ChatService#getRoomInfo(String)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomInfo {

    /**
     * Unique room identifier
     */
    @NotBlank(message = "roomId cannot be blank")
    private String roomId;

    /**
     * Display name of the room
     */
    @NotBlank(message = "name cannot be blank")
    private String name;

    /**
     * Current number of active users in the room
     */
    @NotNull(message = "userCount cannot be null")
    @Min(value = 0, message = "userCount must be >= 0")
    private Integer userCount;

    /**
     * Timestamp when room was created
     */
    @NotNull(message = "createdAt cannot be null")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}
