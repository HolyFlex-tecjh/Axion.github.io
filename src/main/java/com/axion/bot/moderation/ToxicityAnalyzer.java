package com.axion.bot.moderation;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// Using stub Logger implementation for compilation

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// Stub Logger implementation for compilation
class Logger {
    public void info(String message, Object... args) { System.out.println(String.format(message.replace("{}", "%s"), args)); }
    public void warn(String message, Object... args) { System.out.println("WARN: " + String.format(message.replace("{}", "%s"), args)); }
    public void error(String message, Object... args) { System.err.println("ERROR: " + String.format(message.replace("{}", "%s"), args)); }
    public void error(String message, Throwable throwable) { System.err.println("ERROR: " + message + " - " + throwable.getMessage()); }
    public void debug(String message, Object... args) { System.out.println("DEBUG: " + String.format(message.replace("{}", "%s"), args)); }
}

class LoggerFactory {
    public static Logger getLogger(Class<?> clazz) { return new Logger(); }
}

// Stub classes for compilation
class ModerationResult {
    private boolean allowed;
    private String reason;
    private ModerationAction action;
    private int severityLevel;
    
    public ModerationResult(boolean allowed, String reason, ModerationAction action, int severityLevel) {
        this.allowed = allowed;
        this.reason = reason;
        this.action = action;
        this.severityLevel = severityLevel;
    }
    
    public static ModerationResult allowed() { return new ModerationResult(true, "Allowed", ModerationAction.NONE, 0); }
    public static ModerationResult custom(boolean allowed, String reason, ModerationAction action, int severityLevel) {
        return new ModerationResult(allowed, reason, action, severityLevel);
    }
    
    public boolean isAllowed() { return allowed; }
    public String getReason() { return reason; }
    public ModerationAction getAction() { return action; }
    public int getSeverityLevel() { return severityLevel; }
}

enum ModerationAction {
    NONE, DELETE_MESSAGE, WARN, TIMEOUT, KICK, BAN, TEMP_BAN, FLAG_FOR_REVIEW, LOG_ONLY, WARN_USER, DELETE_AND_TIMEOUT, DELETE_AND_WARN, SYSTEM_ACTION
}

enum ModerationSeverity {
    LOW(0), MEDIUM(1), HIGH(2), VERY_HIGH(3), CRITICAL(4);
    
    private final int level;
    
    ModerationSeverity(int level) {
        this.level = level;
    }
    
    public int getLevel() { return level; }
    
    public static ModerationSeverity fromToxicity(ToxicityAnalyzer.ToxicitySeverity toxicity) {
        switch (toxicity) {
            case MILD: return LOW;
            case MODERATE: return MEDIUM;
            case SEVERE: return HIGH;
            case VERY_HIGH: return VERY_HIGH;
            default: return LOW;
        }
    }
}



/**
 * Advanced Toxicity Analysis System
 * Provides sophisticated content analysis for detecting harmful content
 * Uses multiple detection methods while respecting user privacy
 */
