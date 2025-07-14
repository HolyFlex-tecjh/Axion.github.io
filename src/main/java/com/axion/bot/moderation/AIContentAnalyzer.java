package com.axion.bot.moderation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.time.Instant;
import java.time.Duration;

/**
 * AI-Enhanced Content Analyzer for advanced toxicity and threat detection
 * Implements machine learning-based content analysis with context awareness
 */
public class AIContentAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(AIContentAnalyzer.class);
    
    // AI Model components (simplified implementation)
    private final ToxicityModel toxicityModel;
    private final SentimentAnalyzer sentimentAnalyzer;
    private final ContextAnalyzer contextAnalyzer;
    private final LanguageDetector languageDetector;
    
    // Pattern-based fallback systems
    private final Map<String, List<Pattern>> languageSpecificPatterns;
    private final Map<String, Double> patternWeights;
    
    // Performance tracking
    private final Map<String, Long> analysisMetrics = new ConcurrentHashMap<>();
    private final Map<String, Integer> detectionCounts = new ConcurrentHashMap<>();
    
    // Configuration
    private double toxicityThreshold = 0.7;
    private double sentimentThreshold = -0.6;
    private boolean contextAwareAnalysis = true;
    private boolean multiLanguageSupport = true;
    
    public AIContentAnalyzer() {
        this.toxicityModel = new ToxicityModel();
        this.sentimentAnalyzer = new SentimentAnalyzer();
        this.contextAnalyzer = new ContextAnalyzer();
        this.languageDetector = new LanguageDetector();
        this.languageSpecificPatterns = initializeLanguagePatterns();
        this.patternWeights = initializePatternWeights();
        
        logger.info("AIContentAnalyzer initialized with ML models");
    }
    
    /**
     * Main content analysis method with multi-layer detection
     */
    public ContentAnalysisResult analyzeContent(String content, ConversationContext context) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Preprocess content
            String normalizedContent = preprocessContent(content);
            
            // Detect language for language-specific analysis
            String detectedLanguage = languageDetector.detect(normalizedContent);
            
            // Multi-layer analysis
            List<AnalysisLayer> analysisResults = new ArrayList<>();
            
            // 1. Toxicity Analysis (Primary AI Model)
            ToxicityScore toxicityScore = toxicityModel.analyze(normalizedContent, detectedLanguage);
            analysisResults.add(new AnalysisLayer("toxicity", toxicityScore.getScore(), toxicityScore.getCategories()));
            
            // 2. Sentiment Analysis
            SentimentScore sentimentScore = sentimentAnalyzer.analyze(normalizedContent, detectedLanguage);
            analysisResults.add(new AnalysisLayer("sentiment", sentimentScore.getScore(), sentimentScore.getEmotions()));
            
            // 3. Context-Aware Analysis
            if (contextAwareAnalysis && context != null) {
                ContextScore contextScore = contextAnalyzer.analyze(normalizedContent, context);
                analysisResults.add(new AnalysisLayer("context", contextScore.getScore(), contextScore.getContextFactors()));
            }
            
            // 4. Pattern-Based Analysis (Fallback and Enhancement)
            PatternAnalysisResult patternResult = analyzePatterns(normalizedContent, detectedLanguage);
            analysisResults.add(new AnalysisLayer("patterns", patternResult.getScore(), patternResult.getMatchedPatterns()));
            
            // 5. Advanced Threat Detection
            ContentThreatAnalysisResult threatResult = analyzeThreatIndicators(normalizedContent, context);
            analysisResults.add(new AnalysisLayer("threats", threatResult.getScore(), threatResult.getThreatTypes()));
            
            // Consolidate results
            ContentAnalysisResult finalResult = consolidateAnalysisResults(analysisResults, content, detectedLanguage);
            
            // Update metrics
            updateAnalysisMetrics(finalResult, System.currentTimeMillis() - startTime);
            
            return finalResult;
            
        } catch (Exception e) {
            logger.error("Error during content analysis", e);
            return ContentAnalysisResult.error("Analysis failed: " + e.getMessage());
        }
    }
    
    /**
     * Preprocess content for analysis
     */
    private String preprocessContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "";
        }
        
        // Normalize whitespace and remove excessive formatting
        String normalized = content.trim().replaceAll("\\s+", " ");
        
        // Handle special characters and emojis
        normalized = normalizeSpecialCharacters(normalized);
        
        // Remove or normalize URLs while preserving context
        normalized = normalizeUrls(normalized);
        
        return normalized;
    }
    
    /**
     * Pattern-based analysis for known toxic patterns
     */
    private PatternAnalysisResult analyzePatterns(String content, String language) {
        List<String> matchedPatterns = new ArrayList<>();
        double totalScore = 0.0;
        
        // Get language-specific patterns
        List<Pattern> patterns = languageSpecificPatterns.getOrDefault(language, 
                                 languageSpecificPatterns.get("en")); // Default to English
        
        for (Pattern pattern : patterns) {
            if (pattern.matcher(content).find()) {
                String patternName = getPatternName(pattern);
                matchedPatterns.add(patternName);
                totalScore += patternWeights.getOrDefault(patternName, 0.5);
            }
        }
        
        // Normalize score
        double normalizedScore = Math.min(1.0, totalScore / patterns.size());
        
        return new PatternAnalysisResult(normalizedScore, matchedPatterns);
    }
    
    /**
     * Advanced threat indicator analysis
     */
    private ContentThreatAnalysisResult analyzeThreatIndicators(String content, ConversationContext context) {
        List<String> threatTypes = new ArrayList<>();
        double threatScore = 0.0;
        
        // Check for phishing indicators
        if (containsPhishingIndicators(content)) {
            threatTypes.add("phishing");
            threatScore += 0.8;
        }
        
        // Check for malware/scam indicators
        if (containsMalwareIndicators(content)) {
            threatTypes.add("malware");
            threatScore += 0.9;
        }
        
        // Check for social engineering
        if (containsSocialEngineeringIndicators(content, context)) {
            threatTypes.add("social_engineering");
            threatScore += 0.7;
        }
        
        // Check for spam indicators
        if (containsSpamIndicators(content)) {
            threatTypes.add("spam");
            threatScore += 0.6;
        }
        
        // Check for doxxing/personal info sharing
        if (containsPersonalInfoIndicators(content)) {
            threatTypes.add("personal_info");
            threatScore += 0.8;
        }
        
        return new ContentThreatAnalysisResult(Math.min(1.0, threatScore), threatTypes);
    }
    
    /**
     * Consolidate all analysis results into final decision
     */
    private ContentAnalysisResult consolidateAnalysisResults(List<AnalysisLayer> analysisResults, 
                                                           String originalContent, 
                                                           String detectedLanguage) {
        
        double weightedScore = 0.0;
        Map<String, Double> layerScores = new HashMap<>();
        List<String> detectionReasons = new ArrayList<>();
        
        // Weight different analysis layers
        Map<String, Double> layerWeights = Map.of(
            "toxicity", 0.35,
            "sentiment", 0.20,
            "context", 0.15,
            "patterns", 0.15,
            "threats", 0.15
        );
        
        for (AnalysisLayer layer : analysisResults) {
            double weight = layerWeights.getOrDefault(layer.getType(), 0.1);
            weightedScore += layer.getScore() * weight;
            layerScores.put(layer.getType(), layer.getScore());
            
            if (layer.getScore() > getThresholdForLayer(layer.getType())) {
                detectionReasons.addAll(layer.getDetails());
            }
        }
        
        // Determine if content is clean or problematic
        boolean isClean = weightedScore < toxicityThreshold;
        String detectionType = determineDetectionType(layerScores);
        
        return new ContentAnalysisResult(
            isClean,
            weightedScore,
            detectionType,
            detectionReasons,
            detectedLanguage,
            layerScores,
            originalContent.length()
        );
    }
    
    /**
     * Initialize language-specific pattern matching
     */
    private Map<String, List<Pattern>> initializeLanguagePatterns() {
        Map<String, List<Pattern>> patterns = new HashMap<>();
        
        // English patterns
        List<Pattern> englishPatterns = Arrays.asList(
            Pattern.compile("\\b(kill\\s+yourself|kys)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(f[u*]+ck\\s+you|f[u*]+ck\\s+off)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(hate\\s+speech|racial\\s+slur)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(discord\\s+nitro\\s+free|free\\s+nitro)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(click\\s+here|download\\s+now|limited\\s+time)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(scam|phishing|malware|virus)\\b", Pattern.CASE_INSENSITIVE)
        );
        patterns.put("en", englishPatterns);
        
        // Danish patterns (for Axion Bot's primary language)
        List<Pattern> danishPatterns = Arrays.asList(
            Pattern.compile("\\b(dræb\\s+dig\\s+selv|selvmord)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(fuck\\s+dig|skrid)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(hadtale|racistisk)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(gratis\\s+nitro|discord\\s+gave)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(klik\\s+her|download\\s+nu)\\b", Pattern.CASE_INSENSITIVE)
        );
        patterns.put("da", danishPatterns);
        
        // Add more languages as needed
        patterns.put("de", createGermanPatterns());
        patterns.put("fr", createFrenchPatterns());
        patterns.put("es", createSpanishPatterns());
        
        return patterns;
    }
    
    /**
     * Initialize pattern weights for scoring
     */
    private Map<String, Double> initializePatternWeights() {
        Map<String, Double> weights = new HashMap<>();
        weights.put("hate_speech", 0.9);
        weights.put("threats", 0.8);
        weights.put("harassment", 0.7);
        weights.put("spam", 0.6);
        weights.put("phishing", 0.8);
        weights.put("malware", 0.9);
        weights.put("personal_info", 0.7);
        return weights;
    }
    
    // Helper methods for threat detection
    private boolean containsPhishingIndicators(String content) {
        return content.toLowerCase().matches(".*(free\\s+nitro|discord\\s+gift|steam\\s+gift|click\\s+here).*");
    }
    
    private boolean containsMalwareIndicators(String content) {
        return content.toLowerCase().matches(".*(download\\s+now|install\\s+this|run\\s+this|exe\\s+file).*");
    }
    
    private boolean containsSocialEngineeringIndicators(String content, ConversationContext context) {
        boolean hasUrgency = content.toLowerCase().matches(".*(urgent|immediately|now|quick|fast|limited\\s+time).*");
        boolean hasAuthority = content.toLowerCase().matches(".*(admin|moderator|staff|official|discord\\s+team).*");
        return hasUrgency && hasAuthority;
    }
    
    private boolean containsSpamIndicators(String content) {
        return content.toLowerCase().matches(".*(join\\s+my\\s+server|check\\s+out|visit\\s+my|dm\\s+me).*");
    }
    
    private boolean containsPersonalInfoIndicators(String content) {
        return content.matches(".*(\\d{3}-\\d{2}-\\d{4}|\\d{4}\\s*-\\s*\\d{4}\\s*-\\s*\\d{4}\\s*-\\s*\\d{4}).*");
    }
    
    private String normalizeSpecialCharacters(String content) {
        // Replace common character substitutions used to bypass filters
        return content
            .replaceAll("[4@]", "a")
            .replaceAll("[3€]", "e")
            .replaceAll("[1!|]", "i")
            .replaceAll("[0°]", "o")
            .replaceAll("[5$]", "s")
            .replaceAll("[7]", "t");
    }
    
    private String normalizeUrls(String content) {
        // Replace URLs with placeholder while preserving context
        return content.replaceAll("https?://[^\\s]+", "[URL]");
    }
    
    private String getPatternName(Pattern pattern) {
        // Simple pattern name extraction (in real implementation, maintain a pattern registry)
        String patternStr = pattern.pattern().toLowerCase();
        if (patternStr.contains("kill") || patternStr.contains("die")) return "threats";
        if (patternStr.contains("hate") || patternStr.contains("racial")) return "hate_speech";
        if (patternStr.contains("nitro") || patternStr.contains("free")) return "phishing";
        if (patternStr.contains("fuck") || patternStr.contains("shit")) return "profanity";
        return "general";
    }
    
    private double getThresholdForLayer(String layerType) {
        switch (layerType) {
            case "toxicity": return toxicityThreshold;
            case "sentiment": return Math.abs(sentimentThreshold);
            case "context": return 0.6;
            case "patterns": return 0.5;
            case "threats": return 0.7;
            default: return 0.5;
        }
    }
    
    private String determineDetectionType(Map<String, Double> layerScores) {
        return layerScores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("unknown");
    }
    
    private void updateAnalysisMetrics(ContentAnalysisResult result, long processingTime) {
        analysisMetrics.put("total_analyses", analysisMetrics.getOrDefault("total_analyses", 0L) + 1);
        analysisMetrics.put("avg_processing_time", 
            (analysisMetrics.getOrDefault("avg_processing_time", 0L) + processingTime) / 2);
        
        if (!result.isClean()) {
            detectionCounts.merge(result.getDetectionType(), 1, Integer::sum);
        }
    }
    
    // Language-specific pattern creators
    private List<Pattern> createGermanPatterns() {
        return Arrays.asList(
            Pattern.compile("\\b(töte\\s+dich|selbstmord)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(fick\\s+dich|verpiss\\s+dich)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(hassrede|rassistisch)\\b", Pattern.CASE_INSENSITIVE)
        );
    }
    
    private List<Pattern> createFrenchPatterns() {
        return Arrays.asList(
            Pattern.compile("\\b(tue-toi|suicide)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(va\\s+te\\s+faire|merde)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(discours\\s+de\\s+haine|raciste)\\b", Pattern.CASE_INSENSITIVE)
        );
    }
    
    private List<Pattern> createSpanishPatterns() {
        return Arrays.asList(
            Pattern.compile("\\b(mátate|suicidio)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(jódete|vete\\s+a\\s+la\\s+mierda)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(discurso\\s+de\\s+odio|racista)\\b", Pattern.CASE_INSENSITIVE)
        );
    }
    
    /**
     * Get analysis performance metrics
     */
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("analysis_metrics", new HashMap<>(analysisMetrics));
        metrics.put("detection_counts", new HashMap<>(detectionCounts));
        metrics.put("configuration", Map.of(
            "toxicity_threshold", toxicityThreshold,
            "sentiment_threshold", sentimentThreshold,
            "context_aware", contextAwareAnalysis,
            "multi_language", multiLanguageSupport
        ));
        return metrics;
    }
    
    /**
     * Update configuration
     */
    public void updateConfiguration(double toxicityThreshold, double sentimentThreshold, 
                                  boolean contextAware, boolean multiLanguage) {
        this.toxicityThreshold = toxicityThreshold;
        this.sentimentThreshold = sentimentThreshold;
        this.contextAwareAnalysis = contextAware;
        this.multiLanguageSupport = multiLanguage;
        
        logger.info("AIContentAnalyzer configuration updated");
    }
}

