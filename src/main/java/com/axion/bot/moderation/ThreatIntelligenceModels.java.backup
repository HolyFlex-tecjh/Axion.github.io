package com.axion.bot.moderation;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Supporting models and classes for the Real-time Threat Intelligence System
 */

// Threat Analysis Result
class ThreatAnalysisResult {
    private final ThreatIntelligenceResult result;
    private final long processingTimeMs;
    
    public ThreatAnalysisResult(ThreatIntelligenceResult result, long processingTimeMs) {
        this.result = result;
        this.processingTimeMs = processingTimeMs;
    }
    
    public ThreatIntelligenceResult getResult() { return result; }
    public long getProcessingTimeMs() { return processingTimeMs; }
}

// Threat Intelligence Result
class ThreatIntelligenceResult {
    private final ThreatLevel threatLevel;
    private final double confidence;
    private final Set<ThreatType> threatTypes;
    private final List<String> indicators;
    private final List<String> recommendations;
    private final Map<String, Object> metadata;
    private final List<ThreatDetection> detections;
    private final boolean isError;
    private final String errorMessage;
    
    public ThreatIntelligenceResult(ThreatLevel threatLevel, double confidence, Set<ThreatType> threatTypes,
                                   List<String> indicators, List<String> recommendations,
                                   Map<String, Object> metadata, List<ThreatDetection> detections) {
        this.threatLevel = threatLevel;
        this.confidence = confidence;
        this.threatTypes = new HashSet<>(threatTypes);
        this.indicators = new ArrayList<>(indicators);
        this.recommendations = new ArrayList<>(recommendations);
        this.metadata = new HashMap<>(metadata);
        this.detections = new ArrayList<>(detections);
        this.isError = false;
        this.errorMessage = null;
    }
    
    private ThreatIntelligenceResult(String errorMessage) {
        this.threatLevel = ThreatLevel.NONE;
        this.confidence = 0.0;
        this.threatTypes = new HashSet<>();
        this.indicators = new ArrayList<>();
        this.recommendations = new ArrayList<>();
        this.metadata = new HashMap<>();
        this.detections = new ArrayList<>();
        this.isError = true;
        this.errorMessage = errorMessage;
    }
    
    public static ThreatIntelligenceResult clean() {
        return new ThreatIntelligenceResult(
            ThreatLevel.NONE, 0.0, new HashSet<>(), new ArrayList<>(),
            new ArrayList<>(), new HashMap<>(), new ArrayList<>()
        );
    }
    
    public static ThreatIntelligenceResult error(String message) {
        return new ThreatIntelligenceResult(message);
    }
    
    // Getters
    public ThreatLevel getThreatLevel() { return threatLevel; }
    public double getConfidence() { return confidence; }
    public Set<ThreatType> getThreatTypes() { return new HashSet<>(threatTypes); }
    public List<String> getIndicators() { return new ArrayList<>(indicators); }
    public List<String> getRecommendations() { return new ArrayList<>(recommendations); }
    public Map<String, Object> getMetadata() { return new HashMap<>(metadata); }
    public List<ThreatDetection> getDetections() { return new ArrayList<>(detections); }
    public boolean isError() { return isError; }
    public String getErrorMessage() { return errorMessage; }
}

// Threat Detection
class ThreatDetection {
    private final ThreatType threatType;
    private final ThreatLevel threatLevel;
    private final double confidence;
    private final List<String> indicators;
    private final String description;
    private final Map<String, Object> details;
    private final Instant detectedAt;
    
    public ThreatDetection(ThreatType threatType, ThreatLevel threatLevel, double confidence,
                          List<String> indicators, String description, Map<String, Object> details) {
        this.threatType = threatType;
        this.threatLevel = threatLevel;
        this.confidence = confidence;
        this.indicators = new ArrayList<>(indicators);
        this.description = description;
        this.details = new HashMap<>(details);
        this.detectedAt = Instant.now();
    }
    
    public boolean isDetected() {
        return threatLevel != ThreatLevel.NONE && confidence > 0.0;
    }
    
