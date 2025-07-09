package com.axion.bot.moderation;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Advanced Spam Detection Engine
 * Provides sophisticated spam detection using multiple algorithms
 * Tracks user behavior patterns and adapts to new spam techniques
 */
public class SpamDetectionEngine {
    private static final Logger logger = LoggerFactory.getLogger(SpamDetectionEngine.class);
    
    // User message tracking
    private final Map<String, UserMessageHistory> userMessageHistories = new ConcurrentHashMap<>();
    private final Map<String, UserBehaviorProfile> userBehaviorProfiles = new ConcurrentHashMap<>();
    
    // Spam patterns and detection
    private final Set<Pattern> spamPatterns;
    private final Set<String> spamKeywords;
    private final Map<String, Integer> suspiciousDomains;
    
    // Configuration thresholds
    private static final int MAX_MESSAGES_PER_MINUTE = 10;
    private static final int MAX_IDENTICAL_MESSAGES = 3;
    private static final int MAX_SIMILAR_MESSAGES = 5;
    private static final double SIMILARITY_THRESHOLD = 0.8;
    private static final int MAX_MENTIONS_PER_MESSAGE = 5;
    private static final int MAX_LINKS_PER_MESSAGE = 3;
    private static final Duration FAST_TYPING_THRESHOLD = Duration.ofSeconds(2);
    
    public SpamDetectionEngine() {
        this.spamPatterns = initializeSpamPatterns();
        this.spamKeywords = initializeSpamKeywords();
        this.suspiciousDomains = initializeSuspiciousDomains();
    }
    
    /**
     * Analyze message for spam characteristics
     */
    public SpamDetectionResult analyzeMessage(Message message, UserModerationProfile profile) {
        String userId = message.getAuthor().getId();
        String content = message.getContentRaw();
        Instant timestamp = message.getTimeCreated().toInstant();
        
        // Get or create user histories
        UserMessageHistory history = userMessageHistories.computeIfAbsent(userId, k -> new UserMessageHistory());
        UserBehaviorProfile behaviorProfile = userBehaviorProfiles.computeIfAbsent(userId, k -> new UserBehaviorProfile());
        
        // Record message
        MessageRecord record = new MessageRecord(content, timestamp, message.getChannel().getId());
        history.addMessage(record);
        
        List<SpamFlag> flags = new ArrayList<>();
        int totalScore = 0;
        
        // Check message frequency
        SpamFlag frequencyFlag = checkMessageFrequency(history, timestamp);
        if (frequencyFlag != null) {
            flags.add(frequencyFlag);
            totalScore += frequencyFlag.getScore();
        }
        
        // Check for identical messages
        SpamFlag identicalFlag = checkIdenticalMessages(history, content);
        if (identicalFlag != null) {
            flags.add(identicalFlag);
            totalScore += identicalFlag.getScore();
        }
        
        // Check for similar messages
        SpamFlag similarFlag = checkSimilarMessages(history, content);
        if (similarFlag != null) {
            flags.add(similarFlag);
            totalScore += similarFlag.getScore();
        }
        
        // Check for spam patterns
        SpamFlag patternFlag = checkSpamPatterns(content);
        if (patternFlag != null) {
            flags.add(patternFlag);
            totalScore += patternFlag.getScore();
        }
        
        // Check for excessive mentions
        SpamFlag mentionFlag = checkExcessiveMentions(message);
        if (mentionFlag != null) {
            flags.add(mentionFlag);
            totalScore += mentionFlag.getScore();
        }
        
        // Check for excessive links
        SpamFlag linkFlag = checkExcessiveLinks(content);
        if (linkFlag != null) {
            flags.add(linkFlag);
            totalScore += linkFlag.getScore();
        }
        
        // Check for fast typing (bot-like behavior)
        SpamFlag typingFlag = checkFastTyping(history, content, timestamp);
        if (typingFlag != null) {
            flags.add(typingFlag);
            totalScore += typingFlag.getScore();
        }
        
        // Check for suspicious domains
        SpamFlag domainFlag = checkSuspiciousDomains(content);
        if (domainFlag != null) {
            flags.add(domainFlag);
            totalScore += domainFlag.getScore();
        }
        
        // Behavioral analysis
        SpamFlag behaviorFlag = analyzeBehaviorPattern(behaviorProfile, record);
        if (behaviorFlag != null) {
            flags.add(behaviorFlag);
            totalScore += behaviorFlag.getScore();
        }
        
        // Update behavior profile
        behaviorProfile.updateProfile(record, flags);
        
        // Clean old data
        cleanOldData(history, timestamp);
        
        // Determine spam likelihood
        SpamLikelihood likelihood = determineSpamLikelihood(totalScore);
        boolean isSpam = likelihood != SpamLikelihood.LOW;
        
        if (isSpam) {
            logger.debug("Spam detected - User: {}, Score: {}, Likelihood: {}", 
                userId, totalScore, likelihood);
        }
        
        return new SpamDetectionResult(isSpam, likelihood, totalScore, flags);
    }
    
