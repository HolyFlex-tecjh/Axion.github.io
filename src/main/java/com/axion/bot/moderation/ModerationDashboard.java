package com.axion.bot.moderation;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import java.awt.Color;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Moderation dashboard til at vise system status og statistikker
 */
public class ModerationDashboard {
    private final ModerationManager moderationManager;
    private final ModerationConfig config;
    
    public ModerationDashboard(ModerationManager moderationManager, ModerationConfig config) {
        this.moderationManager = moderationManager;
        this.config = config;
    }
    
    /**
     * Opretter hovedoversigt embed
     */
    public MessageEmbed createOverviewEmbed(String guildId) {
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("🛡️ Moderation System Dashboard")
            .setColor(Color.BLUE)
            .setTimestamp(Instant.now());
        
        ModerationStats stats = moderationManager.getModerationStats(guildId);
        
        // System status
        StringBuilder systemStatus = new StringBuilder();
        systemStatus.append(getStatusIcon(config.isSpamProtectionEnabled())).append(" Spam Beskyttelse\n");
        systemStatus.append(getStatusIcon(config.isToxicDetectionEnabled())).append(" Toksisk Indhold\n");
        systemStatus.append(getStatusIcon(config.isLinkProtectionEnabled())).append(" Link Beskyttelse\n");
        systemStatus.append(getStatusIcon(config.isAttachmentScanningEnabled())).append(" Vedhæftning Scanning\n");
        systemStatus.append(getStatusIcon(config.isAdvancedSpamDetectionEnabled())).append(" Avanceret Spam\n");
        systemStatus.append(getStatusIcon(config.isTempBanEnabled())).append(" Midlertidige Bans");
        
        embed.addField("📊 System Status", systemStatus.toString(), true);
        
        // Statistikker
        StringBuilder statsText = new StringBuilder();
        statsText.append("👥 **Sporede Brugere:** ").append(stats.getTotalTrackedUsers()).append("\n");
        statsText.append("⚠️ **Aktive Overtrædelser:** ").append(stats.getActiveViolations()).append("\n");
        statsText.append("🚫 **Aktive Temp Bans:** ").append(stats.getActiveTempBans()).append("\n");
        statsText.append("📈 **Total Handlinger:** ").append(stats.getTotalModerationActions()).append("\n");
        statsText.append("📊 **Overtrædelsesrate:** ").append(String.format("%.1f%%", stats.getViolationRate())).append("\n");
        statsText.append("🔒 **Temp Ban Rate:** ").append(String.format("%.1f%%", stats.getTempBanRate()));
        
        embed.addField("📈 Statistikker", statsText.toString(), true);
        
        // Konfiguration
        StringBuilder configText = new StringBuilder();
        configText.append("📨 **Max Beskeder/Min:** ").append(config.getMaxMessagesPerMinute()).append("\n");
        configText.append("⚠️ **Max Advarsler:** ").append(config.getMaxWarningsBeforeBan()).append("\n");
        configText.append("🔗 **Max Links:** ").append(config.getMaxLinksPerMessage()).append("\n");
        configText.append("⏰ **Max Temp Ban:** ").append(config.getMaxTempBanHours()).append(" timer\n");
        configText.append("🔄 **Overtrædelse Forfald:** ").append(config.getViolationDecayHours()).append(" timer");
        
        embed.addField("⚙️ Konfiguration", configText.toString(), true);
        
        return embed.build();
    }
    
    /**
     * Opretter detaljeret statistik embed
     */
    public MessageEmbed createDetailedStatsEmbed(String guildId) {
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("📊 Detaljerede Moderation Statistikker")
            .setColor(Color.GREEN)
            .setTimestamp(Instant.now());
        
        ModerationStats stats = moderationManager.getModerationStats(guildId);
        
        // Bruger statistikker
        embed.addField("👥 Bruger Statistikker", 
            String.format("**Total Sporede:** %d\n**Med Overtrædelser:** %d\n**Overtrædelsesrate:** %.1f%%",
                stats.getTotalTrackedUsers(),
                stats.getActiveViolations(),
                stats.getViolationRate()), true);
        
        // Temp ban statistikker
        embed.addField("🚫 Temp Ban Statistikker", 
            String.format("**Aktive Bans:** %d\n**Ban Rate:** %.1f%%",
                stats.getActiveTempBans(),
                stats.getTempBanRate()), true);
        
        // System effektivitet
        double efficiency = stats.getTotalModerationActions() > 0 ? 
            (double) stats.getActiveViolations() / stats.getTotalModerationActions() * 100 : 0;
        
        embed.addField("⚡ System Effektivitet", 
            String.format("**Total Handlinger:** %d\n**Effektivitetsrate:** %.1f%%",
                stats.getTotalModerationActions(),
                efficiency), true);
        
        return embed.build();
    }
    