    // Getters
    public ThreatType getThreatType() { return threatType; }
    public ThreatLevel getThreatLevel() { return threatLevel; }
    public double getConfidence() { return confidence; }
    public List<String> getIndicators() { return new ArrayList<>(indicators); }
    public String getDescription() { return description; }
    public Map<String, Object> getDetails() { return new HashMap<>(details); }
    public Instant getDetectedAt() { return detectedAt; }
}

// Domain Reputation
class DomainReputation {
    private final String domain;
    private final ReputationLevel reputationLevel;
    private final double riskScore;
    private final List<String> riskFactors;
    private final Instant lastChecked;
    
    public DomainReputation(String domain, ReputationLevel reputationLevel, double riskScore,
                           List<String> riskFactors, Instant lastChecked) {
        this.domain = domain;
        this.reputationLevel = reputationLevel;
        this.riskScore = riskScore;
        this.riskFactors = new ArrayList<>(riskFactors);
        this.lastChecked = lastChecked;
    }
    
    // Getters
    public String getDomain() { return domain; }
    public ReputationLevel getReputationLevel() { return reputationLevel; }
    public double getRiskScore() { return riskScore; }
    public List<String> getRiskFactors() { return new ArrayList<>(riskFactors); }
    public Instant getLastChecked() { return lastChecked; }
}

// IP Reputation
class IPReputation {
    private final String ipAddress;
    private final ReputationLevel reputationLevel;
    private final double riskScore;
    private final List<String> riskFactors;
    private final String geolocation;
    private final Instant lastChecked;
    
    public IPReputation(String ipAddress, ReputationLevel reputationLevel, double riskScore,
                       List<String> riskFactors, String geolocation, Instant lastChecked) {
        this.ipAddress = ipAddress;
        this.reputationLevel = reputationLevel;
        this.riskScore = riskScore;
        this.riskFactors = new ArrayList<>(riskFactors);
        this.geolocation = geolocation;
        this.lastChecked = lastChecked;
    }
    
    // Getters
    public String getIpAddress() { return ipAddress; }
    public ReputationLevel getReputationLevel() { return reputationLevel; }
    public double getRiskScore() { return riskScore; }
    public List<String> getRiskFactors() { return new ArrayList<>(riskFactors); }
    public String getGeolocation() { return geolocation; }
    public Instant getLastChecked() { return lastChecked; }
}

// User Threat Profile
class UserThreatProfile {
    private final String userId;
    private final List<ThreatIntelligenceResult> threatHistory;
    private final Map<ThreatType, Integer> threatTypeCounts;
    private double overallRiskScore;
    private ThreatLevel maxThreatLevel;
    private Instant lastThreatDetection;
    private Instant profileCreated;
    private Instant lastUpdated;
    
    public UserThreatProfile(String userId) {
        this.userId = userId;
        this.threatHistory = new ArrayList<>();
        this.threatTypeCounts = new ConcurrentHashMap<>();
        this.overallRiskScore = 0.0;
        this.maxThreatLevel = ThreatLevel.NONE;
        this.profileCreated = Instant.now();
        this.lastUpdated = Instant.now();
    }
    
    public void addThreatResult(ThreatIntelligenceResult result) {
        if (result.getThreatLevel() == ThreatLevel.NONE) {
            return;
        }
        
        threatHistory.add(result);
        
        // Update threat type counts
        for (ThreatType type : result.getThreatTypes()) {
            threatTypeCounts.merge(type, 1, Integer::sum);
        }
        
        // Update max threat level
        if (result.getThreatLevel().getLevel() > maxThreatLevel.getLevel()) {
            maxThreatLevel = result.getThreatLevel();
        }
        
        // Update risk score
        updateRiskScore();
        
        lastThreatDetection = Instant.now();
        lastUpdated = Instant.now();
    }
    
    private void updateRiskScore() {
        if (threatHistory.isEmpty()) {
            overallRiskScore = 0.0;
            return;
        }
        
        // Calculate weighted risk score based on recent threats
        double totalScore = 0.0;
        double totalWeight = 0.0;
        
        Instant now = Instant.now();
        
        for (ThreatIntelligenceResult threat : threatHistory) {
            // Weight recent threats more heavily
            long hoursAgo = java.time.Duration.between(
                (Instant) threat.getMetadata().get("timestamp"), now
            ).toHours();
            
            double timeWeight = Math.max(0.1, 1.0 - (hoursAgo / (24.0 * 7.0))); // Decay over a week
            double threatScore = threat.getThreatLevel().getLevel() * threat.getConfidence();
            
            totalScore += threatScore * timeWeight;
            totalWeight += timeWeight;
        }
        
        overallRiskScore = totalWeight > 0 ? totalScore / totalWeight : 0.0;
    }
    
