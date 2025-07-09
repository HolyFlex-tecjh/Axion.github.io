package com.axion.bot.moderation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Threat Intelligence System
 * Provides advanced threat detection and intelligence gathering
 * Maintains databases of known threats and suspicious patterns
 */
public class ThreatIntelligence {
    private static final Logger logger = LoggerFactory.getLogger(ThreatIntelligence.class);
    
    // Threat databases
    private final Map<String, ThreatEntry> knownThreats = new ConcurrentHashMap<>();
    private final Map<String, SuspiciousPattern> suspiciousPatterns = new ConcurrentHashMap<>();
    private final Map<String, MaliciousDomain> maliciousDomains = new ConcurrentHashMap<>();
    private final Map<String, UserThreatProfile> userThreatProfiles = new ConcurrentHashMap<>();
    
    // Pattern matching
    private final Set<Pattern> phishingPatterns;
    private final Set<Pattern> malwarePatterns;
    private final Set<Pattern> scamPatterns;
    private final Set<Pattern> doxxingPatterns;
    
    // Configuration
    private static final int THREAT_SCORE_THRESHOLD = 50;
    private static final Duration THREAT_CACHE_DURATION = Duration.ofHours(24);
    
    public ThreatIntelligence() {
        this.phishingPatterns = initializePhishingPatterns();
        this.malwarePatterns = initializeMalwarePatterns();
        this.scamPatterns = initializeScamPatterns();
        this.doxxingPatterns = initializeDoxxingPatterns();
        
        initializeKnownThreats();
        initializeMaliciousDomains();
    }
    
    /**
     * Analyze content for threats
     */
    public ThreatAnalysisResult analyzeContent(String content, String userId, String channelId) {
        if (content == null || content.trim().isEmpty()) {
            return new ThreatAnalysisResult(false, ThreatLevel.NONE, 0, Collections.emptyList());
        }
        
        List<ThreatFlag> flags = new ArrayList<>();
        int totalScore = 0;
        
        // Check against known threats
        ThreatFlag knownThreatFlag = checkKnownThreats(content);
        if (knownThreatFlag != null) {
            flags.add(knownThreatFlag);
            totalScore += knownThreatFlag.getScore();
        }
        
        // Check for phishing attempts
        ThreatFlag phishingFlag = checkPhishingPatterns(content);
        if (phishingFlag != null) {
            flags.add(phishingFlag);
            totalScore += phishingFlag.getScore();
        }
        
        // Check for malware distribution
        ThreatFlag malwareFlag = checkMalwarePatterns(content);
        if (malwareFlag != null) {
            flags.add(malwareFlag);
            totalScore += malwareFlag.getScore();
        }
        
        // Check for scam attempts
        ThreatFlag scamFlag = checkScamPatterns(content);
        if (scamFlag != null) {
            flags.add(scamFlag);
            totalScore += scamFlag.getScore();
        }
        
        // Check for doxxing attempts
        ThreatFlag doxxingFlag = checkDoxxingPatterns(content);
        if (doxxingFlag != null) {
            flags.add(doxxingFlag);
            totalScore += doxxingFlag.getScore();
        }
        
        // Check malicious domains
        ThreatFlag domainFlag = checkMaliciousDomains(content);
        if (domainFlag != null) {
            flags.add(domainFlag);
            totalScore += domainFlag.getScore();
        }
        
        // Check suspicious patterns
        ThreatFlag patternFlag = checkSuspiciousPatterns(content);
        if (patternFlag != null) {
            flags.add(patternFlag);
            totalScore += patternFlag.getScore();
        }
        
        // Update user threat profile
        updateUserThreatProfile(userId, flags, totalScore);
        
        // Determine threat level
        ThreatLevel level = determineThreatLevel(totalScore);
        boolean isThreat = totalScore >= THREAT_SCORE_THRESHOLD;
        
        if (isThreat) {
            logger.warn("Threat detected - User: {}, Channel: {}, Score: {}, Level: {}", 
                userId, channelId, totalScore, level);
        }
        
        return new ThreatAnalysisResult(isThreat, level, totalScore, flags);
    }
    
