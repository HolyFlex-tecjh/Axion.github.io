package com.axion.bot.moderation;

import java.util.*;

/**
 * Manager class for handling appeal review workflows
 */
public class ReviewWorkflowManager {
    private final WorkflowConfig config;
    private final Queue<Appeal> reviewQueue = new LinkedList<>();
    private final Queue<Appeal> priorityQueue = new LinkedList<>();
    
    public ReviewWorkflowManager(WorkflowConfig config) {
        this.config = config;
    }
    
    public void addToReviewQueue(Appeal appeal) {
        if (appeal.getProcessingPath() == ProcessingPath.PRIORITY_REVIEW && config.isPriorityQueueEnabled()) {
            priorityQueue.offer(appeal);
        } else {
            reviewQueue.offer(appeal);
        }
    }
    
    public Appeal getNextAppealForReview() {
        // Priority queue first
        Appeal appeal = priorityQueue.poll();
        if (appeal != null) {
            return appeal;
        }
        
        // Then regular queue
        return reviewQueue.poll();
    }
    
    public int getQueueSize() {
        return reviewQueue.size() + priorityQueue.size();
    }
}