    public boolean isHighRisk() {
        return overallRiskScore > 2.5 || maxThreatLevel.getLevel() >= ThreatLevel.HIGH.getLevel();
    }
    
    public int getThreatCount(ThreatType type) {
        return threatTypeCounts.getOrDefault(type, 0);
    }
    
    public List<ThreatIntelligenceResult> getRecentThreats(int hours) {
        Instant cutoff = Instant.now().minusSeconds(hours * 3600L);
        
        return threatHistory.stream()
            .filter(threat -> {
                Instant timestamp = (Instant) threat.getMetadata().get("timestamp");
                return timestamp != null && timestamp.isAfter(cutoff);
            })
            .collect(Collectors.toList());
    }
    
    // Getters
    public String getUserId() { return userId; }
    public List<ThreatIntelligenceResult> getThreatHistory() { return new ArrayList<>(threatHistory); }
    public Map<ThreatType, Integer> getThreatTypeCounts() { return new HashMap<>(threatTypeCounts); }
    public double getOverallRiskScore() { return overallRiskScore; }
    public ThreatLevel getMaxThreatLevel() { return maxThreatLevel; }
    public Instant getLastThreatDetection() { return lastThreatDetection; }
    public Instant getProfileCreated() { return profileCreated; }
    public Instant getLastUpdated() { return lastUpdated; }
}

// Threat Report
class ThreatReport {
    private final ThreatType threatType;
    private final String indicator;
    private final String description;
    private final double confidence;
    private final String source;
    private final Map<String, Object> metadata;
    private final Instant reportedAt;
    
    public ThreatReport(ThreatType threatType, String indicator, String description,
                       double confidence, String source, Map<String, Object> metadata) {
        this.threatType = threatType;
        this.indicator = indicator;
        this.description = description;
        this.confidence = confidence;
        this.source = source;
        this.metadata = new HashMap<>(metadata);
        this.reportedAt = Instant.now();
    }
    
    // Getters
    public ThreatType getThreatType() { return threatType; }
    public String getIndicator() { return indicator; }
    public String getDescription() { return description; }
    public double getConfidence() { return confidence; }
    public String getSource() { return source; }
    public Map<String, Object> getMetadata() { return new HashMap<>(metadata); }
    public Instant getReportedAt() { return reportedAt; }
}

// Threat Indicator
class ThreatIndicator {
    private final String indicator;
    private final ThreatType type;
    private final double confidence;
    private final String source;
    private final Instant firstSeen;
    private final Instant lastSeen;
    private final Map<String, Object> attributes;
    
    public ThreatIndicator(String indicator, ThreatType type, double confidence, String source,
                          Instant firstSeen, Instant lastSeen, Map<String, Object> attributes) {
        this.indicator = indicator;
        this.type = type;
        this.confidence = confidence;
        this.source = source;
        this.firstSeen = firstSeen;
        this.lastSeen = lastSeen;
        this.attributes = new HashMap<>(attributes);
    }
    
    // Getters
    public String getIndicator() { return indicator; }
    public ThreatType getType() { return type; }
    public double getConfidence() { return confidence; }
    public String getSource() { return source; }
    public Instant getFirstSeen() { return firstSeen; }
    public Instant getLastSeen() { return lastSeen; }
    public Map<String, Object> getAttributes() { return new HashMap<>(attributes); }
}

// Threat Intelligence Statistics
class ThreatIntelligenceStats {
    private final long totalThreatsDetected;
    private final long malwareDetections;
    private final long phishingDetections;
    private final long spamDetections;
    private final long coordinatedAttacks;
    private final long cachedThreats;
    private final long cachedDomains;
    private final long cachedIPs;
    private final long cachedUsers;
    