// Supporting classes for AI analysis
class ToxicityModel {
    public ToxicityScore analyze(String content, String language) {
        // Simplified ML model simulation
        double score = calculateToxicityScore(content);
        List<String> categories = identifyToxicityCategories(content);
        return new ToxicityScore(score, categories);
    }
    
    private double calculateToxicityScore(String content) {
        // Simplified scoring based on content analysis
        double score = 0.0;
        String lower = content.toLowerCase();
        
        if (lower.contains("kill") || lower.contains("die")) score += 0.8;
        if (lower.contains("hate") || lower.contains("stupid")) score += 0.6;
        if (lower.matches(".*f[u*]+ck.*")) score += 0.7;
        if (lower.matches(".*[!?]{3,}.*")) score += 0.2;
        if (lower.matches(".*[A-Z]{5,}.*")) score += 0.3;
        
        return Math.min(1.0, score);
    }
    
    private List<String> identifyToxicityCategories(String content) {
        List<String> categories = new ArrayList<>();
        String lower = content.toLowerCase();
        
        if (lower.contains("kill") || lower.contains("die")) categories.add("threats");
        if (lower.contains("hate")) categories.add("hate_speech");
        if (lower.matches(".*f[u*]+ck.*")) categories.add("profanity");
        if (lower.contains("stupid") || lower.contains("idiot")) categories.add("insults");
        
        return categories;
    }
}

