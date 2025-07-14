package com.axion.bot.moderation;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Enhanced Appeal and Review System for moderation transparency and user experience
 * Provides automated appeal processing, review workflows, and appeal analytics
 */
public class EnhancedAppealSystem {
    private static final Logger logger = LoggerFactory.getLogger(EnhancedAppealSystem.class);
    
    // Appeal storage and caching
    private final Map<String, Appeal> activeAppeals = new ConcurrentHashMap<>();
    private final Map<String, List<Appeal>> userAppealHistory = new ConcurrentHashMap<>();
    private final Cache<String, AppealAnalysis> appealAnalysisCache;
    
    // Review workflow management
    private final ReviewWorkflowManager workflowManager;
    private final AppealAnalyzer appealAnalyzer;
    private final AutoReviewEngine autoReviewEngine;
    private final NotificationManager notificationManager;
    
    // Performance tracking
    private final AtomicLong totalAppeals = new AtomicLong(0);
    private final AtomicLong approvedAppeals = new AtomicLong(0);
    private final AtomicLong rejectedAppeals = new AtomicLong(0);
    private final AtomicLong autoProcessedAppeals = new AtomicLong(0);
    
    // Configuration
    private final AppealSystemConfig config;
    
    public EnhancedAppealSystem(AppealSystemConfig config) {
        this.config = config;
        
        // Initialize cache
        this.appealAnalysisCache = Caffeine.newBuilder()
            .maximumSize(config.getMaxCachedAnalyses())
            .expireAfterWrite(Duration.ofHours(config.getAnalysisCacheHours()))
            .build();
        
        // Initialize components
        this.workflowManager = new ReviewWorkflowManager(config.getWorkflowConfig());
        this.appealAnalyzer = new AppealAnalyzer(config.getAnalyzerConfig());
        this.autoReviewEngine = new AutoReviewEngine(config.getAutoReviewConfig());
        this.notificationManager = new NotificationManager(config.getNotificationConfig());
        
        logger.info("EnhancedAppealSystem initialized with auto-review enabled: {}", 
                   config.isAutoReviewEnabled());
    }
    
