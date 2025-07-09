package com.axion.bot.moderation;

/**
 * Enum der definerer forskellige moderation handlinger
 * Bruges til at specificere hvilken handling der skal tages mod en bruger
 */
public enum ModerationAction {
    /**
     * Ingen handling - beskeden er tilladt
     */
    NONE("Ingen handling", 0),
    
    /**
     * Slet beskeden
     */
    DELETE_MESSAGE("Slet besked", 1),
    
    /**
     * Send en advarsel til brugeren
     */
    WARN_USER("Advar bruger", 2),
    
    /**
     * Giv brugeren en timeout (mute)
     */
    TIMEOUT("Timeout bruger", 3),
    
    /**
     * Kick brugeren fra serveren
     */
    KICK("Kick bruger", 4),
    
    /**
     * Ban brugeren fra serveren
     */
    BAN("Ban bruger", 5),
    
    /**
     * Slet besked og advar bruger
     */
    DELETE_AND_WARN("Slet besked og advar", 2),
    
    /**
     * Slet besked og giv timeout
     */
    DELETE_AND_TIMEOUT("Slet besked og timeout", 3),
    
    /**
     * Log handlingen uden at tage action
     */
    LOG_ONLY("Log kun", 0),
    
    /**
     * System handling - automatisk handling udført af systemet
     */
<<<<<<< HEAD
    SYSTEM_ACTION("System handling", 0);
=======
    SYSTEM_ACTION("System handling", 0),
    
    /**
     * Flag user for manual review
     */
    FLAG_FOR_REVIEW("Flag for review", 1),
    
    /**
     * Temporary ban from server
     */
    TEMP_BAN("Temporary ban", 4);
>>>>>>> 7264671782849e6cd81d554807906b664cb5d408
    
    private final String description;
    private final int severity;
    
    ModerationAction(String description, int severity) {
        this.description = description;
        this.severity = severity;
    }
    
    /**
     * Får beskrivelsen af handlingen
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Får alvorligheden af handlingen (0-5)
     */
    public int getSeverity() {
        return severity;
    }
    
    /**
     * Tjekker om handlingen involverer at slette beskeden
     */
    public boolean involvesMessageDeletion() {
        return this == DELETE_MESSAGE || 
               this == DELETE_AND_WARN || 
               this == DELETE_AND_TIMEOUT;
    }
    
    /**
     * Tjekker om handlingen involverer bruger disciplin
     */
    public boolean involvesUserDiscipline() {
        return this == WARN_USER || 
               this == TIMEOUT || 
               this == KICK || 
               this == BAN ||
               this == TEMP_BAN ||
               this == DELETE_AND_WARN ||
               this == DELETE_AND_TIMEOUT;
    }
    
    /**
     * Tjekker om handlingen er permanent
     */
    public boolean isPermanent() {
        return this == BAN;
    }
    
    /**
     * Tjekker om handlingen fjerner brugeren fra serveren
     */
    public boolean removesFromServer() {
        return this == KICK || this == BAN || this == TEMP_BAN;
    }
    
    /**
     * Får emoji der repræsenterer handlingen
     */
    public String getEmoji() {
        switch (this) {
            case NONE:
            case LOG_ONLY:
                return "✅";
            case DELETE_MESSAGE:
                return "🗑️";
            case WARN_USER:
            case DELETE_AND_WARN:
                return "⚠️";
            case TIMEOUT:
            case DELETE_AND_TIMEOUT:
                return "🔇";
            case KICK:
                return "👢";
            case BAN:
            case TEMP_BAN:
                return "🔨";
            case SYSTEM_ACTION:
                return "🤖";
<<<<<<< HEAD
=======
            case FLAG_FOR_REVIEW:
                return "🚩";
>>>>>>> 7264671782849e6cd81d554807906b664cb5d408
            default:
                return "❓";
        }
    }
    
