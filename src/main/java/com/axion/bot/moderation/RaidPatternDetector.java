package com.axion.bot.moderation;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Detects raid patterns and coordinated attacks
 */
public class RaidPatternDetector {
    private final RaidDetectionConfig config;
    
    public RaidPatternDetector(RaidDetectionConfig config) {
        this.config = config;
    }
    
    public Optional<CoordinatedPattern> detectRaidPattern(List<UserActivity> activities) {
        if (activities.size() < config.getMinRaidUsers()) {
            return Optional.empty();
        }
        
        // Group activities by type
        Map<ActivityType, List<UserActivity>> activityGroups = activities.stream()
            .collect(Collectors.groupingBy(UserActivity::getType));
        
        // Check for coordinated joins
        Optional<CoordinatedPattern> joinRaid = detectJoinRaid(activityGroups.get(ActivityType.JOIN));
        if (joinRaid.isPresent()) {
            return joinRaid;
        }
        
        // Check for coordinated messaging
        Optional<CoordinatedPattern> messageRaid = detectMessageRaid(activityGroups.get(ActivityType.MESSAGE));
        if (messageRaid.isPresent()) {
            return messageRaid;
        }
        
        // Check for coordinated reactions
        Optional<CoordinatedPattern> reactionRaid = detectReactionRaid(activityGroups.get(ActivityType.REACTION));
        if (reactionRaid.isPresent()) {
            return reactionRaid;
        }
        
        return Optional.empty();
    }
    
    private Optional<CoordinatedPattern> detectJoinRaid(List<UserActivity> joinActivities) {
        if (joinActivities == null || joinActivities.size() < config.getMinRaidUsers()) {
            return Optional.empty();
        }
        
        // Check if joins occurred within the raid time window
        Instant earliestJoin = joinActivities.stream()
            .map(UserActivity::getTimestamp)
            .min(Instant::compareTo)
            .orElse(Instant.now());
        
        Instant latestJoin = joinActivities.stream()
            .map(UserActivity::getTimestamp)
            .max(Instant::compareTo)
            .orElse(Instant.now());
        
        Duration joinSpan = Duration.between(earliestJoin, latestJoin);
        
        if (joinSpan.compareTo(config.getRaidTimeWindow()) <= 0) {
            Set<String> userIds = joinActivities.stream()
                .map(UserActivity::getUserId)
                .collect(Collectors.toSet());
            
            return Optional.of(CoordinatedPattern.builder(CoordinatedPatternType.RAID)
                .addInvolvedUsers(userIds)
                .withConfidence(0.8)
                .withDescription("Join raid detected: " + userIds.size() + " users joined within " + 
                    joinSpan.toSeconds() + " seconds")
                .build());
        }
        
        return Optional.empty();
    }
    
    private Optional<CoordinatedPattern> detectMessageRaid(List<UserActivity> messageActivities) {
        if (messageActivities == null || messageActivities.size() < config.getMinRaidUsers()) {
            return Optional.empty();
        }
        
        // Check for similar content
        Map<String, List<UserActivity>> contentGroups = messageActivities.stream()
            .collect(Collectors.groupingBy(a -> normalizeContent(a.getContent())));
        
        for (Map.Entry<String, List<UserActivity>> group : contentGroups.entrySet()) {
            if (group.getValue().size() >= config.getMinRaidUsers()) {
                Set<String> userIds = group.getValue().stream()
                    .map(UserActivity::getUserId)
                    .collect(Collectors.toSet());
                
                if (userIds.size() >= config.getMinRaidUsers()) {
                    // Check timing
                    Instant earliest = group.getValue().stream()
                        .map(UserActivity::getTimestamp)
                        .min(Instant::compareTo)
                        .orElse(Instant.now());
                    
                    Instant latest = group.getValue().stream()
                        .map(UserActivity::getTimestamp)
                        .max(Instant::compareTo)
                        .orElse(Instant.now());
                    
                    Duration span = Duration.between(earliest, latest);
                    
                    if (span.compareTo(config.getRaidTimeWindow()) <= 0) {
                        return Optional.of(CoordinatedPattern.builder(CoordinatedPatternType.SPAM)
                            .addInvolvedUsers(userIds)
                            .withConfidence(0.9)
                            .withDescription("Message raid detected: " + userIds.size() + " users posting similar content")
                            .build());
                    }
                }
            }
        }
        
        // Check for rapid messaging without similar content
        Set<String> userIds = messageActivities.stream()
            .map(UserActivity::getUserId)
            .collect(Collectors.toSet());
        
        if (userIds.size() >= config.getMinRaidUsers()) {
            Instant earliest = messageActivities.stream()
                .map(UserActivity::getTimestamp)
                .min(Instant::compareTo)
                .orElse(Instant.now());
            
            Instant latest = messageActivities.stream()
                .map(UserActivity::getTimestamp)
                .max(Instant::compareTo)
                .orElse(Instant.now());
            
            Duration span = Duration.between(earliest, latest);
            
            if (span.compareTo(config.getRaidTimeWindow()) <= 0) {
                return Optional.of(CoordinatedPattern.builder(CoordinatedPatternType.RAID)
                    .addInvolvedUsers(userIds)
                    .withConfidence(0.7)
                    .withDescription("Coordinated messaging raid detected: " + userIds.size() + " users")
                    .build());
            }
        }
        
        return Optional.empty();
    }
    
    private Optional<CoordinatedPattern> detectReactionRaid(List<UserActivity> reactionActivities) {
        if (reactionActivities == null || reactionActivities.size() < config.getMinRaidUsers()) {
            return Optional.empty();
        }
        
        // Group by target message
        Map<String, List<UserActivity>> messageGroups = reactionActivities.stream()
            .collect(Collectors.groupingBy(a -> a.getTargetMessageId() != null ? a.getTargetMessageId() : "unknown"));
        
        for (Map.Entry<String, List<UserActivity>> group : messageGroups.entrySet()) {
            if (group.getValue().size() >= config.getMinRaidUsers()) {
                Set<String> userIds = group.getValue().stream()
                    .map(UserActivity::getUserId)
                    .collect(Collectors.toSet());
                
                if (userIds.size() >= config.getMinRaidUsers()) {
                    Instant earliest = group.getValue().stream()
                        .map(UserActivity::getTimestamp)
                        .min(Instant::compareTo)
                        .orElse(Instant.now());
                    
                    Instant latest = group.getValue().stream()
                        .map(UserActivity::getTimestamp)
                        .max(Instant::compareTo)
                        .orElse(Instant.now());
                    
                    Duration span = Duration.between(earliest, latest);
                    
                    if (span.compareTo(config.getRaidTimeWindow()) <= 0) {
                        return Optional.of(CoordinatedPattern.builder(CoordinatedPatternType.HARASSMENT)
                            .addInvolvedUsers(userIds)
                            .withConfidence(0.6)
                            .withDescription("Reaction raid detected: " + userIds.size() + " users targeting same message")
                            .build());
                    }
                }
            }
        }
        
        return Optional.empty();
    }
    
    private String normalizeContent(String content) {
        if (content == null) {
            return "";
        }
        
        return content.toLowerCase()
            .replaceAll("\\s+", " ")
            .replaceAll("[^a-zA-Z0-9\\s]", "")
            .trim();
    }
}