    /**
     * Check message frequency
     */
    private SpamFlag checkMessageFrequency(UserMessageHistory history, Instant timestamp) {
        long recentMessages = history.getMessages().stream()
            .filter(msg -> Duration.between(msg.getTimestamp(), timestamp).toMinutes() <= 1)
            .count();
        
        if (recentMessages > MAX_MESSAGES_PER_MINUTE) {
            int score = Math.min((int)(recentMessages - MAX_MESSAGES_PER_MINUTE) * 10, 50);
            return new SpamFlag(
                SpamType.HIGH_FREQUENCY,
                "High message frequency: " + recentMessages + " messages per minute",
                score
            );
        }
        
        return null;
    }
    
    /**
     * Check for identical messages
     */
    private SpamFlag checkIdenticalMessages(UserMessageHistory history, String content) {
        long identicalCount = history.getMessages().stream()
            .filter(msg -> Duration.between(msg.getTimestamp(), Instant.now()).toMinutes() <= 10)
            .filter(msg -> msg.getContent().equals(content))
            .count();
        
        if (identicalCount > MAX_IDENTICAL_MESSAGES) {
            int score = Math.min((int)(identicalCount - MAX_IDENTICAL_MESSAGES) * 15, 60);
            return new SpamFlag(
                SpamType.IDENTICAL_MESSAGES,
                "Identical message repeated " + identicalCount + " times",
                score
            );
        }
        
        return null;
    }
    
    /**
     * Check for similar messages
     */
    private SpamFlag checkSimilarMessages(UserMessageHistory history, String content) {
        List<MessageRecord> recentMessages = history.getMessages().stream()
            .filter(msg -> Duration.between(msg.getTimestamp(), Instant.now()).toMinutes() <= 15)
            .collect(Collectors.toList());
        
        int similarCount = 0;
        for (MessageRecord msg : recentMessages) {
            if (calculateSimilarity(content, msg.getContent()) >= SIMILARITY_THRESHOLD) {
                similarCount++;
            }
        }
        
        if (similarCount > MAX_SIMILAR_MESSAGES) {
            int score = Math.min((similarCount - MAX_SIMILAR_MESSAGES) * 12, 45);
            return new SpamFlag(
                SpamType.SIMILAR_MESSAGES,
                "Similar messages detected: " + similarCount,
                score
            );
        }
        
        return null;
    }
    
    /**
     * Check for spam patterns
     */
    private SpamFlag checkSpamPatterns(String content) {
        int patternMatches = 0;
        List<String> matchedPatterns = new ArrayList<>();
        
        for (Pattern pattern : spamPatterns) {
            if (pattern.matcher(content).find()) {
                patternMatches++;
                matchedPatterns.add(pattern.pattern());
            }
        }
        
        // Check for spam keywords
        String lowerContent = content.toLowerCase();
        for (String keyword : spamKeywords) {
            if (lowerContent.contains(keyword)) {
                patternMatches++;
                matchedPatterns.add(keyword);
            }
        }
        
        if (patternMatches > 0) {
            int score = Math.min(patternMatches * 20, 70);
            return new SpamFlag(
                SpamType.SPAM_PATTERNS,
                "Spam patterns detected: " + patternMatches,
                score
            );
        }
        
        return null;
    }
    
    /**
     * Check for excessive mentions
     */
    private SpamFlag checkExcessiveMentions(Message message) {
        int mentionCount = message.getMentions().getUsers().size() + 
                         message.getMentions().getRoles().size() + 
                         (message.mentionsEveryone() ? 1 : 0);
        
        if (mentionCount > MAX_MENTIONS_PER_MESSAGE) {
            int score = Math.min((mentionCount - MAX_MENTIONS_PER_MESSAGE) * 15, 50);
            return new SpamFlag(
                SpamType.EXCESSIVE_MENTIONS,
                "Excessive mentions: " + mentionCount,
                score
            );
        }
        
        return null;
    }
    
