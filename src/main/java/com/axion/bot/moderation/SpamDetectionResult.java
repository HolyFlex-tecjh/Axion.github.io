package com.axion.bot.moderation;

import java.time.Instant;
import java.util.*;

/**
 * Result of spam detection analysis
 */
public class SpamDetectionResult {
    private final String messageId;
    private final String userId;
    private final String guildId;
    private final String channelId;
    private final boolean isSpam;
    private final double spamScore;
    private final double confidence;
    private final List<String> spamIndicators;
    private final Map<String, Double> spamFactors;
    private final String spamType;
    private final Instant detectionTime;
    private final long analysisTimeMs;
    private final String messageContent;
    private final boolean requiresAction;
    private final List<String> recommendedActions;
    private final Map<String, Object> metadata;
    private final String detectionMethod;
    
    private SpamDetectionResult(Builder builder) {
        this.messageId = Objects.requireNonNull(builder.messageId, "Message ID cannot be null");
        this.userId = Objects.requireNonNull(builder.userId, "User ID cannot be null");
        this.guildId = Objects.requireNonNull(builder.guildId, "Guild ID cannot be null");
        this.channelId = Objects.requireNonNull(builder.channelId, "Channel ID cannot be null");
        this.isSpam = builder.isSpam;
        this.spamScore = Math.max(0.0, Math.min(100.0, builder.spamScore));
        this.confidence = Math.max(0.0, Math.min(100.0, builder.confidence));
        this.spamIndicators = Collections.unmodifiableList(new ArrayList<>(builder.spamIndicators));
        this.spamFactors = Collections.unmodifiableMap(new HashMap<>(builder.spamFactors));
        this.spamType = builder.spamType != null ? builder.spamType : "Unknown";
        this.detectionTime = builder.detectionTime != null ? builder.detectionTime : Instant.now();
        this.analysisTimeMs = builder.analysisTimeMs;
        this.messageContent = builder.messageContent;
        this.requiresAction = builder.requiresAction;
        this.recommendedActions = Collections.unmodifiableList(new ArrayList<>(builder.recommendedActions));
        this.metadata = Collections.unmodifiableMap(new HashMap<>(builder.metadata));
        this.detectionMethod = builder.detectionMethod != null ? builder.detectionMethod : "Unknown";
    }
    
    /**
     * Create a new builder
     */
    public static Builder builder(String messageId, String userId, String guildId, String channelId) {
        return new Builder(messageId, userId, guildId, channelId);
    }
    
    /**
     * Create a no-spam result
     */
    public static SpamDetectionResult noSpam(String messageId, String userId, String guildId, String channelId) {
        return builder(messageId, userId, guildId, channelId)
            .withIsSpam(false)
            .withSpamScore(0.0)
            .withConfidence(95.0)
            .withSpamType("None")
            .build();
    }
    
    /**
     * Create a spam result
     */
    public static SpamDetectionResult spam(String messageId, String userId, String guildId, String channelId,
                                          double score, String spamType, String indicator) {
        return builder(messageId, userId, guildId, channelId)
            .withIsSpam(true)
            .withSpamScore(score)
            .withConfidence(Math.min(95.0, score * 0.9))
            .withSpamType(spamType)
            .addSpamIndicator(indicator)
            .withRequiresAction(score >= 60.0)
            .build();
    }
    
    /**
     * Create a link spam result
     */
    public static SpamDetectionResult linkSpam(String messageId, String userId, String guildId, String channelId,
                                              double score, String link) {
        return builder(messageId, userId, guildId, channelId)
            .withIsSpam(true)
            .withSpamScore(score)
            .withConfidence(Math.min(90.0, score * 0.85))
            .withSpamType("Link Spam")
            .addSpamIndicator("Suspicious link detected: " + link)
            .addSpamFactor("link_spam", score)
            .withRequiresAction(score >= 50.0)
            .build();
    }
    
    /**
     * Create a repetitive spam result
     */
    public static SpamDetectionResult repetitiveSpam(String messageId, String userId, String guildId, String channelId,
                                                    double score, int repetitionCount) {
        return builder(messageId, userId, guildId, channelId)
            .withIsSpam(true)
            .withSpamScore(score)
            .withConfidence(Math.min(92.0, score * 0.88))
            .withSpamType("Repetitive Spam")
            .addSpamIndicator(String.format("Message repeated %d times", repetitionCount))
            .addSpamFactor("repetition", score)
            .withRequiresAction(score >= 55.0)
            .build();
    }
    