    /**
     * Får en brugervenlig besked for handlingen
     */
    public String getUserMessage() {
        switch (this) {
            case DELETE_MESSAGE:
                return "Din besked blev slettet på grund af overtrædelse af serverreglerne.";
            case WARN_USER:
                return "Du har modtaget en advarsel for overtrædelse af serverreglerne.";
            case TIMEOUT:
                return "Du er blevet givet en timeout på grund af overtrædelse af serverreglerne.";
            case KICK:
                return "Du er blevet kicket fra serveren på grund af overtrædelse af serverreglerne.";
            case BAN:
                return "Du er blevet bannet fra serveren på grund af overtrædelse af serverreglerne.";
            case TEMP_BAN:
                return "Du er blevet midlertidigt bannet fra serveren på grund af overtrædelse af serverreglerne.";
            case DELETE_AND_WARN:
                return "Din besked blev slettet og du har modtaget en advarsel.";
            case DELETE_AND_TIMEOUT:
                return "Din besked blev slettet og du er blevet givet en timeout.";
            case SYSTEM_ACTION:
                return "Automatisk system handling udført.";
<<<<<<< HEAD
=======
            case FLAG_FOR_REVIEW:
                return "Din aktivitet er blevet flagget til manuel gennemgang.";
>>>>>>> 7264671782849e6cd81d554807906b664cb5d408
            default:
                return "Moderation handling udført.";
        }
    }
    
    /**
     * Får en admin besked for handlingen
     */
    public String getAdminMessage(String username, String reason) {
        String emoji = getEmoji();
        switch (this) {
            case DELETE_MESSAGE:
                return emoji + " Slettet besked fra **" + username + "**: " + reason;
            case WARN_USER:
                return emoji + " Advaret **" + username + "**: " + reason;
            case TIMEOUT:
                return emoji + " Timeout givet til **" + username + "**: " + reason;
            case KICK:
                return emoji + " Kicket **" + username + "**: " + reason;
            case BAN:
                return emoji + " Bannet **" + username + "**: " + reason;
            case TEMP_BAN:
                return emoji + " Midlertidigt bannet **" + username + "**: " + reason;
            case DELETE_AND_WARN:
                return emoji + " Slettet besked og advaret **" + username + "**: " + reason;
            case DELETE_AND_TIMEOUT:
                return emoji + " Slettet besked og givet timeout til **" + username + "**: " + reason;
            case LOG_ONLY:
                return "📝 Loggede handling for **" + username + "**: " + reason;
            case SYSTEM_ACTION:
                return emoji + " Automatisk system handling for **" + username + "**: " + reason;
<<<<<<< HEAD
=======
            case FLAG_FOR_REVIEW:
                return emoji + " Flagget **" + username + "** til manuel gennemgang: " + reason;
>>>>>>> 7264671782849e6cd81d554807906b664cb5d408
            default:
                return emoji + " Moderation handling udført på **" + username + "**: " + reason;
        }
    }
    
    /**
     * Konverterer fra alvorlighed til passende handling
     */
    public static ModerationAction fromSeverity(int severity) {
        switch (severity) {
            case 0:
                return NONE;
            case 1:
                return DELETE_MESSAGE;
            case 2:
                return DELETE_AND_WARN;
            case 3:
                return DELETE_AND_TIMEOUT;
            case 4:
                return KICK;
            case 5:
                return BAN;
            default:
                return DELETE_MESSAGE;
        }
    }
    
    /**
     * Eskalerer handlingen til næste niveau
     */
    public ModerationAction escalate() {
        switch (this) {
            case NONE:
            case LOG_ONLY:
                return DELETE_MESSAGE;
            case DELETE_MESSAGE:
                return DELETE_AND_WARN;
            case WARN_USER:
            case DELETE_AND_WARN:
                return DELETE_AND_TIMEOUT;
            case TIMEOUT:
            case DELETE_AND_TIMEOUT:
                return KICK;
            case KICK:
                return BAN;
            case BAN:
                return BAN; // Kan ikke eskalere højere
            default:
                return this;
        }
    }
}