    /**
     * Check for excessive links
     */
    private SpamFlag checkExcessiveLinks(String content) {
        Pattern urlPattern = Pattern.compile("https?://[^\\s]+", Pattern.CASE_INSENSITIVE);
        long linkCount = urlPattern.matcher(content).results().count();
        
        if (linkCount > MAX_LINKS_PER_MESSAGE) {
            int score = Math.min((int)(linkCount - MAX_LINKS_PER_MESSAGE) * 20, 60);
            return new SpamFlag(
                SpamType.EXCESSIVE_LINKS,
                "Excessive links: " + linkCount,
                score
            );
        }
        
        return null;
    }
    
    /**
     * Check for fast typing (potential bot behavior)
     */
    private SpamFlag checkFastTyping(UserMessageHistory history, String content, Instant timestamp) {
        if (content.length() < 20) return null; // Short messages are fine
        
        List<MessageRecord> recentMessages = history.getMessages().stream()
            .filter(msg -> Duration.between(msg.getTimestamp(), timestamp).toMinutes() <= 5)
            .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
            .collect(Collectors.toList());
        
        if (recentMessages.size() >= 2) {
            MessageRecord lastMessage = recentMessages.get(0);
            Duration timeBetween = Duration.between(lastMessage.getTimestamp(), timestamp);
            
            // Calculate expected typing time (rough estimate: 5 characters per second)
            Duration expectedTime = Duration.ofSeconds(content.length() / 5);
            
            if (timeBetween.compareTo(FAST_TYPING_THRESHOLD) < 0 && 
                timeBetween.compareTo(expectedTime.dividedBy(3)) < 0) {
                
                return new SpamFlag(
                    SpamType.FAST_TYPING,
                    "Suspiciously fast typing detected",
                    25
                );
            }
        }
        
        return null;
    }
    
    /**
     * Check for suspicious domains
     */
    private SpamFlag checkSuspiciousDomains(String content) {
        Pattern urlPattern = Pattern.compile("https?://([^/\\s]+)", Pattern.CASE_INSENSITIVE);
        
        int suspiciousCount = 0;
        List<String> foundDomains = new ArrayList<>();
        
        urlPattern.matcher(content).results().forEach(match -> {
            String domain = match.group(1).toLowerCase();
            if (suspiciousDomains.containsKey(domain)) {
                suspiciousCount++;
                foundDomains.add(domain);
            }
        });
        
        if (suspiciousCount > 0) {
            int score = suspiciousCount * 30;
            return new SpamFlag(
                SpamType.SUSPICIOUS_LINKS,
                "Suspicious domains detected: " + foundDomains.size(),
                score
            );
        }
        
        return null;
    }
    
    /**
     * Analyze user behavior patterns
     */
    private SpamFlag analyzeBehaviorPattern(UserBehaviorProfile profile, MessageRecord record) {
        profile.updateMetrics(record);
        
        // Check for bot-like patterns
        if (profile.getMessageCount() >= 10) {
            double avgInterval = profile.getAverageMessageInterval();
            double intervalVariance = profile.getMessageIntervalVariance();
            
            // Very consistent intervals might indicate bot behavior
            if (avgInterval > 0 && intervalVariance / avgInterval < 0.1) {
                return new SpamFlag(
                    SpamType.BOT_BEHAVIOR,
                    "Bot-like messaging pattern detected",
                    30
                );
            }
            
            // Check for repetitive content patterns
            if (profile.getContentDiversityScore() < 0.3) {
                return new SpamFlag(
                    SpamType.REPETITIVE_CONTENT,
                    "Highly repetitive content pattern",
                    25
                );
            }
        }
        
        return null;
    }
    
    /**
     * Calculate similarity between two strings
     */
    private double calculateSimilarity(String s1, String s2) {
        if (s1.equals(s2)) return 1.0;
        
        String[] words1 = s1.toLowerCase().split("\\s+");
        String[] words2 = s2.toLowerCase().split("\\s+");
        
        Set<String> set1 = new HashSet<>(Arrays.asList(words1));
        Set<String> set2 = new HashSet<>(Arrays.asList(words2));
        
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }
    