    /**
     * Submit a new appeal
     */
    public AppealSubmissionResult submitAppeal(AppealRequest request) {
        long startTime = System.currentTimeMillis();
        totalAppeals.incrementAndGet();
        
        try {
            // Validate appeal request
            ValidationResult validation = validateAppealRequest(request);
            if (!validation.isValid()) {
                return AppealSubmissionResult.rejected(validation.getErrors());
            }
            
            // Check for duplicate appeals
            if (hasDuplicateAppeal(request)) {
                return AppealSubmissionResult.rejected(
                    Arrays.asList("Duplicate appeal detected. Please wait for your existing appeal to be processed.")
                );
            }
            
            // Create appeal
            Appeal appeal = createAppeal(request);
            
            // Analyze appeal content and context
            AppealAnalysis analysis = analyzeAppeal(appeal);
            appeal.setAnalysis(analysis);
            
            // Determine processing path
            ProcessingPath path = determineProcessingPath(appeal, analysis);
            appeal.setProcessingPath(path);
            
            // Store appeal
            activeAppeals.put(appeal.getId(), appeal);
            addToUserHistory(appeal.getUserId(), appeal);
            
            // Process based on path
            AppealSubmissionResult result;
            if (path == ProcessingPath.AUTO_REVIEW && config.isAutoReviewEnabled()) {
                result = processAutoReview(appeal);
                autoProcessedAppeals.incrementAndGet();
            } else {
                result = queueForManualReview(appeal);
            }
            
            // Send notifications
            notificationManager.notifyAppealSubmitted(appeal, result);
            
            // Log appeal submission
            logger.info("Appeal submitted: {} by user {} in guild {} - Path: {}", 
                       appeal.getId(), appeal.getUserId(), appeal.getGuildId(), path);
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error submitting appeal for user {} in guild {}", 
                        request.getUserId(), request.getGuildId(), e);
            return AppealSubmissionResult.error("Appeal submission failed: " + e.getMessage());
        }
    }
    
    /**
     * Process manual review of an appeal
     */
    public ReviewResult processManualReview(String appealId, String reviewerId, 
                                          ReviewDecision decision, String reviewNotes) {
        try {
            Appeal appeal = activeAppeals.get(appealId);
            if (appeal == null) {
                return ReviewResult.error("Appeal not found: " + appealId);
            }
            
            if (appeal.getStatus() != AppealStatus.PENDING_REVIEW) {
                return ReviewResult.error("Appeal is not in reviewable state: " + appeal.getStatus());
            }
            
            // Create review record
            AppealReview review = new AppealReview(
                reviewerId,
                decision,
                reviewNotes,
                Instant.now()
            );
            
            // Update appeal
            appeal.addReview(review);
            appeal.setStatus(decision == ReviewDecision.APPROVED ? 
                           AppealStatus.APPROVED : AppealStatus.REJECTED);
            appeal.setResolvedAt(Instant.now());
            
            // Update statistics
            if (decision == ReviewDecision.APPROVED) {
                approvedAppeals.incrementAndGet();
            } else {
                rejectedAppeals.incrementAndGet();
            }
            
            // Execute appeal decision
            AppealExecutionResult executionResult = executeAppealDecision(appeal, decision);
            
            // Send notifications
            notificationManager.notifyAppealDecision(appeal, review, executionResult);
            
            // Log review
            logger.info("Appeal {} reviewed by {}: {} - {}", 
                       appealId, reviewerId, decision, reviewNotes);
            
            return ReviewResult.success(appeal, review, executionResult);
            
        } catch (Exception e) {
            logger.error("Error processing manual review for appeal {}", appealId, e);
            return ReviewResult.error("Review processing failed: " + e.getMessage());
        }
    }
    
    /**
     * Get appeal status and details
     */
    public AppealStatusResult getAppealStatus(String appealId) {
        Appeal appeal = activeAppeals.get(appealId);
        if (appeal == null) {
            return AppealStatusResult.notFound(appealId);
        }
        
        return new AppealStatusResult(appeal, calculateEstimatedProcessingTime(appeal));
    }
    
    /**
     * Get user's appeal history
     */
    public UserAppealHistory getUserAppealHistory(String userId, String guildId) {
        List<Appeal> userAppeals = userAppealHistory.getOrDefault(userId, new ArrayList<>())
            .stream()
            .filter(appeal -> appeal.getGuildId().equals(guildId))
            .sorted(Comparator.comparing(Appeal::getSubmittedAt).reversed())
            .collect(Collectors.toList());
        
        return new UserAppealHistory(userId, guildId, userAppeals, 
                                   calculateUserAppealStats(userAppeals));
    }
    
    /**
     * Get pending appeals for review
     */
    public List<Appeal> getPendingAppeals(String guildId, int limit) {
        return activeAppeals.values().stream()
            .filter(appeal -> appeal.getGuildId().equals(guildId))
            .filter(appeal -> appeal.getStatus() == AppealStatus.PENDING_REVIEW)
            .sorted(Comparator.comparing(Appeal::getSubmittedAt))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    /**
     * Get appeal analytics for a guild
     */
    public AppealAnalytics getAppealAnalytics(String guildId, Duration period) {
        Instant since = Instant.now().minus(period);
        
        List<Appeal> periodAppeals = activeAppeals.values().stream()
            .filter(appeal -> appeal.getGuildId().equals(guildId))
            .filter(appeal -> appeal.getSubmittedAt().isAfter(since))
            .collect(Collectors.toList());
        
        return calculateAppealAnalytics(periodAppeals, period);
    }
    
    /**
     * Validate appeal request
     */
    private ValidationResult validateAppealRequest(AppealRequest request) {
        List<String> errors = new ArrayList<>();
        
        if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
            errors.add("User ID is required");
        }
        
        if (request.getGuildId() == null || request.getGuildId().trim().isEmpty()) {
            errors.add("Guild ID is required");
        }
        
        if (request.getViolationId() == null || request.getViolationId().trim().isEmpty()) {
            errors.add("Violation ID is required");
        }
        
        if (request.getReason() == null || request.getReason().trim().isEmpty()) {
            errors.add("Appeal reason is required");
        } else if (request.getReason().length() < config.getMinAppealLength()) {
            errors.add("Appeal reason is too short (minimum " + config.getMinAppealLength() + " characters)");
        } else if (request.getReason().length() > config.getMaxAppealLength()) {
            errors.add("Appeal reason is too long (maximum " + config.getMaxAppealLength() + " characters)");
        }
        
        // Check appeal cooldown
        if (hasRecentAppeal(request.getUserId(), request.getGuildId())) {
            errors.add("You must wait " + config.getAppealCooldownHours() + " hours between appeals");
        }
        
        // Check maximum appeals per user
        if (getUserActiveAppealCount(request.getUserId(), request.getGuildId()) >= config.getMaxAppealsPerUser()) {
            errors.add("Maximum number of active appeals reached");
        }
        
        return new ValidationResult(errors.isEmpty(), errors, new ArrayList<>());
    }
    
    /**
     * Check for duplicate appeals
     */
    private boolean hasDuplicateAppeal(AppealRequest request) {
        return activeAppeals.values().stream()
            .anyMatch(appeal -> 
                appeal.getUserId().equals(request.getUserId()) &&
                appeal.getGuildId().equals(request.getGuildId()) &&
                appeal.getViolationId().equals(request.getViolationId()) &&
                appeal.getStatus() == AppealStatus.PENDING_REVIEW
            );
    }
    
    /**
     * Create appeal from request
     */
    private Appeal createAppeal(AppealRequest request) {
        String appealId = generateAppealId();
        
        return new Appeal(
            appealId,
            request.getUserId(),
            request.getGuildId(),
            request.getViolationId(),
            request.getReason(),
            request.getEvidence(),
            AppealStatus.PENDING_ANALYSIS,
            Instant.now()
        );
    }
    
    /**
     * Analyze appeal content and context
     */
    private AppealAnalysis analyzeAppeal(Appeal appeal) {
        String cacheKey = generateAnalysisCacheKey(appeal);
        AppealAnalysis cached = appealAnalysisCache.getIfPresent(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        AppealAnalysis analysis = appealAnalyzer.analyze(appeal);
        appealAnalysisCache.put(cacheKey, analysis);
        
        return analysis;
    }
    
    /**
     * Determine processing path for appeal
     */
    private ProcessingPath determineProcessingPath(Appeal appeal, AppealAnalysis analysis) {
        // High-confidence auto-approval cases
        if (analysis.getAutoApprovalConfidence() >= config.getAutoApprovalThreshold()) {
            return ProcessingPath.AUTO_REVIEW;
        }
        
        // High-confidence auto-rejection cases
        if (analysis.getAutoRejectionConfidence() >= config.getAutoRejectionThreshold()) {
            return ProcessingPath.AUTO_REVIEW;
        }
        
        // Complex cases requiring human review
        if (analysis.getComplexityScore() >= config.getComplexityThreshold()) {
            return ProcessingPath.MANUAL_REVIEW;
        }
        
        // Priority cases (e.g., severe violations, repeat offenders)
        if (analysis.isPriorityCase()) {
            return ProcessingPath.PRIORITY_REVIEW;
        }
        
        // Default to auto-review if enabled, otherwise manual
        return config.isAutoReviewEnabled() ? ProcessingPath.AUTO_REVIEW : ProcessingPath.MANUAL_REVIEW;
    }
    
    /**
     * Process auto-review
     */
    private AppealSubmissionResult processAutoReview(Appeal appeal) {
        AutoReviewResult autoResult = autoReviewEngine.processAppeal(appeal);
        
        appeal.setStatus(autoResult.getDecision() == ReviewDecision.APPROVED ? 
                        AppealStatus.APPROVED : AppealStatus.REJECTED);
        appeal.setResolvedAt(Instant.now());
        
        // Add auto-review record
        AppealReview autoReview = new AppealReview(
            "AUTO_REVIEW_SYSTEM",
            autoResult.getDecision(),
            autoResult.getReason(),
            Instant.now()
        );
        appeal.addReview(autoReview);
        
        // Update statistics
        if (autoResult.getDecision() == ReviewDecision.APPROVED) {
            approvedAppeals.incrementAndGet();
        } else {
            rejectedAppeals.incrementAndGet();
        }
        
        // Execute decision
        AppealExecutionResult executionResult = executeAppealDecision(appeal, autoResult.getDecision());
        
        return AppealSubmissionResult.autoProcessed(appeal, autoResult, executionResult);
    }
    
    /**
     * Queue appeal for manual review
     */
    private AppealSubmissionResult queueForManualReview(Appeal appeal) {
        appeal.setStatus(AppealStatus.PENDING_REVIEW);
        
        // Add to review workflow
        workflowManager.addToReviewQueue(appeal);
        
        Duration estimatedTime = calculateEstimatedProcessingTime(appeal);
        
        return AppealSubmissionResult.queued(appeal, estimatedTime);
    }
    
    /**
     * Execute appeal decision (unban, remove timeout, etc.)
     */
    private AppealExecutionResult executeAppealDecision(Appeal appeal, ReviewDecision decision) {
        if (decision != ReviewDecision.APPROVED) {
            return AppealExecutionResult.noAction("Appeal was not approved");
        }
        
        try {
            // This would integrate with the actual moderation system
            // For now, we'll simulate the execution
            
            List<String> actionsPerformed = new ArrayList<>();
            
            // Determine what actions to reverse based on the original violation
            String violationType = getViolationType(appeal.getViolationId());
            
            switch (violationType) {
                case "BAN":
                    // Unban user
                    actionsPerformed.add("User unbanned");
                    break;
                case "TIMEOUT":
                    // Remove timeout
                    actionsPerformed.add("Timeout removed");
                    break;
                case "WARN":
                    // Remove warning from record
                    actionsPerformed.add("Warning removed from record");
                    break;
                case "KICK":
                    // Send reinvite link
                    actionsPerformed.add("Reinvite link sent");
                    break;
                default:
                    actionsPerformed.add("Violation record updated");
            }
            
            return AppealExecutionResult.success(actionsPerformed);
            
        } catch (Exception e) {
            logger.error("Error executing appeal decision for appeal {}", appeal.getId(), e);
            return AppealExecutionResult.error("Failed to execute appeal decision: " + e.getMessage());
        }
    }
    
    /**
     * Calculate estimated processing time
     */
    private Duration calculateEstimatedProcessingTime(Appeal appeal) {
        // Base processing time
        Duration baseTime = Duration.ofHours(config.getBaseProcessingHours());
        
        // Adjust based on queue length
        int queueLength = getReviewQueueLength(appeal.getGuildId());
        Duration queueDelay = Duration.ofMinutes(config.getProcessingTimePerAppeal() * queueLength);
        
        // Adjust based on complexity
        if (appeal.getAnalysis() != null) {
            double complexityMultiplier = 1.0 + (appeal.getAnalysis().getComplexityScore() * 0.5);
            baseTime = baseTime.multipliedBy((long) complexityMultiplier);
        }
        
        // Adjust based on processing path
        if (appeal.getProcessingPath() == ProcessingPath.PRIORITY_REVIEW) {
            baseTime = baseTime.dividedBy(2); // Priority cases processed faster
        }
        
        return baseTime.plus(queueDelay);
    }
    
    /**
     * Calculate appeal analytics
     */
    private AppealAnalytics calculateAppealAnalytics(List<Appeal> appeals, Duration period) {
        int totalAppeals = appeals.size();
        int approvedCount = (int) appeals.stream().filter(a -> a.getStatus() == AppealStatus.APPROVED).count();
        int rejectedCount = (int) appeals.stream().filter(a -> a.getStatus() == AppealStatus.REJECTED).count();
        int pendingCount = (int) appeals.stream().filter(a -> a.getStatus() == AppealStatus.PENDING_REVIEW).count();
        
        double approvalRate = totalAppeals > 0 ? (double) approvedCount / totalAppeals : 0.0;
        
        // Calculate average processing time for resolved appeals
        OptionalDouble avgProcessingTime = appeals.stream()
            .filter(a -> a.getResolvedAt() != null)
            .mapToLong(a -> Duration.between(a.getSubmittedAt(), a.getResolvedAt()).toMinutes())
            .average();
        
        // Calculate auto-processing rate
        long autoProcessedCount = appeals.stream()
            .filter(a -> a.getReviews().stream()
                .anyMatch(r -> "AUTO_REVIEW_SYSTEM".equals(r.getReviewerId())))
            .count();
        
        double autoProcessingRate = totalAppeals > 0 ? (double) autoProcessedCount / totalAppeals : 0.0;
        
        // Most common violation types
        Map<String, Long> violationTypes = appeals.stream()
            .collect(Collectors.groupingBy(
                a -> getViolationType(a.getViolationId()),
                Collectors.counting()
            ));
        
        return new AppealAnalytics(
            totalAppeals,
            approvedCount,
            rejectedCount,
            pendingCount,
            approvalRate,
            avgProcessingTime.orElse(0.0),
            autoProcessingRate,
            violationTypes,
            period
        );
    }
    
    // Helper methods
    private boolean hasRecentAppeal(String userId, String guildId) {
        Instant cutoff = Instant.now().minus(Duration.ofHours(config.getAppealCooldownHours()));
        
        return userAppealHistory.getOrDefault(userId, new ArrayList<>()).stream()
            .anyMatch(appeal -> 
                appeal.getGuildId().equals(guildId) &&
                appeal.getSubmittedAt().isAfter(cutoff)
            );
    }
    
    private int getUserActiveAppealCount(String userId, String guildId) {
        return (int) activeAppeals.values().stream()
            .filter(appeal -> 
                appeal.getUserId().equals(userId) &&
                appeal.getGuildId().equals(guildId) &&
                appeal.getStatus() == AppealStatus.PENDING_REVIEW
            )
            .count();
    }
    
    private void addToUserHistory(String userId, Appeal appeal) {
        userAppealHistory.computeIfAbsent(userId, k -> new ArrayList<>()).add(appeal);
    }
    
    private String generateAppealId() {
        return "APPEAL_" + System.currentTimeMillis() + "_" + 
               Integer.toHexString(new Random().nextInt());
    }
    
    private String generateAnalysisCacheKey(Appeal appeal) {
        return appeal.getUserId() + ":" + appeal.getViolationId() + ":" + 
               appeal.getReason().hashCode();
    }
    
    private String getViolationType(String violationId) {
        // This would integrate with the actual violation tracking system
        // For now, we'll extract from the violation ID or use a default
        if (violationId.contains("BAN")) return "BAN";
        if (violationId.contains("TIMEOUT")) return "TIMEOUT";
        if (violationId.contains("WARN")) return "WARN";
        if (violationId.contains("KICK")) return "KICK";
        return "OTHER";
    }
    
    private int getReviewQueueLength(String guildId) {
        return (int) activeAppeals.values().stream()
            .filter(appeal -> 
                appeal.getGuildId().equals(guildId) &&
                appeal.getStatus() == AppealStatus.PENDING_REVIEW
            )
            .count();
    }
    
    private UserAppealStats calculateUserAppealStats(List<Appeal> userAppeals) {
        int total = userAppeals.size();
        int approved = (int) userAppeals.stream().filter(a -> a.getStatus() == AppealStatus.APPROVED).count();
        int rejected = (int) userAppeals.stream().filter(a -> a.getStatus() == AppealStatus.REJECTED).count();
        int pending = (int) userAppeals.stream().filter(a -> a.getStatus() == AppealStatus.PENDING_REVIEW).count();
        
        return new UserAppealStats(total, approved, rejected, pending);
    }
    
    /**
     * Get system performance metrics
     */
    public AppealSystemMetrics getMetrics() {
        return new AppealSystemMetrics(
            totalAppeals.get(),
            approvedAppeals.get(),
            rejectedAppeals.get(),
            autoProcessedAppeals.get(),
            activeAppeals.size(),
            appealAnalysisCache.estimatedSize()
        );
    }
    
    /**
     * Perform system maintenance
     */
    public void performMaintenance() {
        logger.info("Performing appeal system maintenance");
        
        // Clean up old resolved appeals
        Instant cutoff = Instant.now().minus(Duration.ofDays(config.getAppealRetentionDays()));
        
        activeAppeals.entrySet().removeIf(entry -> {
            Appeal appeal = entry.getValue();
            return appeal.getResolvedAt() != null && appeal.getResolvedAt().isBefore(cutoff);
        });
        
        // Clean up old user history
        userAppealHistory.values().forEach(history -> 
            history.removeIf(appeal -> 
                appeal.getResolvedAt() != null && appeal.getResolvedAt().isBefore(cutoff)));
        
        // Clean up cache
        appealAnalysisCache.cleanUp();
        
        logger.info("Appeal system maintenance completed");
    }
}