    /**
     * Create a mention spam result
     */
    public static SpamDetectionResult mentionSpam(String messageId, String userId, String guildId, String channelId,
                                                 double score, int mentionCount) {
        return builder(messageId, userId, guildId, channelId)
            .withIsSpam(true)
            .withSpamScore(score)
            .withConfidence(Math.min(88.0, score * 0.82))
            .withSpamType("Mention Spam")
            .addSpamIndicator(String.format("Excessive mentions: %d", mentionCount))
            .addSpamFactor("mention_spam", score)
            .withRequiresAction(score >= 65.0)
            .build();
    }
    
    /**
     * Get the primary spam factor
     */
    public String getPrimarySpamFactor() {
        return spamFactors.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("unknown");
    }
    
    /**
     * Check if spam is severe
     */
    public boolean isSevereSpam() {
        return isSpam && spamScore >= 75.0;
    }
    
    /**
     * Check if spam is moderate
     */
    public boolean isModerateSpam() {
        return isSpam && spamScore >= 50.0 && spamScore < 75.0;
    }
    
    /**
     * Check if spam is mild
     */
    public boolean isMildSpam() {
        return isSpam && spamScore >= 25.0 && spamScore < 50.0;
    }
    
    /**
     * Get spam severity level
     */
    public String getSpamSeverity() {
        if (!isSpam) return "None";
        if (isSevereSpam()) return "Severe";
        if (isModerateSpam()) return "Moderate";
        if (isMildSpam()) return "Mild";
        return "Low";
    }
    
    /**
     * Get confidence level category
     */
    public String getConfidenceCategory() {
        if (confidence >= 90.0) return "Very High";
        if (confidence >= 75.0) return "High";
        if (confidence >= 60.0) return "Medium";
        if (confidence >= 40.0) return "Low";
        return "Very Low";
    }
    
    /**
     * Check if detection is reliable
     */
    public boolean isReliable() {
        return confidence >= 60.0 && !spamIndicators.isEmpty();
    }
    
    /**
     * Get detection age in milliseconds
     */
    public long getAgeMs() {
        return Instant.now().toEpochMilli() - detectionTime.toEpochMilli();
    }
    
    /**
     * Check if detection is recent
     */
    public boolean isRecent(long thresholdMs) {
        return getAgeMs() <= thresholdMs;
    }
    
    /**
     * Get spam detection summary
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("Spam Detection for Message %s:\n", messageId));
        summary.append(String.format("- User: %s\n", userId));
        summary.append(String.format("- Is Spam: %s\n", isSpam ? "YES" : "NO"));
        
        if (isSpam) {
            summary.append(String.format("- Spam Score: %.1f/100\n", spamScore));
            summary.append(String.format("- Spam Type: %s\n", spamType));
            summary.append(String.format("- Severity: %s\n", getSpamSeverity()));
            summary.append(String.format("- Confidence: %s (%.1f%%)\n", getConfidenceCategory(), confidence));
            
            if (!spamIndicators.isEmpty()) {
                summary.append("- Indicators:\n");
                for (String indicator : spamIndicators) {
                    summary.append(String.format("  * %s\n", indicator));
                }
            }
            
            if (requiresAction && !recommendedActions.isEmpty()) {
                summary.append("- Recommended Actions:\n");
                for (String action : recommendedActions) {
                    summary.append(String.format("  * %s\n", action));
                }
            }
        }
        
        summary.append(String.format("- Detection Method: %s\n", detectionMethod));
        summary.append(String.format("- Analysis Time: %d ms", analysisTimeMs));
        
        return summary.toString();
    }
    
    /**
     * Get detailed analysis
     */
    public Map<String, Object> getDetailedAnalysis() {
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("messageId", messageId);
        analysis.put("userId", userId);
        analysis.put("guildId", guildId);
        analysis.put("channelId", channelId);
        analysis.put("isSpam", isSpam);
        analysis.put("spamScore", spamScore);
        analysis.put("confidence", confidence);
        analysis.put("spamType", spamType);
        analysis.put("spamSeverity", getSpamSeverity());
        analysis.put("confidenceCategory", getConfidenceCategory());
        analysis.put("isSevereSpam", isSevereSpam());
        analysis.put("isReliable", isReliable());
        analysis.put("requiresAction", requiresAction);
        analysis.put("primarySpamFactor", getPrimarySpamFactor());
        analysis.put("spamIndicators", spamIndicators);
        analysis.put("spamFactors", spamFactors);
        analysis.put("recommendedActions", recommendedActions);
        analysis.put("detectionTime", detectionTime);
        analysis.put("analysisTimeMs", analysisTimeMs);
        analysis.put("detectionMethod", detectionMethod);
        analysis.put("ageMs", getAgeMs());
        analysis.put("metadata", metadata);
        return analysis;
    }
    
