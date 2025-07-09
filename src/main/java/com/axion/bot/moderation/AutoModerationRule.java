package com.axion.bot.moderation;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
<<<<<<< HEAD
=======
import java.time.Duration;
>>>>>>> 7264671782849e6cd81d554807906b664cb5d408
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Represents a custom auto-moderation rule that can be applied to messages
 */
public class AutoModerationRule {
    private final String ruleId;
    private final String guildId;
    private String name;
    private String description;
    private boolean enabled;
    private RuleType ruleType;
    private String pattern;
    private Pattern compiledPattern;
    private ModerationAction action;
    private ModerationSeverity severity;
    private List<String> exemptRoles;
    private List<String> exemptUsers;
    private List<String> exemptChannels;
    private int triggerCount;
    private long triggerTimeframe; // milliseconds
    private Instant createdAt;
    private Instant lastTriggered;
    private int totalTriggers;
    
    public enum RuleType {
        REGEX_CONTENT,
        WORD_FILTER,
        LINK_FILTER,
        CAPS_FILTER,
        EMOJI_SPAM,
        MENTION_SPAM,
        DUPLICATE_MESSAGE,
        RAPID_POSTING
    }
    
    public AutoModerationRule(String ruleId, String guildId, String name, RuleType ruleType) {
        this.ruleId = ruleId;
        this.guildId = guildId;
        this.name = name;
        this.ruleType = ruleType;
        this.enabled = true;
        this.action = ModerationAction.DELETE_AND_WARN;
        this.severity = ModerationSeverity.LOW;
        this.exemptRoles = new ArrayList<>();
        this.exemptUsers = new ArrayList<>();
        this.exemptChannels = new ArrayList<>();
        this.triggerCount = 1;
        this.triggerTimeframe = 60000; // 1 minute
        this.createdAt = Instant.now();
        this.totalTriggers = 0;
    }
    
    /**
     * Evaluate if this rule should trigger for the given message
     */
    public ModerationResult evaluate(String content, UserModerationProfile profile, MessageReceivedEvent event) {
        if (!enabled) {
            return ModerationResult.allowed();
        }
        
        // Check exemptions
        if (isExempt(event)) {
            return ModerationResult.allowed();
        }
        
        // Check if rule matches
        if (!matches(content, profile, event)) {
            return ModerationResult.allowed();
        }
        
        // Check trigger frequency
        if (!shouldTrigger(profile)) {
            return ModerationResult.allowed();
        }
        
        // Rule triggered
        recordTrigger();
        profile.recordRuleViolation(this.ruleId);
        
        return ModerationResult.moderate(
            String.format("Auto-moderation rule '%s' triggered: %s", name, description),
<<<<<<< HEAD
            action,
            severity
=======
            action
>>>>>>> 7264671782849e6cd81d554807906b664cb5d408
        );
    }
    
    /**
     * Check if the content matches this rule
     */
    private boolean matches(String content, UserModerationProfile profile, MessageReceivedEvent event) {
        switch (ruleType) {
            case REGEX_CONTENT:
                return matchesRegex(content);
            case WORD_FILTER:
                return matchesWordFilter(content);
            case LINK_FILTER:
                return matchesLinkFilter(content);
            case CAPS_FILTER:
                return matchesCapsFilter(content);
            case EMOJI_SPAM:
                return matchesEmojiSpam(content);
            case MENTION_SPAM:
                return matchesMentionSpam(content);
            case DUPLICATE_MESSAGE:
                return matchesDuplicateMessage(content, profile);
            case RAPID_POSTING:
                return matchesRapidPosting(profile);
            default:
                return false;
        }
    }
    
    private boolean matchesRegex(String content) {
        if (compiledPattern == null) {
            return false;
        }
        return compiledPattern.matcher(content).find();
    }
    
    private boolean matchesWordFilter(String content) {
        if (pattern == null) return false;
        String[] words = pattern.toLowerCase().split(",");
        String lowerContent = content.toLowerCase();
        for (String word : words) {
            if (lowerContent.contains(word.trim())) {
                return true;
            }
        }
        return false;
    }
    
    private boolean matchesLinkFilter(String content) {
        Pattern linkPattern = Pattern.compile("https?://[^\\s]+");
        return linkPattern.matcher(content).find();
    }
    