// Supporting enums and classes
enum AppealStatus {
    PENDING_ANALYSIS, PENDING_REVIEW, APPROVED, REJECTED, EXPIRED
}

enum ProcessingPath {
    AUTO_REVIEW, MANUAL_REVIEW, PRIORITY_REVIEW
}

enum ReviewDecision {
    APPROVED, REJECTED
}

// Configuration class
class AppealSystemConfig {
    private boolean autoReviewEnabled = true;
    private int maxCachedAnalyses = 5000;
    private int analysisCacheHours = 6;
    private int minAppealLength = 50;
    private int maxAppealLength = 2000;
    private int appealCooldownHours = 24;
    private int maxAppealsPerUser = 3;
    private double autoApprovalThreshold = 0.8;
    private double autoRejectionThreshold = 0.8;
    private double complexityThreshold = 0.6;
    private int baseProcessingHours = 24;
    private int processingTimePerAppeal = 30; // minutes
    private int appealRetentionDays = 90;
    
    private WorkflowConfig workflowConfig = new WorkflowConfig();
    private AppealAnalyzerConfig analyzerConfig = new AppealAnalyzerConfig();
    private AutoReviewConfig autoReviewConfig = new AutoReviewConfig();
    private NotificationConfig notificationConfig = new NotificationConfig();
    