    /**
     * Check if message should be deleted
     */
    public boolean shouldDelete() {
        return isSpam && (isSevereSpam() || 
                         spamType.equals("Link Spam") ||
                         spamType.equals("Mention Spam"));
    }
    
    /**
     * Check if user should be warned
     */
    public boolean shouldWarn() {
        return isSpam && (isModerateSpam() || isSevereSpam());
    }
    
    /**
     * Check if user should be temporarily muted
     */
    public boolean shouldMute() {
        return isSpam && isSevereSpam() && confidence >= 80.0;
    }
    
    // Getters
    public String getMessageId() { return messageId; }
    public String getUserId() { return userId; }
    public String getGuildId() { return guildId; }
    public String getChannelId() { return channelId; }
    public boolean isSpam() { return isSpam; }
    public double getSpamScore() { return spamScore; }
    public double getConfidence() { return confidence; }
    public List<String> getSpamIndicators() { return spamIndicators; }
    public Map<String, Double> getSpamFactors() { return spamFactors; }
    public String getSpamType() { return spamType; }
    public Instant getDetectionTime() { return detectionTime; }
    public long getAnalysisTimeMs() { return analysisTimeMs; }
    public String getMessageContent() { return messageContent; }
    public boolean requiresAction() { return requiresAction; }
    public List<String> getRecommendedActions() { return recommendedActions; }
    public Map<String, Object> getMetadata() { return metadata; }
    public String getDetectionMethod() { return detectionMethod; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        SpamDetectionResult that = (SpamDetectionResult) obj;
        return Objects.equals(messageId, that.messageId) &&
               Objects.equals(userId, that.userId) &&
               Objects.equals(guildId, that.guildId) &&
               Objects.equals(channelId, that.channelId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(messageId, userId, guildId, channelId);
    }
    
    @Override
    public String toString() {
        return String.format("SpamDetectionResult{messageId='%s', userId='%s', isSpam=%s, score=%.1f, type='%s'}",
            messageId, userId, isSpam, spamScore, spamType);
    }
    
    /**
     * Builder for SpamDetectionResult
     */
    public static class Builder {
        private final String messageId;
        private final String userId;
        private final String guildId;
        private final String channelId;
        private boolean isSpam = false;
        private double spamScore = 0.0;
        private double confidence = 0.0;
        private final List<String> spamIndicators = new ArrayList<>();
        private final Map<String, Double> spamFactors = new HashMap<>();
        private String spamType;
        private Instant detectionTime;
        private long analysisTimeMs = 0;
        private String messageContent;
        private boolean requiresAction = false;
        private final List<String> recommendedActions = new ArrayList<>();
        private final Map<String, Object> metadata = new HashMap<>();
        private String detectionMethod;
        
        private Builder(String messageId, String userId, String guildId, String channelId) {
            this.messageId = messageId;
            this.userId = userId;
            this.guildId = guildId;
            this.channelId = channelId;
        }
        
        public Builder withIsSpam(boolean isSpam) {
            this.isSpam = isSpam;
            return this;
        }
        
        public Builder withSpamScore(double spamScore) {
            this.spamScore = spamScore;
            return this;
        }
        
        public Builder withConfidence(double confidence) {
            this.confidence = confidence;
            return this;
        }
        
        public Builder addSpamIndicator(String indicator) {
            this.spamIndicators.add(indicator);
            return this;
        }
        
        public Builder addSpamFactor(String factor, double weight) {
            this.spamFactors.put(factor, weight);
            return this;
        }
        
        public Builder withSpamType(String spamType) {
            this.spamType = spamType;
            return this;
        }
        
        public Builder withDetectionTime(Instant detectionTime) {
            this.detectionTime = detectionTime;
            return this;
        }
        
        public Builder withAnalysisTime(long analysisTimeMs) {
            this.analysisTimeMs = analysisTimeMs;
            return this;
        }
        
        public Builder withMessageContent(String messageContent) {
            this.messageContent = messageContent;
            return this;
        }
        
        public Builder withRequiresAction(boolean requiresAction) {
            this.requiresAction = requiresAction;
            return this;
        }
        
        public Builder addRecommendedAction(String action) {
            this.recommendedActions.add(action);
            return this;
        }
        
        public Builder addMetadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }
        
        public Builder withDetectionMethod(String detectionMethod) {
            this.detectionMethod = detectionMethod;
            return this;
        }
        
        public SpamDetectionResult build() {
            return new SpamDetectionResult(this);
        }
    }
}