    /**
     * Check against known threats database
     */
    private ThreatFlag checkKnownThreats(String content) {
        String normalizedContent = content.toLowerCase();
        
        for (ThreatEntry threat : knownThreats.values()) {
            if (threat.matches(normalizedContent)) {
                return new ThreatFlag(
                    threat.getType(),
                    "Known threat detected: " + threat.getDescription(),
                    threat.getSeverityScore()
                );
            }
        }
        
        return null;
    }
    
    /**
     * Check for phishing patterns
     */
    private ThreatFlag checkPhishingPatterns(String content) {
        int matchCount = 0;
        List<String> matchedPatterns = new ArrayList<>();
        
        for (Pattern pattern : phishingPatterns) {
            if (pattern.matcher(content).find()) {
                matchCount++;
                matchedPatterns.add(pattern.pattern());
            }
        }
        
        if (matchCount > 0) {
            int score = Math.min(matchCount * 30, 90);
            return new ThreatFlag(
                ThreatType.PHISHING,
                "Phishing patterns detected: " + matchCount,
                score
            );
        }
        
        return null;
    }
    
    /**
     * Check for malware patterns
     */
    private ThreatFlag checkMalwarePatterns(String content) {
        int matchCount = 0;
        
        for (Pattern pattern : malwarePatterns) {
            if (pattern.matcher(content).find()) {
                matchCount++;
            }
        }
        
        if (matchCount > 0) {
            int score = Math.min(matchCount * 40, 100);
            return new ThreatFlag(
                ThreatType.MALWARE,
                "Malware distribution patterns detected: " + matchCount,
                score
            );
        }
        
        return null;
    }
    
    /**
     * Check for scam patterns
     */
    private ThreatFlag checkScamPatterns(String content) {
        int matchCount = 0;
        
        for (Pattern pattern : scamPatterns) {
            if (pattern.matcher(content).find()) {
                matchCount++;
            }
        }
        
        if (matchCount > 0) {
            int score = Math.min(matchCount * 25, 75);
            return new ThreatFlag(
                ThreatType.SCAM,
                "Scam patterns detected: " + matchCount,
                score
            );
        }
        
        return null;
    }
    
    /**
     * Check for doxxing patterns
     */
    private ThreatFlag checkDoxxingPatterns(String content) {
        int matchCount = 0;
        
        for (Pattern pattern : doxxingPatterns) {
            if (pattern.matcher(content).find()) {
                matchCount++;
            }
        }
        
        if (matchCount > 0) {
            int score = Math.min(matchCount * 35, 85);
            return new ThreatFlag(
                ThreatType.DOXXING,
                "Doxxing patterns detected: " + matchCount,
                score
            );
        }
        
        return null;
    }
    
    /**
     * Check for malicious domains
     */
    private ThreatFlag checkMaliciousDomains(String content) {
        Pattern urlPattern = Pattern.compile("https?://([^/\\s]+)", Pattern.CASE_INSENSITIVE);
        
        List<String> foundDomains = new ArrayList<>();
        final int[] totalScore = {0};
        
        urlPattern.matcher(content).results().forEach(match -> {
            String domain = match.group(1).toLowerCase();
            MaliciousDomain maliciousDomain = maliciousDomains.get(domain);
            if (maliciousDomain != null) {
                foundDomains.add(domain);
                totalScore[0] += maliciousDomain.getThreatScore();
            }
        });
        
        if (!foundDomains.isEmpty()) {
            return new ThreatFlag(
                ThreatType.MALICIOUS_LINK,
                "Malicious domains detected: " + foundDomains.size(),
                Math.min(totalScore[0], 95)
            );
        }
        
        return null;
    }
    