    public ThreatIntelligenceStats(long totalThreatsDetected, long malwareDetections,
                                  long phishingDetections, long spamDetections,
                                  long coordinatedAttacks, long cachedThreats,
                                  long cachedDomains, long cachedIPs, long cachedUsers) {
        this.totalThreatsDetected = totalThreatsDetected;
        this.malwareDetections = malwareDetections;
        this.phishingDetections = phishingDetections;
        this.spamDetections = spamDetections;
        this.coordinatedAttacks = coordinatedAttacks;
        this.cachedThreats = cachedThreats;
        this.cachedDomains = cachedDomains;
        this.cachedIPs = cachedIPs;
        this.cachedUsers = cachedUsers;
    }
    
    // Getters
    public long getTotalThreatsDetected() { return totalThreatsDetected; }
    public long getMalwareDetections() { return malwareDetections; }
    public long getPhishingDetections() { return phishingDetections; }
    public long getSpamDetections() { return spamDetections; }
    public long getCoordinatedAttacks() { return coordinatedAttacks; }
    public long getCachedThreats() { return cachedThreats; }
    public long getCachedDomains() { return cachedDomains; }
    public long getCachedIPs() { return cachedIPs; }
    public long getCachedUsers() { return cachedUsers; }
}

// Detection Engines
class MalwareDetectionEngine {
    private final Set<String> knownMalwareHashes;
    private final List<Pattern> malwarePatterns;
    
    public MalwareDetectionEngine(MalwareDetectionConfig config) {
        this.knownMalwareHashes = new HashSet<>();
        this.malwarePatterns = initializeMalwarePatterns();
        
        // Load known malware hashes
        loadKnownMalwareHashes();
    }
    
    public ThreatDetection analyzeContent(String content, ContentType contentType) {
        Map<String, Object> details = new HashMap<>();
        List<String> indicators = new ArrayList<>();
        double confidence = 0.0;
        ThreatLevel level = ThreatLevel.NONE;
        
        // Check for malware patterns in text
        if (contentType == ContentType.TEXT || contentType == ContentType.URL) {
            for (Pattern pattern : malwarePatterns) {
                if (pattern.matcher(content.toLowerCase()).find()) {
                    indicators.add("Malware pattern detected: " + pattern.pattern());
                    confidence = Math.max(confidence, 0.7);
                    level = ThreatLevel.HIGH;
                }
            }
        }
        
        // Check for suspicious file extensions in URLs
        if (contentType == ContentType.URL) {
            if (content.matches(".*\\.(exe|scr|bat|cmd|com|pif|vbs|js|jar|zip|rar)($|\\?|#)")) {
                indicators.add("Suspicious file extension in URL");
                confidence = Math.max(confidence, 0.6);
                level = ThreatLevel.MEDIUM;
            }
        }
        
        // Check for URL shorteners (often used to hide malicious links)
        if (contentType == ContentType.URL) {
            String[] shorteners = {"bit.ly", "tinyurl", "t.co", "goo.gl", "ow.ly", "short.link"};
            for (String shortener : shorteners) {
                if (content.contains(shortener)) {
                    indicators.add("URL shortener detected: " + shortener);
                    confidence = Math.max(confidence, 0.4);
                    level = ThreatLevel.LOW;
                }
            }
        }
        
        details.put("contentType", contentType);
        details.put("contentLength", content.length());
        details.put("patternsChecked", malwarePatterns.size());
        
        String description = confidence > 0 ? 
            "Malware indicators detected with " + String.format("%.1f%%", confidence * 100) + " confidence" :
            "No malware indicators detected";
        
        return new ThreatDetection(
            ThreatType.MALWARE,
            level,
            confidence,
            indicators,
            description,
            details
        );
    }
    
    private List<Pattern> initializeMalwarePatterns() {
        List<Pattern> patterns = new ArrayList<>();
        
        // Common malware-related terms
        patterns.add(Pattern.compile("(download|install)\\s+(crack|keygen|patch|activator)"));
        patterns.add(Pattern.compile("free\\s+(hack|cheat|bot|generator)"));
        patterns.add(Pattern.compile("(virus|trojan|malware|spyware|adware)\\s+(download|free)"));
        patterns.add(Pattern.compile("click\\s+here\\s+to\\s+(download|install|activate)"));
        patterns.add(Pattern.compile("(bypass|remove)\\s+(antivirus|security|protection)"));
        
        return patterns;
    }
    