    private boolean matchesCapsFilter(String content) {
        if (content.length() < 10) return false;
        long upperCount = content.chars().filter(Character::isUpperCase).count();
        double ratio = (double) upperCount / content.length();
        return ratio > 0.7; // 70% caps
    }
    
    private boolean matchesEmojiSpam(String content) {
        // Count Unicode emojis and Discord custom emojis
        long emojiCount = content.codePoints()
            .filter(cp -> cp >= 0x1F600 && cp <= 0x1F64F || // Emoticons
                         cp >= 0x1F300 && cp <= 0x1F5FF || // Misc Symbols
                         cp >= 0x1F680 && cp <= 0x1F6FF || // Transport
                         cp >= 0x2600 && cp <= 0x26FF ||   // Misc symbols
                         cp >= 0x2700 && cp <= 0x27BF)     // Dingbats
            .count();
        
        // Count Discord custom emojis <:name:id>
        long customEmojiCount = Pattern.compile("<a?:[^:]+:\\d+>").matcher(content).results().count();
        
        return (emojiCount + customEmojiCount) > 5;
    }
    
    private boolean matchesMentionSpam(String content) {
        long mentionCount = Pattern.compile("<@[!&]?\\d+>").matcher(content).results().count();
        return mentionCount > 3;
    }
    
    private boolean matchesDuplicateMessage(String content, UserModerationProfile profile) {
        return profile.hasRecentDuplicateMessage(content, triggerTimeframe);
    }
    
    private boolean matchesRapidPosting(UserModerationProfile profile) {
        return profile.getRecentMessageCount(triggerTimeframe) > triggerCount;
    }
    
    private boolean isExempt(MessageReceivedEvent event) {
        String userId = event.getAuthor().getId();
        String channelId = event.getChannel().getId();
        
        if (exemptUsers.contains(userId) || exemptChannels.contains(channelId)) {
            return true;
        }
        
        if (event.getMember() != null) {
            return event.getMember().getRoles().stream()
                .anyMatch(role -> exemptRoles.contains(role.getId()));
        }
        
        return false;
    }
    
    private boolean shouldTrigger(UserModerationProfile profile) {
<<<<<<< HEAD
        return profile.getRuleViolationCount(ruleId, triggerTimeframe) < triggerCount;
=======
        return profile.getRuleViolationCount(ruleId, Duration.ofMillis(triggerTimeframe)) < triggerCount;
>>>>>>> 7264671782849e6cd81d554807906b664cb5d408
    }
    
    private void recordTrigger() {
        this.lastTriggered = Instant.now();
        this.totalTriggers++;
    }
    
    // Getters and Setters
    public String getRuleId() { return ruleId; }
    public String getGuildId() { return guildId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public RuleType getRuleType() { return ruleType; }
    public String getPattern() { return pattern; }
    
    public void setPattern(String pattern) {
        this.pattern = pattern;
        if (ruleType == RuleType.REGEX_CONTENT && pattern != null) {
            try {
                this.compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            } catch (PatternSyntaxException e) {
                this.compiledPattern = null;
            }
        }
    }
    
    public ModerationAction getAction() { return action; }
    public void setAction(ModerationAction action) { this.action = action; }
    public ModerationSeverity getSeverity() { return severity; }
    public void setSeverity(ModerationSeverity severity) { this.severity = severity; }
    public List<String> getExemptRoles() { return new ArrayList<>(exemptRoles); }
    public void setExemptRoles(List<String> exemptRoles) { this.exemptRoles = new ArrayList<>(exemptRoles); }
    public List<String> getExemptUsers() { return new ArrayList<>(exemptUsers); }
    public void setExemptUsers(List<String> exemptUsers) { this.exemptUsers = new ArrayList<>(exemptUsers); }
    public List<String> getExemptChannels() { return new ArrayList<>(exemptChannels); }
    public void setExemptChannels(List<String> exemptChannels) { this.exemptChannels = new ArrayList<>(exemptChannels); }
    public int getTriggerCount() { return triggerCount; }
    public void setTriggerCount(int triggerCount) { this.triggerCount = Math.max(1, triggerCount); }
    public long getTriggerTimeframe() { return triggerTimeframe; }
    public void setTriggerTimeframe(long triggerTimeframe) { this.triggerTimeframe = Math.max(1000, triggerTimeframe); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getLastTriggered() { return lastTriggered; }
    public int getTotalTriggers() { return totalTriggers; }
}