class SentimentAnalyzer {
    public SentimentScore analyze(String content, String language) {
        double score = calculateSentimentScore(content);
        List<String> emotions = identifyEmotions(content);
        return new SentimentScore(score, emotions);
    }
    
    private double calculateSentimentScore(String content) {
        // Simplified sentiment analysis (-1 to 1, where -1 is very negative)
        double score = 0.0;
        String lower = content.toLowerCase();
        
        // Negative indicators
        if (lower.contains("hate") || lower.contains("angry")) score -= 0.7;
        if (lower.contains("sad") || lower.contains("depressed")) score -= 0.5;
        if (lower.contains("annoying") || lower.contains("stupid")) score -= 0.6;
        
        // Positive indicators
        if (lower.contains("love") || lower.contains("happy")) score += 0.7;
        if (lower.contains("great") || lower.contains("awesome")) score += 0.6;
        if (lower.contains("thanks") || lower.contains("please")) score += 0.4;
        
        return Math.max(-1.0, Math.min(1.0, score));
    }
    
    private List<String> identifyEmotions(String content) {
        List<String> emotions = new ArrayList<>();
        String lower = content.toLowerCase();
        
        if (lower.contains("angry") || lower.contains("mad")) emotions.add("anger");
        if (lower.contains("sad") || lower.contains("cry")) emotions.add("sadness");
        if (lower.contains("happy") || lower.contains("joy")) emotions.add("joy");
        if (lower.contains("fear") || lower.contains("scared")) emotions.add("fear");
        
        return emotions;
    }
}

