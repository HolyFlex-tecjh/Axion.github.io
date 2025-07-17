package com.axion.bot.moderation;

/**
 * Repræsenterer resultatet af en moderation tjek
 * Indeholder information om hvorvidt en besked er tilladt og hvilken handling der skal tages
 */
public class ModerationResult {
    private final boolean allowed;
    private final String reason;
    private final ModerationAction action;
    private final int severity; // 1-5, hvor 5 er mest alvorligt
    private final String detectionType; // Type of detection that triggered this result
    
    private ModerationResult(boolean allowed, String reason, ModerationAction action, int severity) {
        this(allowed, reason, action, severity, "unknown");
    }
    
    private ModerationResult(boolean allowed, String reason, ModerationAction action, int severity, String detectionType) {
        this.allowed = allowed;
        this.reason = reason;
        this.action = action;
        this.severity = severity;
        this.detectionType = detectionType;
    }
    
    /**
     * Opretter et resultat der tillader beskeden
     */
    public static ModerationResult allowed() {
        return new ModerationResult(true, null, ModerationAction.NONE, 0);
    }
    
    /**
     * Opretter et resultat der blokerer beskeden med en advarsel
     */
    public static ModerationResult warn(String reason, ModerationAction action) {
        return new ModerationResult(false, reason, action, 1);
    }
    
    /**
     * Opretter et resultat der blokerer beskeden med moderat handling
     */
    public static ModerationResult moderate(String reason, ModerationAction action) {
        return new ModerationResult(false, reason, action, 3);
    }
    
    /**
     * Opretter et resultat der blokerer beskeden med streng handling
     */
    public static ModerationResult severe(String reason, ModerationAction action) {
        return new ModerationResult(false, reason, action, 4);
    }
    
    /**
     * Opretter et resultat der resulterer i ban
     */
    public static ModerationResult ban(String reason, ModerationAction action) {
        return new ModerationResult(false, reason, action, 5);
    }
    
    /**
     * Opretter et custom resultat med specificeret alvorlighed
     */
    public static ModerationResult custom(boolean allowed, String reason, ModerationAction action, int severity) {
        return new ModerationResult(allowed, reason, action, severity);
    }
    
    /**
     * Opretter et custom resultat med specificeret alvorlighed og detection type
     */
    public static ModerationResult custom(boolean allowed, String reason, ModerationAction action, int severity, String detectionType) {
        return new ModerationResult(allowed, reason, action, severity, detectionType);
    }
    
    /**
     * Opretter et violation resultat med specificeret grund, handling og alvorlighed
     */
    public static ModerationResult violation(String reason, ModerationAction action, ModerationSeverity severity) {
        return new ModerationResult(false, reason, action, severity.getLevel(), "violation");
    }
    
    // Getters
    public boolean isAllowed() {
        return allowed;
    }
    
    public String getReason() {
        return reason;
    }
    
    public ModerationAction getAction() {
        return action;
    }
    
    public int getSeverity() {
        return severity;
    }
    
    public String getDetectionType() {
        return detectionType;
    }
    
    /**
     * Tjekker om resultatet kræver øjeblikkelig handling
     */
    public boolean requiresImmediateAction() {
        return !allowed && (severity >= 4 || action == ModerationAction.BAN);
    }
    
    /**
     * Tjekker om resultatet er en advarsel
     */
    public boolean isWarning() {
        return !allowed && severity <= 2;
    }
    
    /**
     * Tjekker om resultatet er alvorligt
     */
    public boolean isSevere() {
        return !allowed && severity >= 4;
    }
    
    /**
     * Får en brugervenlig beskrivelse af resultatet
     */
    public String getDisplayMessage() {
        if (allowed) {
            return "Besked tilladt";
        }
        
        String severityText;
        switch (severity) {
            case 1:
                severityText = "⚠️ Advarsel";
                break;
            case 2:
                severityText = "⚠️ Let overtrædelse";
                break;
            case 3:
                severityText = "🚨 Moderat overtrædelse";
                break;
            case 4:
                severityText = "🚨 Alvorlig overtrædelse";
                break;
            case 5:
                severityText = "🔴 Kritisk overtrædelse";
                break;
            default:
                severityText = "❓ Ukendt";
                break;
        }
        
        return severityText + ": " + reason;
    }
    
    /**
     * Får emoji der repræsenterer alvorligheden
     */
    public String getSeverityEmoji() {
        switch (severity) {
            case 1:
            case 2:
                return "⚠️";
            case 3:
            case 4:
                return "🚨";
            case 5:
                return "🔴";
            default:
                return "❓";
        }
    }
    
    /**
     * Kombinerer dette resultat med et andet og returnerer det mest alvorlige
     */
    public ModerationResult combineWith(ModerationResult other) {
        if (this.allowed && other.allowed) {
            return allowed();
        }
        
        if (this.allowed) {
            return other;
        }
        
        if (other.allowed) {
            return this;
        }
        
        // Begge er ikke tilladt, returner den mest alvorlige
        if (this.severity >= other.severity) {
            return this;
        } else {
            return other;
        }
    }
    
    @Override
    public String toString() {
        return "ModerationResult{" +
                "allowed=" + allowed +
                ", reason='" + reason + '\'' +
                ", action=" + action +
                ", severity=" + severity +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        ModerationResult that = (ModerationResult) o;
        
        if (allowed != that.allowed) return false;
        if (severity != that.severity) return false;
        if (reason != null ? !reason.equals(that.reason) : that.reason != null) return false;
        return action == that.action;
    }
    
    @Override
    public int hashCode() {
        int result = (allowed ? 1 : 0);
        result = 31 * result + (reason != null ? reason.hashCode() : 0);
        result = 31 * result + (action != null ? action.hashCode() : 0);
        result = 31 * result + severity;
        return result;
    }
}