    /**
     * Opretter aktive temp bans embed
     */
    public MessageEmbed createActiveTempBansEmbed() {
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("🚫 Aktive Midlertidige Bans")
            .setColor(Color.RED)
            .setTimestamp(Instant.now());
        
        Map<String, Instant> activeBans = moderationManager.getActiveTempBans();
        
        if (activeBans.isEmpty()) {
            embed.setDescription("✅ Ingen aktive midlertidige bans");
        } else {
            StringBuilder bansText = new StringBuilder();
            int count = 0;
            
            for (Map.Entry<String, Instant> entry : activeBans.entrySet()) {
                if (count >= 10) {
                    bansText.append("\n... og ").append(activeBans.size() - 10).append(" flere");
                    break;
                }
                
                String userId = entry.getKey();
                Instant expiry = entry.getValue();
                
                bansText.append("👤 <@").append(userId).append(">\n");
                bansText.append("⏰ Udløber: <t:").append(expiry.getEpochSecond()).append(":R>\n\n");
                count++;
            }
            
            embed.setDescription(bansText.toString());
        }
        
        return embed.build();
    }
    
    /**
     * Opretter seneste moderation logs embed
     */
    public MessageEmbed createRecentLogsEmbed(int limit) {
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("📋 Seneste Moderation Handlinger")
            .setColor(Color.ORANGE)
            .setTimestamp(Instant.now());
        
        List<ModerationLog> recentLogs = moderationManager.getRecentModerationLogs(limit);
        
        if (recentLogs.isEmpty()) {
            embed.setDescription("📭 Ingen seneste moderation handlinger");
        } else {
            StringBuilder logsText = new StringBuilder();
            
            for (ModerationLog log : recentLogs) {
                logsText.append("**").append(log.getShortDescription()).append("**\n");
                logsText.append("👤 ").append(log.getUsername()).append("\n");
                logsText.append("⏰ <t:").append(log.getTimestamp().getEpochSecond()).append(":R>\n\n");
            }
            
            embed.setDescription(logsText.toString());
        }
        
        return embed.build();
    }
    
    /**
     * Opretter system sundhed embed
     */
    public MessageEmbed createSystemHealthEmbed(String guildId) {
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("💚 System Sundhed")
            .setTimestamp(Instant.now());
        
        ModerationStats stats = moderationManager.getModerationStats(guildId);
        
        // Beregn system sundhed baseret på statistikker
        double healthScore = calculateHealthScore(stats);
        Color healthColor = getHealthColor(healthScore);
        String healthStatus = getHealthStatus(healthScore);
        
        embed.setColor(healthColor);
        embed.setDescription(String.format("**System Status:** %s\n**Sundhedsscore:** %.1f/100", 
            healthStatus, healthScore));
        
        // Sundhedsindikatorer
        StringBuilder indicators = new StringBuilder();
        indicators.append(getHealthIndicator("Overtrædelsesrate", stats.getViolationRate(), 10.0, false));
        indicators.append(getHealthIndicator("Temp Ban Rate", stats.getTempBanRate(), 5.0, false));
        indicators.append(getHealthIndicator("Aktive Brugere", stats.getTotalTrackedUsers(), 50, true));
        
        embed.addField("📊 Sundhedsindikatorer", indicators.toString(), false);
        
        // Anbefalinger
        StringBuilder recommendations = new StringBuilder();
        if (stats.getViolationRate() > 15.0) {
            recommendations.append("⚠️ Høj overtrædelsesrate - overvej strengere regler\n");
        }
        if (stats.getTempBanRate() > 8.0) {
            recommendations.append("🚫 Høj temp ban rate - tjek for problematiske brugere\n");
        }
        if (stats.getTotalTrackedUsers() < 10) {
            recommendations.append("👥 Få sporede brugere - systemet er måske ikke aktivt nok\n");
        }
        if (recommendations.length() == 0) {
            recommendations.append("✅ Systemet kører optimalt!");
        }
        
        embed.addField("💡 Anbefalinger", recommendations.toString(), false);
        
        return embed.build();
    }
    
    private String getStatusIcon(boolean enabled) {
        return enabled ? "✅" : "❌";
    }
    
    private double calculateHealthScore(ModerationStats stats) {
        double score = 100.0;
        
        // Træk point for høje rates
        if (stats.getViolationRate() > 10.0) {
            score -= Math.min(30, (stats.getViolationRate() - 10.0) * 2);
        }
        if (stats.getTempBanRate() > 5.0) {
            score -= Math.min(20, (stats.getTempBanRate() - 5.0) * 3);
        }
        
        // Træk point for lav aktivitet
        if (stats.getTotalTrackedUsers() < 10) {
            score -= 20;
        }
        
        return Math.max(0, score);
    }
    
    private Color getHealthColor(double score) {
        if (score >= 80) return Color.GREEN;
        if (score >= 60) return Color.YELLOW;
        if (score >= 40) return Color.ORANGE;
        return Color.RED;
    }
    
    private String getHealthStatus(double score) {
        if (score >= 80) return "🟢 Fremragende";
        if (score >= 60) return "🟡 God";
        if (score >= 40) return "🟠 Acceptabel";
        return "🔴 Kræver Opmærksomhed";
    }
    
    private String getHealthIndicator(String name, double value, double threshold, boolean higherIsBetter) {
        boolean isGood = higherIsBetter ? value >= threshold : value <= threshold;
        String icon = isGood ? "✅" : "⚠️";
        return String.format("%s **%s:** %.1f\n", icon, name, value);
    }
}