class ContextAnalyzer {
    public ContextScore analyze(String content, ConversationContext context) {
        double score = calculateContextScore(content, context);
        List<String> factors = identifyContextFactors(content, context);
        return new ContextScore(score, factors);
    }
    
    private double calculateContextScore(String content, ConversationContext context) {
        double score = 0.0;
        
        // Consider conversation history
        if (context.hasRecentViolations()) {
            score += 0.3;
        }
        
        // Consider user behavior patterns
        if (context.getUserProfile().isHighRisk()) {
            score += 0.4;
        }
        
        // Consider channel context
        if (context.isInSensitiveChannel()) {
            score += 0.2;
        }
        
        return Math.min(1.0, score);
    }
    
    private List<String> identifyContextFactors(String content, ConversationContext context) {
        List<String> factors = new ArrayList<>();
        
        if (context.hasRecentViolations()) factors.add("recent_violations");
        if (context.getUserProfile().isHighRisk()) factors.add("high_risk_user");
        if (context.isInSensitiveChannel()) factors.add("sensitive_channel");
        
        return factors;
    }
}

class LanguageDetector {
    public String detect(String content) {
        // Simplified language detection
        if (content.matches(".*\\b(the|and|or|but|in|on|at|to|for|of|with|by)\\b.*")) {
            return "en";
        } else if (content.matches(".*\\b(og|eller|men|i|på|til|for|af|med|ved)\\b.*")) {
            return "da";
        } else if (content.matches(".*\\b(und|oder|aber|in|auf|zu|für|von|mit|bei)\\b.*")) {
            return "de";
        }
        return "en"; // Default to English
    }
}