    private void loadKnownMalwareHashes() {
        // In a real implementation, this would load from threat intelligence feeds
        // For now, we'll add some example hashes
        knownMalwareHashes.add("d41d8cd98f00b204e9800998ecf8427e"); // Example MD5
        knownMalwareHashes.add("da39a3ee5e6b4b0d3255bfef95601890afd80709"); // Example SHA1
    }
}

class PhishingDetectionEngine {
    private final Set<String> knownPhishingDomains;
    private final List<Pattern> phishingPatterns;
    private final Set<String> legitimateDomains;
    
    public PhishingDetectionEngine(PhishingDetectionConfig config) {
        this.knownPhishingDomains = new HashSet<>();
        this.phishingPatterns = initializePhishingPatterns();
        this.legitimateDomains = initializeLegitimateDomainsSet();
        
        loadKnownPhishingDomains();
    }
    
    public ThreatDetection analyzeContent(String content, ContentType contentType) {
        Map<String, Object> details = new HashMap<>();
        List<String> indicators = new ArrayList<>();
        double confidence = 0.0;
        ThreatLevel level = ThreatLevel.NONE;
        
        if (contentType == ContentType.URL || contentType == ContentType.TEXT) {
            // Extract URLs from content
            List<String> urls = extractUrls(content);
            
            for (String url : urls) {
                // Check against known phishing domains
                String domain = extractDomain(url);
                if (knownPhishingDomains.contains(domain)) {
                    indicators.add("Known phishing domain: " + domain);
                    confidence = Math.max(confidence, 0.9);
                    level = ThreatLevel.CRITICAL;
                }
                
                // Check for domain similarity to legitimate sites
                String suspiciousDomain = checkDomainSimilarity(domain);
                if (suspiciousDomain != null) {
                    indicators.add("Domain similar to legitimate site: " + suspiciousDomain);
                    confidence = Math.max(confidence, 0.7);
                    level = ThreatLevel.HIGH;
                }
                
                // Check for suspicious URL structure
                if (hasSuspiciousUrlStructure(url)) {
                    indicators.add("Suspicious URL structure detected");
                    confidence = Math.max(confidence, 0.5);
                    level = ThreatLevel.MEDIUM;
                }
            }
            
            // Check for phishing patterns in text
            for (Pattern pattern : phishingPatterns) {
                if (pattern.matcher(content.toLowerCase()).find()) {
                    indicators.add("Phishing pattern detected: " + pattern.pattern());
                    confidence = Math.max(confidence, 0.6);
                    level = ThreatLevel.MEDIUM;
                }
            }
        }
        
        details.put("contentType", contentType);
        details.put("urlsFound", extractUrls(content).size());
        details.put("patternsChecked", phishingPatterns.size());
        
        String description = confidence > 0 ? 
            "Phishing indicators detected with " + String.format("%.1f%%", confidence * 100) + " confidence" :
            "No phishing indicators detected";
        
        return new ThreatDetection(
            ThreatType.PHISHING,
            level,
            confidence,
            indicators,
            description,
            details
        );
    }
    
    private List<Pattern> initializePhishingPatterns() {
        List<Pattern> patterns = new ArrayList<>();
        
        // Common phishing phrases
        patterns.add(Pattern.compile("(urgent|immediate)\\s+(action|response)\\s+required"));
        patterns.add(Pattern.compile("verify\\s+your\\s+(account|identity|information)"));
        patterns.add(Pattern.compile("(click|tap)\\s+here\\s+to\\s+(verify|confirm|update)"));
        patterns.add(Pattern.compile("your\\s+(account|payment)\\s+(has\\s+been\\s+)?(suspended|locked|compromised)"));
        patterns.add(Pattern.compile("(limited\\s+time|expires?\\s+(today|soon|in))"));
        patterns.add(Pattern.compile("(congratulations|winner|selected).*prize"));
        patterns.add(Pattern.compile("(free|claim)\\s+(money|gift|reward|prize)"));
        
        return patterns;
    }
    
    private Set<String> initializeLegitimateDomainsSet() {
        Set<String> domains = new HashSet<>();
        domains.add("discord.com");
        domains.add("google.com");
        domains.add("microsoft.com");
        domains.add("apple.com");
        domains.add("amazon.com");
        domains.add("paypal.com");
        domains.add("github.com");
        domains.add("stackoverflow.com");
        return domains;
    }
    