public class ToxicityAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(ToxicityAnalyzer.class);
    
    // Pattern-based detection
    private final Set<Pattern> toxicPatterns;
    private final Set<Pattern> harassmentPatterns;
    private final Set<Pattern> threatPatterns;
    private final Set<Pattern> discriminationPatterns;
    private final Set<String> bannedWords;
    private final Map<String, Integer> contextualWords;
    
    // Severity scoring
    private static final int MILD_TOXICITY_THRESHOLD = 30;
    private static final int MODERATE_TOXICITY_THRESHOLD = 60;
    private static final int SEVERE_TOXICITY_THRESHOLD = 85;
    
    public ToxicityAnalyzer() {
        this.toxicPatterns = initializeToxicPatterns();
        this.harassmentPatterns = initializeHarassmentPatterns();
        this.threatPatterns = initializeThreatPatterns();
        this.discriminationPatterns = initializeDiscriminationPatterns();
        this.bannedWords = initializeBannedWords();
        this.contextualWords = initializeContextualWords();
    }
    
    /**
     * Analyze content for toxicity with trust level consideration
     */
    public ModerationResult analyzeContent(String content, UserModerationProfile.TrustLevel trustLevel) {
        ToxicityResult result = analyzeContent(content, null, null);
        
        if (!result.isToxic()) {
            return ModerationResult.allowed();
        }
        
        // Adjust action based on trust level and severity
        ModerationAction action;
        String reason = "Toxic content detected (" + result.getSeverity() + ")";
        
        switch (result.getSeverity()) {
            case MILD:
                action = (trustLevel == UserModerationProfile.TrustLevel.HIGH) ? 
                    ModerationAction.WARN_USER : ModerationAction.DELETE_AND_WARN;
                break;
            case MODERATE:
                action = ModerationAction.DELETE_AND_TIMEOUT;
                break;
            case SEVERE:
            case VERY_HIGH:
                action = ModerationAction.BAN;
                break;
            default:
                action = ModerationAction.WARN_USER;
                break;
        }
        
        return ModerationResult.custom(false, reason, action, ModerationSeverity.fromToxicity(result.getSeverity()).getLevel());
    }
    
    /**
     * Analyze content for toxicity
     */
    public ToxicityResult analyzeContent(String content, String userId, String channelId) {
        if (content == null || content.trim().isEmpty()) {
            return new ToxicityResult(false, ToxicitySeverity.NONE, 0, Collections.emptyList());
        }
        
        String normalizedContent = normalizeContent(content);
        List<ToxicityFlag> flags = new ArrayList<>();
        int totalScore = 0;
        
        // Check for direct banned words
        ToxicityFlag bannedWordFlag = checkBannedWords(normalizedContent);
        if (bannedWordFlag != null) {
            flags.add(bannedWordFlag);
            totalScore += bannedWordFlag.getScore();
        }
        
        // Check for toxic patterns
        ToxicityFlag toxicFlag = checkToxicPatterns(normalizedContent);
        if (toxicFlag != null) {
            flags.add(toxicFlag);
            totalScore += toxicFlag.getScore();
        }
        
        // Check for harassment patterns
        ToxicityFlag harassmentFlag = checkHarassmentPatterns(normalizedContent);
        if (harassmentFlag != null) {
            flags.add(harassmentFlag);
            totalScore += harassmentFlag.getScore();
        }
        
        // Check for threats
        ToxicityFlag threatFlag = checkThreatPatterns(normalizedContent);
        if (threatFlag != null) {
            flags.add(threatFlag);
            totalScore += threatFlag.getScore();
        }
        
        // Check for discrimination
        ToxicityFlag discriminationFlag = checkDiscriminationPatterns(normalizedContent);
        if (discriminationFlag != null) {
            flags.add(discriminationFlag);
            totalScore += discriminationFlag.getScore();
        }
        
        // Contextual analysis
        int contextualScore = analyzeContextualToxicity(normalizedContent);
        totalScore += contextualScore;
        
        if (contextualScore > 0) {
            flags.add(new ToxicityFlag(
                ToxicityType.CONTEXTUAL,
                "Contextual toxicity detected",
                contextualScore
            ));
        }
        
        // Determine severity
        ToxicitySeverity severity = determineSeverity(totalScore);
        boolean isToxic = totalScore >= MILD_TOXICITY_THRESHOLD;
        
        if (isToxic) {
            logger.debug("Toxicity detected - User: {}, Channel: {}, Score: {}, Severity: {}", 
                userId, channelId, totalScore, severity);
        }
        
        return new ToxicityResult(isToxic, severity, totalScore, flags);
    }
    
    /**
     * Check for banned words
     */
    private ToxicityFlag checkBannedWords(String content) {
        String[] words = content.split("\\s+");
        List<String> foundWords = new ArrayList<>();
        
        for (String word : words) {
            String cleanWord = word.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
            if (bannedWords.contains(cleanWord)) {
                foundWords.add(cleanWord);
            }
        }
        
        if (!foundWords.isEmpty()) {
            int score = Math.min(foundWords.size() * 25, 75); // Cap at 75
            return new ToxicityFlag(
                ToxicityType.BANNED_WORDS,
                "Banned words detected: " + foundWords.size(),
                score
            );
        }
        
        return null;
    }
    
    /**
     * Check for toxic patterns
     */
    private ToxicityFlag checkToxicPatterns(String content) {
        int matchCount = 0;
        
        for (Pattern pattern : toxicPatterns) {
            if (pattern.matcher(content).find()) {
                matchCount++;
            }
        }
        
        if (matchCount > 0) {
            int score = Math.min(matchCount * 20, 60);
            return new ToxicityFlag(
                ToxicityType.TOXIC_PATTERN,
                "Toxic patterns detected: " + matchCount,
                score
            );
        }
        
        return null;
    }
    
    /**
     * Check for harassment patterns
     */
    private ToxicityFlag checkHarassmentPatterns(String content) {
        int matchCount = 0;
        
        for (Pattern pattern : harassmentPatterns) {
            if (pattern.matcher(content).find()) {
                matchCount++;
            }
        }
        
        if (matchCount > 0) {
            int score = Math.min(matchCount * 30, 70);
            return new ToxicityFlag(
                ToxicityType.HARASSMENT,
                "Harassment patterns detected: " + matchCount,
                score
            );
        }
        
        return null;
    }
    
    /**
     * Check for threat patterns
     */
    private ToxicityFlag checkThreatPatterns(String content) {
        int matchCount = 0;
        
        for (Pattern pattern : threatPatterns) {
            if (pattern.matcher(content).find()) {
                matchCount++;
            }
        }
        
        if (matchCount > 0) {
            int score = Math.min(matchCount * 40, 90); // Threats are serious
            return new ToxicityFlag(
                ToxicityType.THREAT,
                "Threat patterns detected: " + matchCount,
                score
            );
        }
        
        return null;
    }
    
    /**
     * Check for discrimination patterns
     */
    private ToxicityFlag checkDiscriminationPatterns(String content) {
        int matchCount = 0;
        
        for (Pattern pattern : discriminationPatterns) {
            if (pattern.matcher(content).find()) {
                matchCount++;
            }
        }
        
        if (matchCount > 0) {
            int score = Math.min(matchCount * 35, 80);
            return new ToxicityFlag(
                ToxicityType.DISCRIMINATION,
                "Discrimination patterns detected: " + matchCount,
                score
            );
        }
        
        return null;
    }
    
    /**
     * Analyze contextual toxicity using word combinations and context
     */
    private int analyzeContextualToxicity(String content) {
        String[] words = content.split("\\s+");
        int score = 0;
        
        // Check for contextual word combinations
        for (int i = 0; i < words.length; i++) {
            String word = words[i].toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
            
            if (contextualWords.containsKey(word)) {
                int baseScore = contextualWords.get(word);
                
                // Check context around the word
                String context = getWordContext(words, i, 2);
                
                // Amplify score based on context
                if (hasNegativeContext(context)) {
                    score += baseScore * 2;
                } else {
                    score += baseScore;
                }
            }
        }
        
        // Check for excessive capitalization (shouting)
        if (isExcessiveCapitalization(content)) {
            score += 10;
        }
        
        // Check for excessive punctuation (aggressive tone)
        if (hasExcessivePunctuation(content)) {
            score += 5;
        }
        
        return Math.min(score, 50); // Cap contextual score
    }
    
    /**
     * Get context around a word
     */
    private String getWordContext(String[] words, int index, int radius) {
        int start = Math.max(0, index - radius);
        int end = Math.min(words.length, index + radius + 1);
        
        StringBuilder context = new StringBuilder();
        for (int i = start; i < end; i++) {
            if (i != index) {
                context.append(words[i]).append(" ");
            }
        }
        
        return context.toString().toLowerCase();
    }
    
    /**
     * Check if context has negative indicators
     */
    private boolean hasNegativeContext(String context) {
        String[] negativeIndicators = {
            "hate", "kill", "die", "stupid", "idiot", "moron",
            "shut up", "go away", "nobody cares", "worthless"
        };
        
        for (String indicator : negativeIndicators) {
            if (context.contains(indicator)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check for excessive capitalization
     */
    private boolean isExcessiveCapitalization(String content) {
        if (content.length() < 10) return false;
        
        long upperCaseCount = content.chars()
            .filter(Character::isUpperCase)
            .count();
        
        double ratio = (double) upperCaseCount / content.length();
        return ratio > 0.6; // More than 60% uppercase
    }
    
    /**
     * Check for excessive punctuation
     */
    private boolean hasExcessivePunctuation(String content) {
        return content.matches(".*[!?]{3,}.*") || // Multiple exclamation/question marks
               content.matches(".*[.]{4,}.*");     // Multiple dots
    }
    
    /**
     * Determine severity based on score
     */
    private ToxicitySeverity determineSeverity(int score) {
        if (score >= SEVERE_TOXICITY_THRESHOLD) {
            return ToxicitySeverity.SEVERE;
        } else if (score >= MODERATE_TOXICITY_THRESHOLD) {
            return ToxicitySeverity.MODERATE;
        } else if (score >= MILD_TOXICITY_THRESHOLD) {
            return ToxicitySeverity.MILD;
        } else {
            return ToxicitySeverity.NONE;
        }
    }
    
    /**
     * Normalize content for analysis
     */
    private String normalizeContent(String content) {
        return content.toLowerCase()
            .replaceAll("[0@]", "o")
            .replaceAll("[1!|]", "i")
            .replaceAll("[3]", "e")
            .replaceAll("[4]", "a")
            .replaceAll("[5]", "s")
            .replaceAll("[7]", "t")
            .replaceAll("\\s+", " ")
            .trim();
    }
    
    /**
     * Initialize toxic patterns
     */
    private Set<Pattern> initializeToxicPatterns() {
        Set<Pattern> patterns = new HashSet<>();
        
        // General toxicity patterns (keeping it appropriate)
        patterns.add(Pattern.compile("\\b(you\\s+suck|go\\s+die|kill\\s+yourself)\\b", Pattern.CASE_INSENSITIVE));
        patterns.add(Pattern.compile("\\b(nobody\\s+likes\\s+you|you\\s+are\\s+worthless)\\b", Pattern.CASE_INSENSITIVE));
        patterns.add(Pattern.compile("\\b(shut\\s+up|get\\s+lost|go\\s+away)\\b", Pattern.CASE_INSENSITIVE));
        
        return patterns;
    }
    
    /**
     * Initialize harassment patterns
     */
    private Set<Pattern> initializeHarassmentPatterns() {
        Set<Pattern> patterns = new HashSet<>();
        
        patterns.add(Pattern.compile("\\b(stop\\s+messaging\\s+me|leave\\s+me\\s+alone)\\b", Pattern.CASE_INSENSITIVE));
        patterns.add(Pattern.compile("\\b(you\\s+are\\s+annoying|stop\\s+bothering)\\b", Pattern.CASE_INSENSITIVE));
        patterns.add(Pattern.compile("\\b(i\\s+will\\s+report\\s+you|this\\s+is\\s+harassment)\\b", Pattern.CASE_INSENSITIVE));
        
        return patterns;
    }
    
    /**
     * Initialize threat patterns
     */
    private Set<Pattern> initializeThreatPatterns() {
        Set<Pattern> patterns = new HashSet<>();
        
        patterns.add(Pattern.compile("\\b(i\\s+will\\s+find\\s+you|i\\s+know\\s+where\\s+you\\s+live)\\b", Pattern.CASE_INSENSITIVE));
        patterns.add(Pattern.compile("\\b(i\\s+will\\s+hurt|i\\s+will\\s+get\\s+you)\\b", Pattern.CASE_INSENSITIVE));
        patterns.add(Pattern.compile("\\b(you\\s+better\\s+watch|i\\s+will\\s+make\\s+you)\\b", Pattern.CASE_INSENSITIVE));
        
        return patterns;
    }
    
    /**
     * Initialize discrimination patterns
     */
    private Set<Pattern> initializeDiscriminationPatterns() {
        Set<Pattern> patterns = new HashSet<>();
        
        // Note: These are very basic patterns. In a real implementation,
        // you would want more sophisticated detection
        patterns.add(Pattern.compile("\\b(all\\s+\\w+\\s+are\\s+bad|\\w+\\s+people\\s+are\\s+stupid)\\b", Pattern.CASE_INSENSITIVE));
        patterns.add(Pattern.compile("\\b(i\\s+hate\\s+all|they\\s+should\\s+all)\\b", Pattern.CASE_INSENSITIVE));
        
        return patterns;
    }
    
    /**
     * Initialize banned words (keeping it clean for example)
     */
    private Set<String> initializeBannedWords() {
        Set<String> words = new HashSet<>();
        
        // Add common inappropriate words (this is a very basic list)
        words.add("spam");
        words.add("scam");
        words.add("hack");
        words.add("cheat");
        
        // In a real implementation, you would have a more comprehensive list
        // loaded from a configuration file or database
        
        return words;
    }
    
    /**
     * Initialize contextual words with base scores
     */
    private Map<String, Integer> initializeContextualWords() {
        Map<String, Integer> words = new HashMap<>();
        
        // Words that can be toxic depending on context
        words.put("stupid", 15);
        words.put("idiot", 20);
        words.put("moron", 20);
        words.put("dumb", 10);
        words.put("loser", 15);
        words.put("pathetic", 18);
        words.put("worthless", 25);
        words.put("useless", 15);
        words.put("annoying", 12);
        words.put("disgusting", 20);
        
        return words;
    }
    
    // Result classes
    
    public static class ToxicityResult {
        private final boolean isToxic;
        private final ToxicitySeverity severity;
        private final int score;
        private final List<ToxicityFlag> flags;
        
        public ToxicityResult(boolean isToxic, ToxicitySeverity severity, int score, List<ToxicityFlag> flags) {
            this.isToxic = isToxic;
            this.severity = severity;
            this.score = score;
            this.flags = flags;
        }
        
        // Getters
        public boolean isToxic() { return isToxic; }
        public ToxicitySeverity getSeverity() { return severity; }
        public int getScore() { return score; }
        public List<ToxicityFlag> getFlags() { return flags; }
        
        public String getDescription() {
            if (!isToxic) {
                return "Content is clean";
            }
            
            return String.format("Toxicity detected (Score: %d, Severity: %s) - %s",
                score, severity, 
                flags.stream().map(ToxicityFlag::getDescription).collect(Collectors.joining(", ")));
        }
    }
    
    public static class ToxicityFlag {
        private final ToxicityType type;
        private final String description;
        private final int score;
        
        public ToxicityFlag(ToxicityType type, String description, int score) {
            this.type = type;
            this.description = description;
            this.score = score;
        }
        
        // Getters
        public ToxicityType getType() { return type; }
        public String getDescription() { return description; }
        public int getScore() { return score; }
    }
    
    public enum ToxicitySeverity {
        NONE,
        MILD,
        MODERATE,
        SEVERE,
        VERY_HIGH
    }
    
    public enum ToxicityType {
        BANNED_WORDS,
        TOXIC_PATTERN,
        HARASSMENT,
        THREAT,
        DISCRIMINATION,
        CONTEXTUAL,
        SPAM,
        INAPPROPRIATE_CONTENT
    }
}