    // Getters
    public boolean isAutoReviewEnabled() { return autoReviewEnabled; }
    public int getMaxCachedAnalyses() { return maxCachedAnalyses; }
    public int getAnalysisCacheHours() { return analysisCacheHours; }
    public int getMinAppealLength() { return minAppealLength; }
    public int getMaxAppealLength() { return maxAppealLength; }
    public int getAppealCooldownHours() { return appealCooldownHours; }
    public int getMaxAppealsPerUser() { return maxAppealsPerUser; }
    public double getAutoApprovalThreshold() { return autoApprovalThreshold; }
    public double getAutoRejectionThreshold() { return autoRejectionThreshold; }
    public double getComplexityThreshold() { return complexityThreshold; }
    public int getBaseProcessingHours() { return baseProcessingHours; }
    public int getProcessingTimePerAppeal() { return processingTimePerAppeal; }
    public int getAppealRetentionDays() { return appealRetentionDays; }
    public WorkflowConfig getWorkflowConfig() { return workflowConfig; }
    public AppealAnalyzerConfig getAnalyzerConfig() { return analyzerConfig; }
    public AutoReviewConfig getAutoReviewConfig() { return autoReviewConfig; }
    public NotificationConfig getNotificationConfig() { return notificationConfig; }
}

// Placeholder configuration classes
class WorkflowConfig {
    private int maxQueueSize = 1000;
    private boolean priorityQueueEnabled = true;
    