    private void loadKnownPhishingDomains() {
        // In a real implementation, this would load from threat intelligence feeds
        knownPhishingDomains.add("discrod.com"); // Typosquatting example
        knownPhishingDomains.add("paypaI.com"); // Using capital i instead of l
        knownPhishingDomains.add("microsft.com"); // Missing letter
    }
    
    private List<String> extractUrls(String content) {
        List<String> urls = new ArrayList<>();
        Pattern urlPattern = Pattern.compile(
            "https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+",
            Pattern.CASE_INSENSITIVE
        );
        
        java.util.regex.Matcher matcher = urlPattern.matcher(content);
        while (matcher.find()) {
            urls.add(matcher.group());
        }
        
        return urls;
    }
    
    private String extractDomain(String url) {
        try {
            // Simple domain extraction
            String domain = url.replaceFirst("^https?://", "");
            domain = domain.split("/")[0];
            domain = domain.split(":")[0];
            return domain.toLowerCase();
        } catch (Exception e) {
            return "";
        }
    }
    
    private String checkDomainSimilarity(String domain) {
        for (String legitimate : legitimateDomains) {
            if (calculateLevenshteinDistance(domain, legitimate) <= 2 && !domain.equals(legitimate)) {
                return legitimate;
            }
        }
        return null;
    }
    
    private boolean hasSuspiciousUrlStructure(String url) {
        // Check for suspicious URL characteristics
        return url.contains("bit.ly") ||
               url.contains("tinyurl") ||
               url.matches(".*\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}.*") || // IP addresses
               url.length() > 100 || // Very long URLs
               url.split("/").length > 10; // Too many path segments
    }
    
    private int calculateLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(
                        dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1),
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1)
                    );
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
}

// SpamDetectionEngine class removed - using separate SpamDetectionEngine.java file

class CoordinatedAttackDetector {
    private final CoordinatedAttackConfig config;
    private final Map<String, List<UserActivity>> guildActivities;
    
    public CoordinatedAttackDetector(CoordinatedAttackConfig config) {
        this.config = config;
        this.guildActivities = new ConcurrentHashMap<>();
    }
    
    public ThreatDetection analyzeUserActivity(String userId, String guildId, String content) {
        Map<String, Object> details = new HashMap<>();
        List<String> indicators = new ArrayList<>();
        double confidence = 0.0;
        ThreatLevel level = ThreatLevel.NONE;
        
        // Record user activity
        UserActivity activity = new UserActivity(userId, content, Instant.now());
        guildActivities.computeIfAbsent(guildId, k -> new ArrayList<>()).add(activity);
        
        // Clean old activities
        cleanOldActivities(guildId);
        
        // Analyze for coordinated patterns
        List<UserActivity> recentActivities = getRecentActivities(guildId);
        
        // Check for similar content from multiple users
        long similarContentCount = recentActivities.stream()
            .filter(a -> !a.getUserId().equals(userId))
            .filter(a -> calculateContentSimilarity(content, a.getContent()) > 0.8)
            .count();
        
        if (similarContentCount >= config.getMinimumParticipants()) {
            indicators.add("Similar content from " + similarContentCount + " users detected");
            confidence = Math.max(confidence, 0.8);
            level = ThreatLevel.HIGH;
        }
        
        // Check for rapid posting from multiple users
        long rapidPosters = recentActivities.stream()
            .collect(Collectors.groupingBy(UserActivity::getUserId, Collectors.counting()))
            .values().stream()
            .filter(count -> count > 3) // More than 3 messages in time window
            .count();
        
        if (rapidPosters >= config.getMinimumParticipants()) {
            indicators.add("Rapid posting from " + rapidPosters + " users detected");
            confidence = Math.max(confidence, 0.7);
            level = ThreatLevel.MEDIUM;
        }
        
        details.put("guildId", guildId);
        details.put("recentActivities", recentActivities.size());
        details.put("similarContentCount", similarContentCount);
        details.put("rapidPosters", rapidPosters);
        
        String description = confidence > 0 ? 
            "Coordinated attack indicators detected with " + String.format("%.1f%%", confidence * 100) + " confidence" :
            "No coordinated attack indicators detected";
        
        return new ThreatDetection(
            ThreatType.COORDINATED_ATTACK,
            level,
            confidence,
            indicators,
            description,
            details
        );
    }
    