    /**
     * Determine spam likelihood based on score
     */
    private SpamLikelihood determineSpamLikelihood(int score) {
        if (score >= 80) {
            return SpamLikelihood.VERY_HIGH;
        } else if (score >= 60) {
            return SpamLikelihood.HIGH;
        } else if (score >= 40) {
            return SpamLikelihood.MEDIUM;
        } else if (score >= 20) {
            return SpamLikelihood.LOW;
        } else {
            return SpamLikelihood.VERY_LOW;
        }
    }
    
    /**
     * Clean old data to prevent memory leaks
     */
    private void cleanOldData(UserMessageHistory history, Instant timestamp) {
        history.getMessages().removeIf(msg -> 
            Duration.between(msg.getTimestamp(), timestamp).toHours() > 24);
    }
    
    /**
     * Initialize spam patterns
     */
    private Set<Pattern> initializeSpamPatterns() {
        Set<Pattern> patterns = new HashSet<>();
        
        // Common spam patterns
        patterns.add(Pattern.compile("\\b(free\\s+money|get\\s+rich\\s+quick|make\\s+money\\s+fast)\\b", Pattern.CASE_INSENSITIVE));
        patterns.add(Pattern.compile("\\b(click\\s+here|visit\\s+now|limited\\s+time)\\b", Pattern.CASE_INSENSITIVE));
        patterns.add(Pattern.compile("\\b(buy\\s+now|order\\s+today|act\\s+fast)\\b", Pattern.CASE_INSENSITIVE));
        patterns.add(Pattern.compile("\\b(100%\\s+free|no\\s+cost|absolutely\\s+free)\\b", Pattern.CASE_INSENSITIVE));
        patterns.add(Pattern.compile("\\b(join\\s+my\\s+server|check\\s+out\\s+my)\\b", Pattern.CASE_INSENSITIVE));
        
        // Repetitive character patterns
        patterns.add(Pattern.compile("(.)\\1{5,}"));
        patterns.add(Pattern.compile("[!?]{4,}"));
        
        return patterns;
    }
    
    /**
     * Initialize spam keywords
     */
    private Set<String> initializeSpamKeywords() {
        Set<String> keywords = new HashSet<>();
        
        keywords.add("discord.gg");
        keywords.add("nitro");
        keywords.add("giveaway");
        keywords.add("free gift");
        keywords.add("promotion");
        keywords.add("limited offer");
        keywords.add("exclusive deal");
        
        return keywords;
    }
    
    /**
     * Initialize suspicious domains
     */
    private Map<String, Integer> initializeSuspiciousDomains() {
        Map<String, Integer> domains = new HashMap<>();
        
        // Common spam/phishing domains (examples)
        domains.put("bit.ly", 10);
        domains.put("tinyurl.com", 10);
        domains.put("t.co", 5);
        
        // Known malicious domains would be added here
        // This would typically be loaded from a threat intelligence feed
        
        return domains;
    }
    
    /**
     * Get user spam statistics
     */
    public UserSpamStats getUserSpamStats(String userId) {
        UserBehaviorProfile profile = userBehaviorProfiles.get(userId);
        if (profile == null) {
            return new UserSpamStats(0, 0, 0.0, Collections.emptyList());
        }
        
        return new UserSpamStats(
            profile.getMessageCount(),
            profile.getSpamFlagCount(),
            profile.getSpamScore(),
            profile.getRecentFlags()
        );
    }
    
    // Data classes
    
    private static class UserMessageHistory {
        private final List<MessageRecord> messages = new ArrayList<>();
        
        public void addMessage(MessageRecord record) {
            messages.add(record);
        }
        
        public List<MessageRecord> getMessages() {
            return messages;
        }
    }
    
    private static class MessageRecord {
        private final String content;
        private final Instant timestamp;
        private final String channelId;
        
        public MessageRecord(String content, Instant timestamp, String channelId) {
            this.content = content;
            this.timestamp = timestamp;
            this.channelId = channelId;
        }
        
        // Getters
        public String getContent() { return content; }
        public Instant getTimestamp() { return timestamp; }
        public String getChannelId() { return channelId; }
    }
    
    private static class UserBehaviorProfile {
        private int messageCount = 0;
        private int spamFlagCount = 0;
        private double totalSpamScore = 0.0;
        private final List<Double> messageIntervals = new ArrayList<>();
        private final Set<String> uniqueContent = new HashSet<>();
        private final List<SpamFlag> recentFlags = new ArrayList<>();
        private Instant lastMessageTime;
        
