package com.axion.bot.moderation;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Advanced raid protection system that detects and mitigates various types of raids
 * including coordinated attacks, bot raids, and mass join events.
 */
public class AdvancedRaidProtection {
    private static final Logger logger = LoggerFactory.getLogger(AdvancedRaidProtection.class);
    
    // Raid detection tracking
    private final Map<String, List<JoinEvent>> recentJoins = new ConcurrentHashMap<>();
    private final Map<String, RaidDetectionState> guildStates = new ConcurrentHashMap<>();
    private final Map<String, List<SuspiciousActivity>> suspiciousActivities = new ConcurrentHashMap<>();
    
    // Pattern analysis
    private final Map<String, Set<String>> suspiciousPatterns = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> raidCounters = new ConcurrentHashMap<>();
    
    // Background processing
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    
    // Configuration
    private final RaidProtectionConfig config;
    
    public AdvancedRaidProtection(RaidProtectionConfig config) {
        this.config = config;
        startBackgroundTasks();
    }
    
    /**
     * Analyzes a member join event for potential raid activity
     */
    public RaidAnalysisResult analyzeMemberJoin(GuildMemberJoinEvent event) {
        Guild guild = event.getGuild();
        Member member = event.getMember();
        User user = event.getUser();
        String guildId = guild.getId();
        String userId = user.getId();
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Record the join event
            JoinEvent joinEvent = new JoinEvent(userId, user.getName(), 
                user.getTimeCreated().toInstant(), Instant.now());
            recordJoinEvent(guildId, joinEvent);
            
            // Get current detection state
            RaidDetectionState state = guildStates.computeIfAbsent(guildId, 
                k -> new RaidDetectionState());
            
            List<String> detectedPatterns = new ArrayList<>();
            List<String> indicators = new ArrayList<>();
            double riskScore = 0.0;
            RaidType detectedRaidType = RaidType.NONE;
            
            // Analyze join patterns
            RaidPatternAnalysis patternAnalysis = analyzeJoinPatterns(guildId);
            if (patternAnalysis.isRaidDetected()) {
                detectedPatterns.addAll(patternAnalysis.getPatterns());
                indicators.addAll(patternAnalysis.getIndicators());
                riskScore = Math.max(riskScore, patternAnalysis.getRiskScore());
                detectedRaidType = patternAnalysis.getRaidType();
            }
            
            // Analyze user characteristics
            UserAnalysisResult userAnalysis = analyzeUserCharacteristics(user);
            if (userAnalysis.isSuspicious()) {
                detectedPatterns.add("SUSPICIOUS_USER");
                indicators.addAll(userAnalysis.getIndicators());
                riskScore = Math.max(riskScore, userAnalysis.getRiskScore());
            }
            
            // Check for coordinated activity
            CoordinationAnalysis coordAnalysis = analyzeCoordination(guildId, joinEvent);
            if (coordAnalysis.isCoordinated()) {
                detectedPatterns.add("COORDINATED_ACTIVITY");
                indicators.addAll(coordAnalysis.getIndicators());
                riskScore = Math.max(riskScore, coordAnalysis.getRiskScore());
                if (detectedRaidType == RaidType.NONE) {
                    detectedRaidType = RaidType.COORDINATED;
                }
            }
            
            // Update guild state
            state.updateState(detectedPatterns, riskScore);
            
            long analysisTime = System.currentTimeMillis() - startTime;
            
            if (detectedPatterns.isEmpty()) {
                return RaidAnalysisResult.noRaid(userId, analysisTime);
            } else {
                // Record suspicious activity
                recordSuspiciousActivity(guildId, new SuspiciousActivity(
                    userId, detectedPatterns, riskScore, Instant.now()
                ));
                
                return RaidAnalysisResult.raidDetected(
                    userId, detectedRaidType, detectedPatterns, indicators,
                    riskScore, generateRecommendations(detectedRaidType, riskScore),
                    analysisTime
                );
            }
            
        } catch (Exception e) {
            logger.error("Error during raid analysis for guild {}", guildId, e);
            return RaidAnalysisResult.raidDetected(
                userId, RaidType.UNKNOWN, Collections.singletonList("ANALYSIS_ERROR"),
                Collections.singletonList("System error during analysis"),
                1.0, Collections.singletonList("Manual review required"),
                System.currentTimeMillis() - startTime
            );
        }
    }
    
    /**
     * Gets the current raid status for a guild
     */
    public RaidStatus getRaidStatus(String guildId) {
        RaidDetectionState state = guildStates.get(guildId);
        if (state == null) {
            return new RaidStatus(false, RaidType.NONE, 0.0, Collections.emptyList());
        }
        
        return new RaidStatus(
            state.isRaidActive(),
            state.getCurrentRaidType(),
            state.getCurrentRiskScore(),
            state.getActivePatterns()
        );
    }
    
    /**
     * Manually triggers raid protection for a guild
     */
    public void activateRaidProtection(String guildId, RaidType raidType, String reason) {
        RaidDetectionState state = guildStates.computeIfAbsent(guildId, 
            k -> new RaidDetectionState());
        
        state.activateRaidProtection(raidType, reason);
        logger.warn("Raid protection activated for guild {}: {} - {}", guildId, raidType, reason);
    }
    
    /**
     * Deactivates raid protection for a guild
     */
    public void deactivateRaidProtection(String guildId) {
        RaidDetectionState state = guildStates.get(guildId);
        if (state != null) {
            state.deactivateRaidProtection();
            logger.info("Raid protection deactivated for guild {}", guildId);
        }
    }
    
    /**
     * Gets raid protection statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        int activeRaids = (int) guildStates.values().stream()
            .mapToLong(state -> state.isRaidActive() ? 1 : 0).sum();
        
        int totalSuspiciousActivities = suspiciousActivities.values().stream()
            .mapToInt(List::size).sum();
        
        stats.put("monitoredGuilds", guildStates.size());
        stats.put("activeRaids", activeRaids);
        stats.put("totalSuspiciousActivities", totalSuspiciousActivities);
        stats.put("recentJoins", recentJoins.values().stream().mapToInt(List::size).sum());
        
        return stats;
    }
    
    /**
     * Records a join event
     */
    private void recordJoinEvent(String guildId, JoinEvent event) {
        recentJoins.computeIfAbsent(guildId, k -> new ArrayList<>()).add(event);
        
        // Increment raid counter
        raidCounters.computeIfAbsent(guildId, k -> new AtomicInteger(0)).incrementAndGet();
    }
    
    /**
     * Analyzes join patterns for raid detection
     */
    private RaidPatternAnalysis analyzeJoinPatterns(String guildId) {
        List<JoinEvent> joins = recentJoins.getOrDefault(guildId, Collections.emptyList());
        
        if (joins.isEmpty()) {
            return new RaidPatternAnalysis(false, RaidType.NONE, Collections.emptyList(),
                Collections.emptyList(), 0.0);
        }
        
        List<String> patterns = new ArrayList<>();
        List<String> indicators = new ArrayList<>();
        double riskScore = 0.0;
        RaidType raidType = RaidType.NONE;
        
        Instant cutoff = Instant.now().minus(config.getJoinAnalysisWindow(), ChronoUnit.SECONDS);
        List<JoinEvent> recentJoins = joins.stream()
            .filter(join -> join.getJoinTime().isAfter(cutoff))
            .collect(Collectors.toList());
        
        // Mass join detection
        if (recentJoins.size() >= config.getMassJoinThreshold()) {
            patterns.add("MASS_JOIN");
            indicators.add(String.format("%d joins in %d seconds", 
                recentJoins.size(), config.getJoinAnalysisWindow()));
            riskScore = Math.max(riskScore, 0.8);
            raidType = RaidType.MASS_JOIN;
        }
        
        // Rapid join detection
        if (recentJoins.size() >= config.getRapidJoinThreshold()) {
            long timeSpan = ChronoUnit.SECONDS.between(
                recentJoins.get(0).getJoinTime(),
                recentJoins.get(recentJoins.size() - 1).getJoinTime()
            );
            
            if (timeSpan <= config.getRapidJoinWindow()) {
                patterns.add("RAPID_JOIN");
                indicators.add(String.format("%d joins in %d seconds", 
                    recentJoins.size(), timeSpan));
                riskScore = Math.max(riskScore, 0.9);
                raidType = RaidType.RAPID_JOIN;
            }
        }
        
        // Bot pattern detection
        long botLikeUsers = recentJoins.stream()
            .mapToLong(join -> isBotLikeUser(join) ? 1 : 0).sum();
        
        if (botLikeUsers >= config.getBotPatternThreshold()) {
            patterns.add("BOT_PATTERN");
            indicators.add(String.format("%d bot-like users detected", botLikeUsers));
            riskScore = Math.max(riskScore, 0.7);
            if (raidType == RaidType.NONE) {
                raidType = RaidType.BOT_RAID;
            }
        }
        
        return new RaidPatternAnalysis(!patterns.isEmpty(), raidType, patterns, indicators, riskScore);
    }
    
    /**
     * Analyzes user characteristics for suspicious behavior
     */
    private UserAnalysisResult analyzeUserCharacteristics(User user) {
        List<String> indicators = new ArrayList<>();
        double riskScore = 0.0;
        
        // Account age analysis
        long accountAge = ChronoUnit.DAYS.between(user.getTimeCreated().toInstant(), Instant.now());
        if (accountAge < config.getMinAccountAge()) {
            indicators.add(String.format("Account age: %d days (threshold: %d)", 
                accountAge, config.getMinAccountAge()));
            riskScore += 0.3;
        }
        
        // Username pattern analysis
        String username = user.getName();
        if (isSuspiciousUsername(username)) {
            indicators.add("Suspicious username pattern");
            riskScore += 0.4;
        }
        
        // Avatar analysis
        if (user.getAvatarUrl() == null) {
            indicators.add("No custom avatar");
            riskScore += 0.2;
        }
        
        return new UserAnalysisResult(!indicators.isEmpty(), indicators, riskScore);
    }
    
    /**
     * Analyzes coordination between users
     */
    private CoordinationAnalysis analyzeCoordination(String guildId, JoinEvent newJoin) {
        List<JoinEvent> recentJoins = this.recentJoins.getOrDefault(guildId, Collections.emptyList());
        
        List<String> indicators = new ArrayList<>();
        double riskScore = 0.0;
        
        // Check for similar usernames
        long similarNames = recentJoins.stream()
            .mapToLong(join -> areSimilarUsernames(join.getUsername(), newJoin.getUsername()) ? 1 : 0)
            .sum();
        
        if (similarNames >= config.getSimilarNameThreshold()) {
            indicators.add(String.format("%d users with similar names", similarNames));
            riskScore += 0.6;
        }
        
        // Check for coordinated timing
        Instant cutoff = Instant.now().minus(config.getCoordinationWindow(), ChronoUnit.SECONDS);
        long coordinatedJoins = recentJoins.stream()
            .filter(join -> join.getJoinTime().isAfter(cutoff))
            .count();
        
        if (coordinatedJoins >= config.getCoordinationThreshold()) {
            indicators.add(String.format("%d coordinated joins detected", coordinatedJoins));
            riskScore += 0.5;
        }
        
        return new CoordinationAnalysis(!indicators.isEmpty(), indicators, riskScore);
    }
    
    /**
     * Checks if a user appears to be bot-like
     */
    private boolean isBotLikeUser(JoinEvent join) {
        String username = join.getUsername();
        
        // Check for bot-like patterns
        return username.matches(".*[0-9]{4,}.*") || // Many numbers
               username.matches(".*[A-Z]{3,}[0-9]{3,}.*") || // Caps + numbers
               username.length() < 3 || // Very short
               username.length() > 20; // Very long
    }
    
    /**
     * Checks if a username is suspicious
     */
    private boolean isSuspiciousUsername(String username) {
        return username.matches(".*[0-9]{6,}.*") || // Many consecutive numbers
               username.matches("^[a-z]+[0-9]+$") || // Simple pattern
               username.contains("discord") ||
               username.contains("bot") ||
               username.matches(".*[A-Z]{5,}.*"); // Many caps
    }
    
    /**
     * Checks if two usernames are similar
     */
    private boolean areSimilarUsernames(String name1, String name2) {
        if (name1.equals(name2)) return true;
        
        // Remove numbers and compare
        String base1 = name1.replaceAll("[0-9]+", "");
        String base2 = name2.replaceAll("[0-9]+", "");
        
        return base1.equals(base2) && base1.length() > 3;
    }
    
    /**
     * Records suspicious activity
     */
    private void recordSuspiciousActivity(String guildId, SuspiciousActivity activity) {
        suspiciousActivities.computeIfAbsent(guildId, k -> new ArrayList<>()).add(activity);
    }
    
    /**
     * Generates recommendations based on raid type and risk score
     */
    private List<String> generateRecommendations(RaidType raidType, double riskScore) {
        List<String> recommendations = new ArrayList<>();
        
        if (riskScore >= 0.9) {
            recommendations.add("Immediate lockdown recommended");
            recommendations.add("Enable verification requirements");
        } else if (riskScore >= 0.7) {
            recommendations.add("Increase moderation vigilance");
            recommendations.add("Consider temporary restrictions");
        } else if (riskScore >= 0.5) {
            recommendations.add("Monitor closely");
            recommendations.add("Review recent joins");
        }
        
        switch (raidType) {
            case BOT_RAID:
                recommendations.add("Enable phone verification");
                recommendations.add("Increase account age requirements");
                break;
            case MASS_JOIN:
                recommendations.add("Temporarily disable invites");
                recommendations.add("Enable slowmode in channels");
                break;
            case COORDINATED:
                recommendations.add("Review user patterns");
                recommendations.add("Check for external coordination");
                break;
        }
        
        return recommendations;
    }
    
    /**
     * Starts background maintenance tasks
     */
    private void startBackgroundTasks() {
        // Clean up old join events
        scheduler.scheduleAtFixedRate(this::cleanupOldEvents, 5, 5, TimeUnit.MINUTES);
        
        // Update raid detection states
        scheduler.scheduleAtFixedRate(this::updateDetectionStates, 1, 1, TimeUnit.MINUTES);
    }
    
    private void cleanupOldEvents() {
        Instant cutoff = Instant.now().minus(config.getEventRetentionPeriod(), ChronoUnit.HOURS);
        
        recentJoins.values().forEach(events -> 
            events.removeIf(event -> event.getJoinTime().isBefore(cutoff)));
        
        suspiciousActivities.values().forEach(activities -> 
            activities.removeIf(activity -> activity.getTimestamp().isBefore(cutoff)));
    }
    
    private void updateDetectionStates() {
        guildStates.values().forEach(RaidDetectionState::updateState);
    }
    
    /**
     * Shuts down the system
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    // Inner classes and enums
    public enum RaidType {
        NONE, MASS_JOIN, RAPID_JOIN, BOT_RAID, COORDINATED, UNKNOWN
    }
    
    public static class JoinEvent {
        private final String userId;
        private final String username;
        private final Instant accountCreated;
        private final Instant joinTime;
        
        public JoinEvent(String userId, String username, Instant accountCreated, Instant joinTime) {
            this.userId = userId;
            this.username = username;
            this.accountCreated = accountCreated;
            this.joinTime = joinTime;
        }
        
        public String getUserId() { return userId; }
        public String getUsername() { return username; }
        public Instant getAccountCreated() { return accountCreated; }
        public Instant getJoinTime() { return joinTime; }
    }
    
    public static class SuspiciousActivity {
        private final String userId;
        private final List<String> patterns;
        private final double riskScore;
        private final Instant timestamp;
        
        public SuspiciousActivity(String userId, List<String> patterns, double riskScore, Instant timestamp) {
            this.userId = userId;
            this.patterns = new ArrayList<>(patterns);
            this.riskScore = riskScore;
            this.timestamp = timestamp;
        }
        
        public String getUserId() { return userId; }
        public List<String> getPatterns() { return new ArrayList<>(patterns); }
        public double getRiskScore() { return riskScore; }
        public Instant getTimestamp() { return timestamp; }
    }
    
    public static class RaidDetectionState {
        private boolean raidActive = false;
        private RaidType currentRaidType = RaidType.NONE;
        private double currentRiskScore = 0.0;
        private List<String> activePatterns = new ArrayList<>();
        private Instant lastUpdate = Instant.now();
        
        public void updateState(List<String> patterns, double riskScore) {
            this.activePatterns = new ArrayList<>(patterns);
            this.currentRiskScore = riskScore;
            this.lastUpdate = Instant.now();
            
            // Auto-deactivate if risk is low
            if (riskScore < 0.3) {
                this.raidActive = false;
                this.currentRaidType = RaidType.NONE;
            }
        }
        
        public void updateState() {
            // Decay risk score over time
            long minutesSinceUpdate = ChronoUnit.MINUTES.between(lastUpdate, Instant.now());
            if (minutesSinceUpdate > 0) {
                currentRiskScore *= Math.pow(0.95, minutesSinceUpdate);
                if (currentRiskScore < 0.1) {
                    raidActive = false;
                    currentRaidType = RaidType.NONE;
                    activePatterns.clear();
                }
            }
        }
        
        public void activateRaidProtection(RaidType raidType, String reason) {
            this.raidActive = true;
            this.currentRaidType = raidType;
            this.currentRiskScore = 1.0;
            this.lastUpdate = Instant.now();
        }
        
        public void deactivateRaidProtection() {
            this.raidActive = false;
            this.currentRaidType = RaidType.NONE;
            this.currentRiskScore = 0.0;
            this.activePatterns.clear();
            this.lastUpdate = Instant.now();
        }
        
        public boolean isRaidActive() { return raidActive; }
        public RaidType getCurrentRaidType() { return currentRaidType; }
        public double getCurrentRiskScore() { return currentRiskScore; }
        public List<String> getActivePatterns() { return new ArrayList<>(activePatterns); }
    }
    
    // Result classes
    public static class RaidAnalysisResult {
        private final String userId;
        private final boolean raidDetected;
        private final RaidType raidType;
        private final List<String> detectedPatterns;
        private final List<String> indicators;
        private final double riskScore;
        private final List<String> recommendations;
        private final long analysisTimeMs;
        
        private RaidAnalysisResult(String userId, boolean raidDetected, RaidType raidType,
                                  List<String> detectedPatterns, List<String> indicators,
                                  double riskScore, List<String> recommendations, long analysisTimeMs) {
            this.userId = userId;
            this.raidDetected = raidDetected;
            this.raidType = raidType;
            this.detectedPatterns = new ArrayList<>(detectedPatterns);
            this.indicators = new ArrayList<>(indicators);
            this.riskScore = riskScore;
            this.recommendations = new ArrayList<>(recommendations);
            this.analysisTimeMs = analysisTimeMs;
        }
        
        public static RaidAnalysisResult noRaid(String userId, long analysisTimeMs) {
            return new RaidAnalysisResult(userId, false, RaidType.NONE,
                Collections.emptyList(), Collections.emptyList(), 0.0,
                Collections.emptyList(), analysisTimeMs);
        }
        
        public static RaidAnalysisResult raidDetected(String userId, RaidType raidType,
                                                     List<String> patterns, List<String> indicators,
                                                     double riskScore, List<String> recommendations,
                                                     long analysisTimeMs) {
            return new RaidAnalysisResult(userId, true, raidType, patterns, indicators,
                riskScore, recommendations, analysisTimeMs);
        }
        
        // Getters
        public String getUserId() { return userId; }
        public boolean isRaidDetected() { return raidDetected; }
        public RaidType getRaidType() { return raidType; }
        public List<String> getDetectedPatterns() { return new ArrayList<>(detectedPatterns); }
        public List<String> getIndicators() { return new ArrayList<>(indicators); }
        public double getRiskScore() { return riskScore; }
        public List<String> getRecommendations() { return new ArrayList<>(recommendations); }
        public long getAnalysisTimeMs() { return analysisTimeMs; }
    }
    
    public static class RaidStatus {
        private final boolean active;
        private final RaidType raidType;
        private final double riskScore;
        private final List<String> activePatterns;
        
        public RaidStatus(boolean active, RaidType raidType, double riskScore, List<String> activePatterns) {
            this.active = active;
            this.raidType = raidType;
            this.riskScore = riskScore;
            this.activePatterns = new ArrayList<>(activePatterns);
        }
        
        public boolean isActive() { return active; }
        public RaidType getRaidType() { return raidType; }
        public double getRiskScore() { return riskScore; }
        public List<String> getActivePatterns() { return new ArrayList<>(activePatterns); }
    }
    
    // Analysis result classes
    private static class RaidPatternAnalysis {
        private final boolean raidDetected;
        private final RaidType raidType;
        private final List<String> patterns;
        private final List<String> indicators;
        private final double riskScore;
        
        public RaidPatternAnalysis(boolean raidDetected, RaidType raidType, List<String> patterns,
                                  List<String> indicators, double riskScore) {
            this.raidDetected = raidDetected;
            this.raidType = raidType;
            this.patterns = new ArrayList<>(patterns);
            this.indicators = new ArrayList<>(indicators);
            this.riskScore = riskScore;
        }
        
        public boolean isRaidDetected() { return raidDetected; }
        public RaidType getRaidType() { return raidType; }
        public List<String> getPatterns() { return new ArrayList<>(patterns); }
        public List<String> getIndicators() { return new ArrayList<>(indicators); }
        public double getRiskScore() { return riskScore; }
    }
    
    private static class UserAnalysisResult {
        private final boolean suspicious;
        private final List<String> indicators;
        private final double riskScore;
        
        public UserAnalysisResult(boolean suspicious, List<String> indicators, double riskScore) {
            this.suspicious = suspicious;
            this.indicators = new ArrayList<>(indicators);
            this.riskScore = riskScore;
        }
        
        public boolean isSuspicious() { return suspicious; }
        public List<String> getIndicators() { return new ArrayList<>(indicators); }
        public double getRiskScore() { return riskScore; }
    }
    
    private static class CoordinationAnalysis {
        private final boolean coordinated;
        private final List<String> indicators;
        private final double riskScore;
        
        public CoordinationAnalysis(boolean coordinated, List<String> indicators, double riskScore) {
            this.coordinated = coordinated;
            this.indicators = new ArrayList<>(indicators);
            this.riskScore = riskScore;
        }
        
        public boolean isCoordinated() { return coordinated; }
        public List<String> getIndicators() { return new ArrayList<>(indicators); }
        public double getRiskScore() { return riskScore; }
    }
    
    // Configuration class
    public static class RaidProtectionConfig {
        private int massJoinThreshold = 10;
        private int rapidJoinThreshold = 5;
        private long rapidJoinWindow = 30; // seconds
        private long joinAnalysisWindow = 300; // seconds
        private int botPatternThreshold = 3;
        private int minAccountAge = 7; // days
        private int similarNameThreshold = 3;
        private long coordinationWindow = 60; // seconds
        private int coordinationThreshold = 5;
        private long eventRetentionPeriod = 24; // hours
        
        // Getters and setters
        public int getMassJoinThreshold() { return massJoinThreshold; }
        public void setMassJoinThreshold(int threshold) { this.massJoinThreshold = threshold; }
        
        public int getRapidJoinThreshold() { return rapidJoinThreshold; }
        public void setRapidJoinThreshold(int threshold) { this.rapidJoinThreshold = threshold; }
        
        public long getRapidJoinWindow() { return rapidJoinWindow; }
        public void setRapidJoinWindow(long window) { this.rapidJoinWindow = window; }
        
        public long getJoinAnalysisWindow() { return joinAnalysisWindow; }
        public void setJoinAnalysisWindow(long window) { this.joinAnalysisWindow = window; }
        
        public int getBotPatternThreshold() { return botPatternThreshold; }
        public void setBotPatternThreshold(int threshold) { this.botPatternThreshold = threshold; }
        
        public int getMinAccountAge() { return minAccountAge; }
        public void setMinAccountAge(int age) { this.minAccountAge = age; }
        
        public int getSimilarNameThreshold() { return similarNameThreshold; }
        public void setSimilarNameThreshold(int threshold) { this.similarNameThreshold = threshold; }
        
        public long getCoordinationWindow() { return coordinationWindow; }
        public void setCoordinationWindow(long window) { this.coordinationWindow = window; }
        
        public int getCoordinationThreshold() { return coordinationThreshold; }
        public void setCoordinationThreshold(int threshold) { this.coordinationThreshold = threshold; }
        
        public long getEventRetentionPeriod() { return eventRetentionPeriod; }
        public void setEventRetentionPeriod(long period) { this.eventRetentionPeriod = period; }
    }
}