    /**
     * Check for suspicious patterns
     */
    private ThreatFlag checkSuspiciousPatterns(String content) {
        int suspicionScore = 0;
        List<String> matchedPatterns = new ArrayList<>();
        
        for (SuspiciousPattern pattern : suspiciousPatterns.values()) {
            if (pattern.matches(content)) {
                suspicionScore += pattern.getScore();
                matchedPatterns.add(pattern.getDescription());
            }
        }
        
        if (suspicionScore > 20) {
            return new ThreatFlag(
                ThreatType.SUSPICIOUS_ACTIVITY,
                "Suspicious patterns detected: " + matchedPatterns.size(),
                Math.min(suspicionScore, 60)
            );
        }
        
        return null;
    }
    
    /**
     * Update user threat profile
     */
    private void updateUserThreatProfile(String userId, List<ThreatFlag> flags, int score) {
        UserThreatProfile profile = userThreatProfiles.computeIfAbsent(userId, k -> new UserThreatProfile());
        profile.updateProfile(flags, score);
    }
    
    /**
     * Determine threat level based on score
     */
    private ThreatLevel determineThreatLevel(int score) {
        if (score >= 90) {
            return ThreatLevel.VERY_HIGH;
        } else if (score >= 70) {
            return ThreatLevel.HIGH;
        } else if (score >= 50) {
            return ThreatLevel.MEDIUM;
        } else if (score >= 30) {
            return ThreatLevel.LOW;
        } else {
            return ThreatLevel.NONE;
        }
    }
    
    /**
     * Add new threat to database
     */
    public void addThreat(String identifier, ThreatType type, String description, int severity) {
        ThreatEntry threat = new ThreatEntry(identifier, type, description, severity, Instant.now());
        knownThreats.put(identifier, threat);
        logger.info("Added new threat: {} (Type: {}, Severity: {})", description, type, severity);
    }
    
    /**
     * Add malicious domain
     */
    public void addMaliciousDomain(String domain, ThreatType type, int threatScore, String source) {
        MaliciousDomain maliciousDomain = new MaliciousDomain(domain, type, threatScore, source, Instant.now());
        maliciousDomains.put(domain.toLowerCase(), maliciousDomain);
        logger.info("Added malicious domain: {} (Score: {}, Source: {})", domain, threatScore, source);
    }
    
    /**
     * Get user threat profile
     */
    public UserThreatProfile getUserThreatProfile(String userId) {
        return userThreatProfiles.getOrDefault(userId, new UserThreatProfile());
    }
    
    /**
     * Check if user is high risk
     */
    public boolean isHighRiskUser(String userId) {
        UserThreatProfile profile = userThreatProfiles.get(userId);
        return profile != null && profile.getThreatScore() >= 100;
    }
    
    /**
     * Clean expired threats
     */
    public void cleanExpiredThreats() {
        Instant cutoff = Instant.now().minus(THREAT_CACHE_DURATION);
        
        knownThreats.entrySet().removeIf(entry -> 
            entry.getValue().getTimestamp().isBefore(cutoff));
        
        maliciousDomains.entrySet().removeIf(entry -> 
            entry.getValue().getTimestamp().isBefore(cutoff));
        
        logger.debug("Cleaned expired threats from cache");
    }
    
    /**
     * Initialize phishing patterns
     */
    private Set<Pattern> initializePhishingPatterns() {
        Set<Pattern> patterns = new HashSet<>();
        
        // Discord phishing patterns
        patterns.add(Pattern.compile("\\b(free\\s+nitro|discord\\s+nitro\\s+free)\\b", Pattern.CASE_INSENSITIVE));
        patterns.add(Pattern.compile("\\b(steam\\s+gift|free\\s+steam)\\b", Pattern.CASE_INSENSITIVE));
        patterns.add(Pattern.compile("\\b(verify\\s+your\\s+account|account\\s+suspended)\\b", Pattern.CASE_INSENSITIVE));
        patterns.add(Pattern.compile("\\b(click\\s+to\\s+verify|urgent\\s+action\\s+required)\\b", Pattern.CASE_INSENSITIVE));
        
        // Generic phishing patterns
        patterns.add(Pattern.compile("\\b(login\\s+here|sign\\s+in\\s+now)\\b", Pattern.CASE_INSENSITIVE));
        patterns.add(Pattern.compile("\\b(update\\s+payment|billing\\s+issue)\\b", Pattern.CASE_INSENSITIVE));
        
        return patterns;
    }
    