    public int getMaxQueueSize() { return maxQueueSize; }
    public boolean isPriorityQueueEnabled() { return priorityQueueEnabled; }
}

class AppealAnalyzerConfig {
    private boolean sentimentAnalysisEnabled = true;
    private boolean contextAnalysisEnabled = true;
    private double sinceritThreshold = 0.6;
    
    public boolean isSentimentAnalysisEnabled() { return sentimentAnalysisEnabled; }
    public boolean isContextAnalysisEnabled() { return contextAnalysisEnabled; }
    public double getSincerityThreshold() { return sinceritThreshold; }
}

class AutoReviewConfig {
    private boolean enabled = true;
    private double confidenceThreshold = 0.8;
    private boolean requireHumanReviewForBans = true;
    
    public boolean isEnabled() { return enabled; }
    public double getConfidenceThreshold() { return confidenceThreshold; }
    public boolean isRequireHumanReviewForBans() { return requireHumanReviewForBans; }
}

class NotificationConfig {
    private boolean notifySubmission = true;
    private boolean notifyDecision = true;
    private boolean notifyModerators = true;
    
    public boolean isNotifySubmission() { return notifySubmission; }
    public boolean isNotifyDecision() { return notifyDecision; }
    public boolean isNotifyModerators() { return notifyModerators; }
}