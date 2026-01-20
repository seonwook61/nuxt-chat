package com.example.chat.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * DTO representing aggregated reactions for a message
 * Maps emoji types to the set of users who reacted with that emoji
 */
public class ReactionSummary {

    private Map<String, Set<String>> reactions; // emoji -> Set of userIds

    // Default constructor
    public ReactionSummary() {
        this.reactions = new HashMap<>();
    }

    @JsonCreator
    public ReactionSummary(@JsonProperty("reactions") Map<String, Set<String>> reactions) {
        this.reactions = reactions != null ? reactions : new HashMap<>();
    }

    public Map<String, Set<String>> getReactions() {
        return reactions;
    }

    public void setReactions(Map<String, Set<String>> reactions) {
        this.reactions = reactions;
    }

    /**
     * Add a reaction for a user
     */
    public void addReaction(String emoji, String userId) {
        reactions.computeIfAbsent(emoji, k -> new HashSet<>()).add(userId);
    }

    /**
     * Remove a reaction from a user
     */
    public void removeReaction(String emoji, String userId) {
        Set<String> users = reactions.get(emoji);
        if (users != null) {
            users.remove(userId);
            if (users.isEmpty()) {
                reactions.remove(emoji);
            }
        }
    }

    /**
     * Check if a user has reacted with a specific emoji
     */
    public boolean hasUserReacted(String emoji, String userId) {
        Set<String> users = reactions.get(emoji);
        return users != null && users.contains(userId);
    }

    /**
     * Get count of reactions for a specific emoji
     */
    public int getReactionCount(String emoji) {
        Set<String> users = reactions.get(emoji);
        return users != null ? users.size() : 0;
    }

    /**
     * Get total count of all reactions
     */
    public int getTotalReactionCount() {
        return reactions.values().stream()
                .mapToInt(Set::size)
                .sum();
    }

    @Override
    public String toString() {
        return "ReactionSummary{" +
                "reactions=" + reactions +
                '}';
    }
}