    /**
     * Initialize malware patterns
     */
    private Set<Pattern> initializeMalwarePatterns() {
        Set<Pattern> patterns = new HashSet<>();
        
        patterns.add(Pattern.compile("\\b(download\\s+hack|free\\s+cheat)\\b", Pattern.CASE_INSENSITIVE));
        patterns.add(Pattern.compile("\\b(crack\\s+download|keygen)\\b", Pattern.CASE_INSENSITIVE));
        patterns.add(Pattern.compile("\\b(virus\\s+free|100%\\s+safe)\\b", Pattern.CASE_INSENSITIVE));
        patterns.add(Pattern.compile("\\.(exe|scr|bat|cmd|pif)\\b", Pattern.CASE_INSENSITIVE));
        
        return patterns;
    }
    
    /**
     * Initialize scam patterns
     */
    private Set<Pattern> initializeScamPatterns() {
        Set<Pattern> patterns = new HashSet<>();
        
        patterns.add(Pattern.compile("\\b(send\\s+money|wire\\s+transfer)\\b", Pattern.CASE_INSENSITIVE));
        patterns.add(Pattern.compile("\\b(investment\\s+opportunity|guaranteed\\s+profit)\\b", Pattern.CASE_INSENSITIVE));
        patterns.add(Pattern.compile("\\b(crypto\\s+giveaway|bitcoin\\s+doubler)\\b", Pattern.CASE_INSENSITIVE));
        patterns.add(Pattern.compile("\\b(nigerian\\s+prince|inheritance)\\b", Pattern.CASE_INSENSITIVE));
        
        return patterns;
    }
    