        public void updateProfile(MessageRecord record, List<SpamFlag> flags) {
            messageCount++;
            
            if (lastMessageTime != null) {
                double interval = Duration.between(lastMessageTime, record.getTimestamp()).toMillis() / 1000.0;
                messageIntervals.add(interval);
            }
            
            lastMessageTime = record.getTimestamp();
            uniqueContent.add(record.getContent().toLowerCase());
            
            if (!flags.isEmpty()) {
                spamFlagCount++;
                recentFlags.addAll(flags);
                totalSpamScore += flags.stream().mapToInt(SpamFlag::getScore).sum();
            }
            
            // Keep only recent flags (last 50)
            if (recentFlags.size() > 50) {
                recentFlags.subList(0, recentFlags.size() - 50).clear();
            }
        }
        
        public void updateMetrics(MessageRecord record) {
            // This method is called during analysis
        }
        
        public double getAverageMessageInterval() {
            return messageIntervals.isEmpty() ? 0.0 : 
                messageIntervals.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        }
        
        public double getMessageIntervalVariance() {
            if (messageIntervals.size() < 2) return 0.0;
            
            double mean = getAverageMessageInterval();
            double variance = messageIntervals.stream()
                .mapToDouble(interval -> Math.pow(interval - mean, 2))
                .average().orElse(0.0);
            
            return Math.sqrt(variance);
        }
        
        public double getContentDiversityScore() {
            return messageCount == 0 ? 1.0 : (double) uniqueContent.size() / messageCount;
        }
        
        // Getters
        public int getMessageCount() { return messageCount; }
        public int getSpamFlagCount() { return spamFlagCount; }
        public double getSpamScore() { return totalSpamScore; }
        public List<SpamFlag> getRecentFlags() { return new ArrayList<>(recentFlags); }
    }
    
    // Result classes
    
    public static class SpamDetectionResult {
        private final boolean isSpam;
        private final SpamLikelihood likelihood;
        private final int score;
        private final List<SpamFlag> flags;
        
        public SpamDetectionResult(boolean isSpam, SpamLikelihood likelihood, int score, List<SpamFlag> flags) {
            this.isSpam = isSpam;
            this.likelihood = likelihood;
            this.score = score;
            this.flags = flags;
        }
        
        // Getters
        public boolean isSpam() { return isSpam; }
        public SpamLikelihood getLikelihood() { return likelihood; }
        public int getScore() { return score; }
        public List<SpamFlag> getFlags() { return flags; }
        
        public String getDescription() {
            if (!isSpam) {
                return "Message is not spam";
            }
            
            return String.format("Spam detected (Score: %d, Likelihood: %s) - %s",
                score, likelihood, 
                flags.stream().map(SpamFlag::getDescription).collect(Collectors.joining(", ")));
        }
    }
    
    public static class SpamFlag {
        private final SpamType type;
        private final String description;
        private final int score;
        
        public SpamFlag(SpamType type, String description, int score) {
            this.type = type;
            this.description = description;
            this.score = score;
        }
        
        // Getters
        public SpamType getType() { return type; }
        public String getDescription() { return description; }
        public int getScore() { return score; }
    }
    
    public static class UserSpamStats {
        private final int messageCount;
        private final int spamFlagCount;
        private final double spamScore;
        private final List<SpamFlag> recentFlags;
        
        public UserSpamStats(int messageCount, int spamFlagCount, double spamScore, List<SpamFlag> recentFlags) {
            this.messageCount = messageCount;
            this.spamFlagCount = spamFlagCount;
            this.spamScore = spamScore;
            this.recentFlags = recentFlags;
        }
        
        // Getters
        public int getMessageCount() { return messageCount; }
        public int getSpamFlagCount() { return spamFlagCount; }
        public double getSpamScore() { return spamScore; }
        public List<SpamFlag> getRecentFlags() { return recentFlags; }
    }
    
    public enum SpamLikelihood {
        VERY_LOW,
        LOW,
        MEDIUM,
        HIGH,
        VERY_HIGH
    }
    
    public enum SpamType {
        HIGH_FREQUENCY,
        IDENTICAL_MESSAGES,
        SIMILAR_MESSAGES,
        SPAM_PATTERNS,
        EXCESSIVE_MENTIONS,
        EXCESSIVE_LINKS,
        FAST_TYPING,
        SUSPICIOUS_LINKS,
        BOT_BEHAVIOR,
        REPETITIVE_CONTENT
    }
}