// Result classes
class ContentAnalysisResult {
    private final boolean isClean;
    private final double confidence;
    private final String detectionType;
    private final List<String> reasons;
    private final String language;
    private final Map<String, Double> layerScores;
    private final int contentLength;
    
    public ContentAnalysisResult(boolean isClean, double confidence, String detectionType, 
                               List<String> reasons, String language, 
                               Map<String, Double> layerScores, int contentLength) {
        this.isClean = isClean;
        this.confidence = confidence;
        this.detectionType = detectionType;
        this.reasons = reasons;
        this.language = language;
        this.layerScores = layerScores;
        this.contentLength = contentLength;
    }
    
    public static ContentAnalysisResult error(String message) {
        return new ContentAnalysisResult(true, 0.0, "error", Arrays.asList(message), 
                                       "unknown", new HashMap<>(), 0);
    }
    
    // Getters
    public boolean isClean() { return isClean; }
    public double getConfidence() { return confidence; }
    public String getDetectionType() { return detectionType; }
    public List<String> getReasons() { return reasons; }
    public String getLanguage() { return language; }
    public Map<String, Double> getLayerScores() { return layerScores; }
    public int getContentLength() { return contentLength; }
}

// Additional supporting classes
class ToxicityScore {
    private final double score;
    private final List<String> categories;
    
    public ToxicityScore(double score, List<String> categories) {
        this.score = score;
        this.categories = categories;
    }
    
    public double getScore() { return score; }
    public List<String> getCategories() { return categories; }
}

class SentimentScore {
    private final double score;
    private final List<String> emotions;
    
    public SentimentScore(double score, List<String> emotions) {
        this.score = score;
        this.emotions = emotions;
    }
    
    public double getScore() { return score; }
    public List<String> getEmotions() { return emotions; }
}

class ContextScore {
    private final double score;
    private final List<String> contextFactors;
    
    public ContextScore(double score, List<String> contextFactors) {
        this.score = score;
        this.contextFactors = contextFactors;
    }
    
    public double getScore() { return score; }
    public List<String> getContextFactors() { return contextFactors; }
}

class PatternAnalysisResult {
    private final double score;
    private final List<String> matchedPatterns;
    
    public PatternAnalysisResult(double score, List<String> matchedPatterns) {
        this.score = score;
        this.matchedPatterns = matchedPatterns;
    }
    
    public double getScore() { return score; }
    public List<String> getMatchedPatterns() { return matchedPatterns; }
}

class ContentThreatAnalysisResult {
    private final double score;
    private final List<String> threatTypes;
    
    public ContentThreatAnalysisResult(double score, List<String> threatTypes) {
        this.score = score;
        this.threatTypes = threatTypes;
    }
    
    public double getScore() { return score; }
    public List<String> getThreatTypes() { return threatTypes; }
}

class AnalysisLayer {
    private final String type;
    private final double score;
    private final List<String> details;
    
    public AnalysisLayer(String type, double score, List<String> details) {
        this.type = type;
        this.score = score;
        this.details = details;
    }
    
    public String getType() { return type; }
    public double getScore() { return score; }
    public List<String> getDetails() { return details; }
}