    /**
     * Initialize doxxing patterns
     */
    private Set<Pattern> initializeDoxxingPatterns() {
        Set<Pattern> patterns = new HashSet<>();
        
        // Personal information patterns
        patterns.add(Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b")); // SSN pattern
        patterns.add(Pattern.compile("\\b\\d{4}\\s+\\d{4}\\s+\\d{4}\\s+\\d{4}\\b")); // Credit card pattern
        patterns.add(Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b")); // Email pattern
        patterns.add(Pattern.compile("\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b")); // IP address pattern
        
        // Address patterns
        patterns.add(Pattern.compile("\\b\\d+\\s+[A-Za-z\\s]+\\s+(Street|St|Avenue|Ave|Road|Rd|Drive|Dr)\\b", Pattern.CASE_INSENSITIVE));
        
        return patterns;
    }
    
    /**
     * Initialize known threats database
     */
    private void initializeKnownThreats() {
        // Add some example threats
        addThreat("discord-nitro-scam", ThreatType.PHISHING, "Discord Nitro scam", 80);
        addThreat("steam-phishing", ThreatType.PHISHING, "Steam account phishing", 75);
        addThreat("crypto-doubler", ThreatType.SCAM, "Cryptocurrency doubler scam", 70);
    }
    
    /**
     * Initialize malicious domains database
     */
    private void initializeMaliciousDomains() {
        // Add some example malicious domains
        addMaliciousDomain("malicious-example.com", ThreatType.PHISHING, 90, "Threat Intelligence");
        addMaliciousDomain("scam-site.net", ThreatType.SCAM, 85, "Community Report");
        addMaliciousDomain("fake-discord.org", ThreatType.PHISHING, 95, "Security Research");
    }
    
    // Data classes
    
    private static class ThreatEntry {
        private final String identifier;
        private final ThreatType type;
        private final String description;
        private final int severityScore;
        private final Instant timestamp;
        
        public ThreatEntry(String identifier, ThreatType type, String description, int severityScore, Instant timestamp) {
            this.identifier = identifier;
            this.type = type;
            this.description = description;
            this.severityScore = severityScore;
            this.timestamp = timestamp;
        }
        
        public boolean matches(String content) {
            return content.contains(identifier.toLowerCase());
        }
        
        // Getters
        public ThreatType getType() { return type; }
        public String getDescription() { return description; }
        public int getSeverityScore() { return severityScore; }
        public Instant getTimestamp() { return timestamp; }
    }
    
    private static class SuspiciousPattern {
        private final String pattern;
        private final String description;
        private final int score;
        
        public SuspiciousPattern(String pattern, String description, int score) {
            this.pattern = pattern;
            this.description = description;
            this.score = score;
        }
        
        public boolean matches(String content) {
            return content.toLowerCase().contains(pattern.toLowerCase());
        }
        
        // Getters
        public String getDescription() { return description; }
        public int getScore() { return score; }
    }
    
    private static class MaliciousDomain {
        private final int threatScore;
        private final Instant timestamp;
        
        public MaliciousDomain(String domain, ThreatType type, int threatScore, String source, Instant timestamp) {
            this.threatScore = threatScore;
            this.timestamp = timestamp;
        }
        
        // Getters
        public int getThreatScore() { return threatScore; }
        public Instant getTimestamp() { return timestamp; }
    }
    
    public static class UserThreatProfile {
        private int threatScore = 0;
        private int threatCount = 0;
        private final Map<ThreatType, Integer> threatTypeCounts = new HashMap<>();
        private final List<ThreatFlag> recentThreats = new ArrayList<>();
        private Instant lastThreatTime;
        
        public void updateProfile(List<ThreatFlag> flags, int score) {
            if (!flags.isEmpty()) {
                threatScore += score;
                threatCount++;
                lastThreatTime = Instant.now();
                
                for (ThreatFlag flag : flags) {
                    threatTypeCounts.merge(flag.getType(), 1, Integer::sum);
                    recentThreats.add(flag);
                }
                
                // Keep only recent threats (last 20)
                if (recentThreats.size() > 20) {
                    recentThreats.subList(0, recentThreats.size() - 20).clear();
                }
            }
        }
        
        public boolean isHighRisk() {
            return threatScore >= 100 || threatCount >= 5;
        }
        
        // Getters
        public int getThreatScore() { return threatScore; }
        public int getThreatCount() { return threatCount; }
        public Map<ThreatType, Integer> getThreatTypeCounts() { return new HashMap<>(threatTypeCounts); }
        public List<ThreatFlag> getRecentThreats() { return new ArrayList<>(recentThreats); }
        public Instant getLastThreatTime() { return lastThreatTime; }
    }
    
    // Result classes
    
    public static class ThreatAnalysisResult {
        private final boolean isThreat;
        private final ThreatLevel level;
        private final int score;
        private final List<ThreatFlag> flags;
        
        public ThreatAnalysisResult(boolean isThreat, ThreatLevel level, int score, List<ThreatFlag> flags) {
            this.isThreat = isThreat;
            this.level = level;
            this.score = score;
            this.flags = flags;
        }
        
        // Getters
        public boolean isThreat() { return isThreat; }
        public ThreatLevel getLevel() { return level; }
        public int getScore() { return score; }
        public List<ThreatFlag> getFlags() { return flags; }
        
        public String getDescription() {
            if (!isThreat) {
                return "No threats detected";
            }
            
            return String.format("Threat detected (Score: %d, Level: %s) - %s",
                score, level, 
                flags.stream().map(ThreatFlag::getDescription).collect(Collectors.joining(", ")));
        }
    }
    
    public static class ThreatFlag {
        private final ThreatType type;
        private final String description;
        private final int score;
        
        public ThreatFlag(ThreatType type, String description, int score) {
            this.type = type;
            this.description = description;
            this.score = score;
        }
        
        // Getters
        public ThreatType getType() { return type; }
        public String getDescription() { return description; }
        public int getScore() { return score; }
    }
    
    public enum ThreatLevel {
        NONE,
        LOW,
        MEDIUM,
        HIGH,
        VERY_HIGH
    }
    
    public enum ThreatType {
        PHISHING,
        MALWARE,
        SCAM,
        DOXXING,
        MALICIOUS_LINK,
        SUSPICIOUS_ACTIVITY,
        COORDINATED_ATTACK
    }
}