    private void cleanOldActivities(String guildId) {
        List<UserActivity> activities = guildActivities.get(guildId);
        if (activities != null) {
            Instant cutoff = Instant.now().minusSeconds(config.getTimeWindowMinutes() * 60L);
            activities.removeIf(activity -> activity.getTimestamp().isBefore(cutoff));
        }
    }
    
    private List<UserActivity> getRecentActivities(String guildId) {
        List<UserActivity> activities = guildActivities.get(guildId);
        if (activities == null) {
            return new ArrayList<>();
        }
        
        Instant cutoff = Instant.now().minusSeconds(config.getTimeWindowMinutes() * 60L);
        return activities.stream()
            .filter(activity -> activity.getTimestamp().isAfter(cutoff))
            .collect(Collectors.toList());
    }
    
    private double calculateContentSimilarity(String content1, String content2) {
        // Simple similarity calculation based on common words
        Set<String> words1 = new HashSet<>(Arrays.asList(content1.toLowerCase().split("\\s+")));
        Set<String> words2 = new HashSet<>(Arrays.asList(content2.toLowerCase().split("\\s+")));
        
        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);
        
        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);
        
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }
}

// UserActivity class removed - using separate UserActivity.java file

class ThreatFeedManager {
    private final Map<String, Set<String>> maliciousDomains;
    private final Map<String, Set<String>> maliciousIPs;
    private final Map<String, Set<String>> suspiciousIPs;
    private final List<ThreatIndicator> threatIndicators;
    
    public ThreatFeedManager(ThreatFeedConfig config) {
        this.maliciousDomains = new ConcurrentHashMap<>();
        this.maliciousIPs = new ConcurrentHashMap<>();
        this.suspiciousIPs = new ConcurrentHashMap<>();
        this.threatIndicators = new ArrayList<>();
        
        initializeFeeds();
    }
    
    private void initializeFeeds() {
        // Initialize with some example data
        maliciousDomains.put("internal", new HashSet<>(Arrays.asList(
            "malware-site.com", "phishing-example.net", "scam-domain.org"
        )));
        
        maliciousIPs.put("internal", new HashSet<>(Arrays.asList(
            "192.168.1.100", "10.0.0.50", "172.16.0.25"
        )));
        
        suspiciousIPs.put("internal", new HashSet<>(Arrays.asList(
            "192.168.1.200", "10.0.0.100"
        )));
    }
    
    public void updateAllFeeds() {
        // In a real implementation, this would fetch from external threat feeds
        // For now, we'll simulate the update
    }
    
    public void updateThreatFeeds(ThreatReport report) {
        // Add new threat indicators from reports
        ThreatIndicator indicator = new ThreatIndicator(
            report.getIndicator(),
            report.getThreatType(),
            report.getConfidence(),
            report.getSource(),
            report.getReportedAt(),
            report.getReportedAt(),
            report.getMetadata()
        );
        
        threatIndicators.add(indicator);
        
        // Update appropriate feed based on threat type
        if (report.getThreatType() == ThreatType.PHISHING || report.getThreatType() == ThreatType.MALWARE) {
            maliciousDomains.computeIfAbsent(report.getSource(), k -> new HashSet<>())
                .add(report.getIndicator());
        }
    }
    
    public boolean isMaliciousDomain(String domain) {
        return maliciousDomains.values().stream()
            .anyMatch(domains -> domains.contains(domain));
    }
    
    public boolean isMaliciousIP(String ip) {
        return maliciousIPs.values().stream()
            .anyMatch(ips -> ips.contains(ip));
    }
    
    public boolean isSuspiciousIP(String ip) {
        return suspiciousIPs.values().stream()
            .anyMatch(ips -> ips.contains(ip));
    }
    
    public List<ThreatIndicator> getActiveThreatIndicators(Instant since) {
        return threatIndicators.stream()
            .filter(indicator -> indicator.getLastSeen().isAfter(since))
            .collect(Collectors.toList());
    }
}