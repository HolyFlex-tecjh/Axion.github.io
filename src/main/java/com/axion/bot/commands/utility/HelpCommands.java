package com.axion.bot.commands.utility;

import com.axion.bot.config.BotConfig;
import com.axion.bot.translation.TranslationManager;
import com.axion.bot.translation.UserLanguageManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import java.lang.management.ManagementFactory;

/**
 * Håndterer hjælpe kommandoer
 */
public class HelpCommands {
    private static final TranslationManager translationManager = TranslationManager.getInstance();
    private static final UserLanguageManager userLanguageManager = UserLanguageManager.getInstance();

    /**
     * Help kommando - viser en interaktiv menu med kommando kategorier
     */
    public static void handleHelp(SlashCommandInteractionEvent event) {
        String userLang = userLanguageManager.getUserLanguage(event.getUser().getId());
        // Get bot statistics
        long ping = event.getJDA().getGatewayPing();
        Runtime runtime = Runtime.getRuntime();
        long memoryUsed = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
        
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🚀 " + translationManager.translate("help.title", userLang))
                .setDescription("📋 **" + translationManager.translate("help.interactive_menu", userLang) + "**\n" + translationManager.translate("help.description", userLang, BotConfig.BOT_NAME) + "\n\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .setColor(0x00D4FF)
                .setThumbnail(event.getJDA().getSelfUser().getAvatarUrl())
                
                .addField("📚 **" + translationManager.translate("help.how_to_use", userLang) + "**", 
                    "```md\n# " + translationManager.translate("help.select_category", userLang) + "\n# " + translationManager.translate("help.view_commands", userLang) + "\n```", false)
                
                .addField("📊 **" + translationManager.translate("help.available_categories", userLang) + "**", 
                    "```yaml\n📚 " + translationManager.translate("help.category.basic", userLang) + "\n🛡️ " + translationManager.translate("help.category.moderation", userLang) + "\n⚙️ " + translationManager.translate("help.category.utility", userLang) + "\n🎮 " + translationManager.translate("help.category.fun", userLang) + "\n🔧 " + translationManager.translate("help.category.config", userLang) + "\n```", false)
                
                .addField("📊 **" + translationManager.translate("help.bot_status", userLang) + "**", 
                    "```yaml\n🏓 " + translationManager.translate("help.ping", userLang) + ": " + ping + "ms\n💾 " + translationManager.translate("help.memory", userLang) + ": " + memoryUsed + "MB\n⏰ " + translationManager.translate("help.uptime", userLang) + ": " + getUptimeString() + "\n```", true)
                
                .addField("❓ **" + translationManager.translate("help.quick_links", userLang) + "**", 
                    "```md\n# /modhelp - " + translationManager.translate("help.modhelp_desc", userLang) + "\n# /support - " + translationManager.translate("help.support_desc", userLang) + "\n# /invite - " + translationManager.translate("help.invite_desc", userLang) + "\n```", true)
                
                .setFooter("🔗 " + translationManager.translate("help.footer", userLang, BotConfig.BOT_VERSION) + " | " + translationManager.translate("help.select_category_below", userLang), event.getJDA().getSelfUser().getAvatarUrl())
                .setTimestamp(java.time.Instant.now());

        // Create dropdown menu
        StringSelectMenu menu = StringSelectMenu.create("help-menu")
                .setPlaceholder("📋 " + translationManager.translate("help.select_placeholder", userLang))
                .addOption("📚 " + translationManager.translate("help.category.basic", userLang), "basic", translationManager.translate("help.category.basic_desc", userLang))
                .addOption("🛡️ " + translationManager.translate("help.category.moderation", userLang), "moderation", translationManager.translate("help.category.moderation_desc", userLang))
                .addOption("⚙️ " + translationManager.translate("help.category.utility", userLang), "utility", translationManager.translate("help.category.utility_desc", userLang))
                .addOption("🎮 " + translationManager.translate("help.category.fun", userLang), "fun", translationManager.translate("help.category.fun_desc", userLang))
                .addOption("🔧 " + translationManager.translate("help.category.config", userLang), "config", translationManager.translate("help.category.config_desc", userLang))
                .addOption("📊 " + translationManager.translate("help.category.overview", userLang), "overview", translationManager.translate("help.category.overview_desc", userLang))
                .build();

        event.replyEmbeds(embed.build())
                .addActionRow(menu)
                .queue();
    }

    /**
     * ModHelp kommando - viser moderation kommandoer
     */
    public static void handleModHelp(SlashCommandInteractionEvent event) {
        String userLang = userLanguageManager.getUserLanguage(event.getUser().getId());
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("" + translationManager.translate("help.modhelp.title", userLang))
                .setColor(0xFF4757)
                .setDescription("" + translationManager.translate("help.modhelp.description", userLang, event.getGuild().getName()) + "\n\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .setThumbnail(event.getJDA().getSelfUser().getAvatarUrl())
                
                .addField("🔨 **" + translationManager.translate("help.modhelp.user_commands", userLang) + "**", 
                    "```yaml\n🔨 /ban - " + translationManager.translate("help.modhelp.ban_desc", userLang) + "\n👢 /kick - " + translationManager.translate("help.modhelp.kick_desc", userLang) + "\n🔇 /mute - " + translationManager.translate("help.modhelp.mute_desc", userLang) + "\n🧹 /clear - " + translationManager.translate("help.modhelp.clear_desc", userLang) + "\n```", false)
                
                .addField("⚠️ **" + translationManager.translate("help.modhelp.warning_system", userLang) + "**", 
                    "```yaml\n⚠️ /warn - " + translationManager.translate("help.modhelp.warn_desc", userLang) + "\n📋 /warnings - " + translationManager.translate("help.modhelp.warnings_desc", userLang) + "\n🗑️ /unwarn - " + translationManager.translate("help.modhelp.unwarn_desc", userLang) + "\n📊 /modlog - " + translationManager.translate("help.modhelp.modlog_desc", userLang) + "\n```", false)
                
                .addField("🔧 **" + translationManager.translate("help.modhelp.channel_management", userLang) + "**", 
                    "```css\n🔒 " + translationManager.translate("help.modhelp.channel_lock", userLang) + "\n🧹 " + translationManager.translate("help.modhelp.bulk_delete", userLang) + "\n📌 " + translationManager.translate("help.modhelp.message_pin", userLang) + "\n⚙️ " + translationManager.translate("help.modhelp.channel_config", userLang) + "\n```", false)
                
                .addField("📊 **" + translationManager.translate("help.modhelp.config_stats", userLang) + "**", 
                    "```json\n{\n  \"modlog\": \"" + translationManager.translate("help.modhelp.config_modlog", userLang) + "\",\n  \"automod\": \"" + translationManager.translate("help.modhelp.config_automod", userLang) + "\",\n  \"roles\": \"" + translationManager.translate("help.modhelp.config_roles", userLang) + "\"\n}\n```", false)
                
                .addField("🚫 **" + translationManager.translate("help.modhelp.auto_moderation", userLang) + "**", 
                    "```md\n# 🚫 " + translationManager.translate("help.modhelp.spam_protection", userLang) + "\n# 🔗 " + translationManager.translate("help.modhelp.link_filter", userLang) + "\n# 🤬 " + translationManager.translate("help.modhelp.profanity_filter", userLang) + "\n# 🔁 " + translationManager.translate("help.modhelp.anti_raid", userLang) + "\n```", false)
                
                .addBlankField(false)
                
                .addField("⚖️ **" + translationManager.translate("help.modhelp.permissions", userLang) + "**", 
                    "```diff\n- ⚠️ " + translationManager.translate("help.modhelp.requires_perms", userLang) + "\n- 🛡️ " + translationManager.translate("help.modhelp.admin_mod_role", userLang) + "\n- 📋 " + translationManager.translate("help.modhelp.specific_perms", userLang) + "\n```", false)
                
                .addField("💡 **" + translationManager.translate("help.modhelp.pro_tips", userLang) + "**", 
                    "```yaml\n💡 " + translationManager.translate("help.modhelp.tip_reason", userLang) + "\n⏰ " + translationManager.translate("help.modhelp.tip_temp", userLang) + "\n📊 " + translationManager.translate("help.modhelp.tip_modlog", userLang) + "\n```", false)
                
                .setFooter("" + translationManager.translate("help.modhelp.footer", userLang), event.getJDA().getSelfUser().getAvatarUrl())
                .setTimestamp(java.time.Instant.now());
        
        event.replyEmbeds(embed.build()).queue();
    }

    /**
     * Invite kommando - viser invite link
     */
    public static void handleInvite(SlashCommandInteractionEvent event) {
        String userLang = userLanguageManager.getUserLanguage(event.getUser().getId());
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("" + translationManager.translate("help.invite.title", userLang, BotConfig.BOT_NAME))
                .setDescription("✨ " + translationManager.translate("help.invite.description", userLang) + "\n\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .setColor(0x7289DA)
                .setThumbnail(event.getJDA().getSelfUser().getAvatarUrl())
                
                .addField("🔗 **" + translationManager.translate("help.invite.bot_link", userLang) + "**", 
                    "\n🔗 [" + translationManager.translate("help.invite.click_to_invite", userLang) + "](" + BotConfig.INVITE_URL + ")\n", false)
                
                .addField("🔐 **" + translationManager.translate("help.invite.permissions", userLang) + "**", 
                    "```yaml\n🔐 " + translationManager.translate("help.invite.admin_perms", userLang) + "\n⚙️ " + translationManager.translate("help.invite.manage_perms", userLang) + "\n🛡️ " + translationManager.translate("help.invite.mod_capabilities", userLang) + "\n```", false)
                
                .addField("✨ **" + translationManager.translate("help.invite.features", userLang) + "**", 
                    "```md\n# ✨ " + translationManager.translate("help.invite.feature_moderation", userLang) + "\n# 🤖 " + translationManager.translate("help.invite.feature_automod", userLang) + "\n# 📊 " + translationManager.translate("help.invite.feature_stats", userLang) + "\n# 🌍 " + translationManager.translate("help.invite.feature_multilang", userLang) + "\n```", false)
                
                .addField("📝 **" + translationManager.translate("help.invite.setup_guide", userLang) + "**", 
                    "```diff\n+ 1. " + translationManager.translate("help.invite.step1", userLang) + "\n+ 2. " + translationManager.translate("help.invite.step2", userLang) + "\n+ 3. " + translationManager.translate("help.invite.step3", userLang) + "\n+ 4. " + translationManager.translate("help.invite.step4", userLang) + "\n```", false)
                
                .addBlankField(false)
                
                .setFooter("💫 " + translationManager.translate("help.invite.footer", userLang), event.getJDA().getSelfUser().getAvatarUrl())
                .setTimestamp(java.time.Instant.now());
        
        event.replyEmbeds(embed.build()).queue();
    }

    /**
     * Support kommando - viser support server info
     */
    public static void handleSupport(SlashCommandInteractionEvent event) {
        String userLang = userLanguageManager.getUserLanguage(event.getUser().getId());
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("" + translationManager.translate("help.support.title", userLang))
                .setDescription("" + translationManager.translate("help.support.description", userLang) + "\n\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .setColor(0x43B581)
                .setThumbnail(event.getJDA().getSelfUser().getAvatarUrl())
                
                .addField("🏠 **" + translationManager.translate("help.support.server", userLang) + "**", 
                    "\n🔗 [" + translationManager.translate("help.support.join_server", userLang) + "](" + BotConfig.SUPPORT_SERVER + ")\n💬 " + translationManager.translate("help.support.get_help", userLang) + "\n", false)
                
                .addField("🐛 **" + translationManager.translate("help.support.bugs", userLang) + "**", 
                    "```md\n# " + translationManager.translate("help.support.found_bug", userLang) + "\n🔗 [GitHub](" + BotConfig.GITHUB_URL + ")\n```", false)
                
                .addField("💡 **" + translationManager.translate("help.support.features", userLang) + "**", 
                    "```yaml\n💭 " + translationManager.translate("help.support.feature_ideas", userLang) + "\n🗣️ " + translationManager.translate("help.support.share_ideas", userLang) + "\n```", false)
                
                .addField("📖 **" + translationManager.translate("help.support.documentation", userLang) + "**", 
                    "\n🌐 [" + translationManager.translate("help.support.website", userLang) + "](" + BotConfig.WEBSITE_URL + ")\n📁 [GitHub](" + BotConfig.GITHUB_URL + ")\n", false)
                
                .addField("❓ **" + translationManager.translate("help.support.faq", userLang) + "**", 
                    "```yaml\n❓ " + translationManager.translate("help.support.faq_desc", userLang) + "\n📖 " + translationManager.translate("help.support.quick_answers", userLang) + "\n```", false)
                
                .addBlankField(false)
                
                .setFooter("" + translationManager.translate("help.support.footer", userLang), event.getJDA().getSelfUser().getAvatarUrl())
                .setTimestamp(java.time.Instant.now());
        
        event.replyEmbeds(embed.build()).queue();
    }

    /**
     * About kommando - detaljeret information om botten
     */
    public static void handleAbout(SlashCommandInteractionEvent event) {
        String userLang = userLanguageManager.getUserLanguage(event.getUser().getId());
        // Gather comprehensive statistics
        long totalGuilds = event.getJDA().getGuilds().size();
        long totalUsers = event.getJDA().getGuilds().stream()
                .mapToLong(guild -> guild.getMemberCount())
                .sum();
        long ping = event.getJDA().getGatewayPing();
        Runtime runtime = Runtime.getRuntime();
        long memoryUsed = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
        long memoryTotal = runtime.totalMemory() / 1024 / 1024;
        
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("" + translationManager.translate("help.about.title", userLang, BotConfig.BOT_NAME))
                .setDescription("" + translationManager.translate("help.about.description", userLang, BotConfig.BOT_DESCRIPTION) + "\n\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .setColor(0x9B59B6)
                .setThumbnail(event.getJDA().getSelfUser().getAvatarUrl())
                
                .addField("📊 **" + translationManager.translate("help.about.live_stats", userLang) + "**", 
                    "```yaml\n🏠 " + translationManager.translate("help.about.servers", userLang) + ": " + String.format("%,d", totalGuilds) + 
                    "\n👥 " + translationManager.translate("help.about.users", userLang) + ": " + String.format("%,d", totalUsers) +
                    "\n🏓 " + translationManager.translate("help.about.ping", userLang) + ": " + ping + "ms" +
                    "\n🚀 " + translationManager.translate("help.about.version", userLang) + ": " + BotConfig.BOT_VERSION +
                    "\n⏰ " + translationManager.translate("help.about.uptime", userLang) + ": " + getUptimeString() + "\n```", true)
                
                .addField("💻 **" + translationManager.translate("help.about.system_info", userLang) + "**", 
                    "```yaml\n💾 " + translationManager.translate("help.about.memory", userLang) + ": " + memoryUsed + "/" + memoryTotal + "MB" +
                    "\n☕ Java: " + System.getProperty("java.version") +
                    "\n🖥️ OS: " + System.getProperty("os.name") +
                    "\n🔧 JDA: 5.x\n```", true)
                
                .addBlankField(false)
                
                .addField("🔧 **" + translationManager.translate("help.about.tech_stack", userLang) + "**", 
                    "```json\n{\n  \"language\": \"Java\",\n  \"framework\": \"JDA 5.x\",\n  \"database\": \"SQLite/MySQL\",\n  \"hosting\": \"VPS/Cloud\"\n}\n```", false)
                
                .addField("✨ **" + translationManager.translate("help.about.main_features", userLang) + "**", 
                    "```md\n# 🛡️ " + translationManager.translate("help.about.feature_moderation", userLang) + "\n# 🤖 " + translationManager.translate("help.about.feature_automod", userLang) + "\n# 📊 " + translationManager.translate("help.about.feature_stats", userLang) + "\n# 🌍 " + translationManager.translate("help.about.feature_multilang", userLang) + "\n# ⚙️ " + translationManager.translate("help.about.feature_settings", userLang) + "\n```", false)
                
                .addField("🔗 **" + translationManager.translate("help.about.important_links", userLang) + "**", 
                    "\n🔗 [" + translationManager.translate("help.about.invite_bot", userLang) + "](" + BotConfig.INVITE_URL + ")\n" +
                    "🏠 [" + translationManager.translate("help.about.support_server", userLang) + "](" + BotConfig.SUPPORT_SERVER + ")\n" +
                    "📁 [" + translationManager.translate("help.about.github_repo", userLang) + "](" + BotConfig.GITHUB_URL + ")\n" +
                    "🌐 [" + translationManager.translate("help.about.website", userLang) + "](" + BotConfig.WEBSITE_URL + ")\n", false)
                
                .addField("🤝 **" + translationManager.translate("help.about.support_community", userLang) + "**", 
                    "```css\n💖 " + translationManager.translate("help.about.join_community", userLang) + "\n🤝 " + translationManager.translate("help.about.get_support", userLang) + "\n🎉 " + translationManager.translate("help.about.events", userLang) + "\n```", false)
                
                .addField("⭐ **" + translationManager.translate("help.about.why_choose", userLang, BotConfig.BOT_NAME) + "**", 
                    "```diff\n+ ✅ " + translationManager.translate("help.about.reliable", userLang) + "\n+ ✅ " + translationManager.translate("help.about.regular_updates", userLang) + "\n+ ✅ " + translationManager.translate("help.about.active_support", userLang) + "\n+ ✅ " + translationManager.translate("help.about.open_source", userLang) + "\n```", false)
                
                .setFooter("" + translationManager.translate("help.about.footer", userLang, BotConfig.BOT_NAME), event.getJDA().getSelfUser().getAvatarUrl())
                .setTimestamp(java.time.Instant.now());
        
        event.replyEmbeds(embed.build()).queue();
    }

    /**
     * Håndterer selection menu interactions for help kommandoer
     */
    public static EmbedBuilder getBasicCommandsEmbed(String userLang) {
        return new EmbedBuilder()
                .setTitle("📚 " + translationManager.translate("help.basic.title", userLang))
                .setDescription(translationManager.translate("help.basic.description", userLang) + "\n\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .setColor(0x00D4FF)
                .addField("📚 **" + translationManager.translate("help.basic.commands", userLang) + "**", 
                    "```yaml\n🔹 /help - " + translationManager.translate("help.basic.help_desc", userLang) +
                    "\n🔹 /ping - " + translationManager.translate("help.basic.ping_desc", userLang) +
                    "\n🔹 /info - " + translationManager.translate("help.basic.info_desc", userLang) +
                    "\n🔹 /about - " + translationManager.translate("help.basic.about_desc", userLang) +
                    "\n🔹 /invite - " + translationManager.translate("help.basic.invite_desc", userLang) +
                    "\n🔹 /support - " + translationManager.translate("help.basic.support_desc", userLang) +
                    "\n🔹 /uptime - " + translationManager.translate("help.basic.uptime_desc", userLang) + "\n```", false)
                .setFooter(translationManager.translate("help.basic.footer", userLang))
                .setTimestamp(java.time.Instant.now());
    }

    public static EmbedBuilder getModerationCommandsEmbed(String userLang) {
        return new EmbedBuilder()
                .setTitle("🛡️ " + translationManager.translate("help.moderation.title", userLang))
                .setDescription(translationManager.translate("help.moderation.description", userLang) + "\n\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .setColor(0xFF4757)
                .addField("🔨 **" + translationManager.translate("help.moderation.user_management", userLang) + "**", 
                    "```yaml\n🔨 /ban - " + translationManager.translate("help.moderation.ban_desc", userLang) +
                    "\n⏰ /tempban - " + translationManager.translate("help.moderation.tempban_desc", userLang) +
                    "\n🔓 /unban - " + translationManager.translate("help.moderation.unban_desc", userLang) +
                    "\n👢 /kick - " + translationManager.translate("help.moderation.kick_desc", userLang) +
                    "\n� /massban - " + translationManager.translate("help.moderation.massban_desc", userLang) +
                    "\n🏷️ /nick - " + translationManager.translate("help.moderation.nick_desc", userLang) +
                    "\n🎭 /role - " + translationManager.translate("help.moderation.role_desc", userLang) + "\n```", false)
                .addField("�🔇 **" + translationManager.translate("help.moderation.mute_system", userLang) + "**", 
                    "```yaml\n🔇 /mute - " + translationManager.translate("help.moderation.mute_desc", userLang) +
                    "\n🔊 /unmute - " + translationManager.translate("help.moderation.unmute_desc", userLang) +
                    "\n⏰ /tempmute - " + translationManager.translate("help.moderation.tempmute_desc", userLang) +
                    "\n⏱️ /timeout - " + translationManager.translate("help.moderation.timeout_desc", userLang) + "\n```", false)
                .addField("⚠️ **" + translationManager.translate("help.moderation.warning_system", userLang) + "**", 
                    "```yaml\n⚠️ /warn - " + translationManager.translate("help.moderation.warn_desc", userLang) +
                    "\n🗑️ /unwarn - " + translationManager.translate("help.moderation.unwarn_desc", userLang) +
                    "\n📋 /warnings - " + translationManager.translate("help.moderation.warnings_desc", userLang) +
                    "\n🧹 /clearwarnings - " + translationManager.translate("help.moderation.clearwarnings_desc", userLang) + "\n```", false)
                .addField("📝 **" + translationManager.translate("help.moderation.message_management", userLang) + "**", 
                    "```yaml\n🧹 /purge - " + translationManager.translate("help.moderation.purge_desc", userLang) +
                    "\n🐌 /slowmode - " + translationManager.translate("help.moderation.slowmode_desc", userLang) +
                    "\n🔒 /lock - " + translationManager.translate("help.moderation.lock_desc", userLang) +
                    "\n🔓 /unlock - " + translationManager.translate("help.moderation.unlock_desc", userLang) +
                    "\n� /lockdown - " + translationManager.translate("help.moderation.lockdown_desc", userLang) +
                    "\n🔓 /unlockdown - " + translationManager.translate("help.moderation.unlockdown_desc", userLang) + "\n```", false)
                .addField("🎙️ **" + translationManager.translate("help.moderation.voice_management", userLang) + "**", 
                    "```yaml\n🎙️ /voicekick - " + translationManager.translate("help.moderation.voicekick_desc", userLang) +
                    "\n🔇 /voiceban - " + translationManager.translate("help.moderation.voiceban_desc", userLang) +
                    "\n🔊 /voiceunban - " + translationManager.translate("help.moderation.voiceunban_desc", userLang) + "\n```", false)
                .addField("📊 **" + translationManager.translate("help.moderation.logs_stats", userLang) + "**", 
                    "```yaml\n📊 /logs - " + translationManager.translate("help.moderation.logs_desc", userLang) +
                    "\n📈 /modstats - " + translationManager.translate("help.moderation.modstats_desc", userLang) +
                    "\n📋 /logstats - " + translationManager.translate("help.moderation.logstats_desc", userLang) +
                    "\n� /exportlogs - " + translationManager.translate("help.moderation.exportlogs_desc", userLang) +
                    "\n🧹 /clearlogs - " + translationManager.translate("help.moderation.clearlogs_desc", userLang) + "\n```", false)
                .addField("⚙️ **" + translationManager.translate("help.moderation.configuration", userLang) + "**", 
                    "```yaml\n⚙️ /modconfig - " + translationManager.translate("help.moderation.modconfig_desc", userLang) +
                    "\n🤖 /automod - " + translationManager.translate("help.moderation.automod_desc", userLang) +
                    "\n�️ /addfilter - " + translationManager.translate("help.moderation.addfilter_desc", userLang) +
                    "\n📝 /setlogchannel - " + translationManager.translate("help.moderation.setlogchannel_desc", userLang) +
                    "\n🔍 /setauditchannel - " + translationManager.translate("help.moderation.setauditchannel_desc", userLang) + "\n```", false)
                .addField("⚖️ **" + translationManager.translate("help.moderation.requirements", userLang) + "**", 
                    "```diff\n- " + translationManager.translate("help.moderation.req_permissions", userLang) +
                    "\n- " + translationManager.translate("help.moderation.req_admin", userLang) +
                    "\n- " + translationManager.translate("help.moderation.req_discord", userLang) + "\n```", false)
                .setFooter(translationManager.translate("help.moderation.footer", userLang))
                .setTimestamp(java.time.Instant.now());
    }

    public static EmbedBuilder getUtilityCommandsEmbed(String userLang) {
        return new EmbedBuilder()
                .setTitle("⚙️ " + translationManager.translate("help.utility.title", userLang))
                .setDescription(translationManager.translate("help.utility.description", userLang) + "\n\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .setColor(0x17A2B8)
                .addField("ℹ️ **" + translationManager.translate("help.utility.information", userLang) + "**", 
                    "```yaml\n👤 /userinfo - " + translationManager.translate("help.utility.userinfo_desc", userLang) +
                    "\n🏠 /serverinfo - " + translationManager.translate("help.utility.serverinfo_desc", userLang) +
                    "\n🔍 /roleinfo - " + translationManager.translate("help.utility.roleinfo_desc", userLang) +
                    "\n📊 /stats - " + translationManager.translate("help.utility.stats_desc", userLang) +
                    "\n🎭 /avatar - " + translationManager.translate("help.utility.avatar_desc", userLang) +
                    "\n📅 /created - " + translationManager.translate("help.utility.created_desc", userLang) + "\n```", false)
                .addField("🛠️ **" + translationManager.translate("help.utility.tools", userLang) + "**", 
                    "```yaml\n🔗 /shorturl - " + translationManager.translate("help.utility.shorturl_desc", userLang) +
                    "\n📝 /embed - " + translationManager.translate("help.utility.embed_desc", userLang) +
                    "\n🎨 /color - " + translationManager.translate("help.utility.color_desc", userLang) +
                    "\n📊 /poll - " + translationManager.translate("help.utility.poll_desc", userLang) +
                    "\n⏰ /remind - " + translationManager.translate("help.utility.remind_desc", userLang) +
                    "\n🔍 /search - " + translationManager.translate("help.utility.search_desc", userLang) + "\n```", false)
                .setFooter(translationManager.translate("help.utility.footer", userLang))
                .setTimestamp(java.time.Instant.now());
    }

    public static EmbedBuilder getFunCommandsEmbed(String userLang) {
        return new EmbedBuilder()
                .setTitle("🎮 " + translationManager.translate("help.fun.title", userLang))
                .setDescription(translationManager.translate("help.fun.description", userLang) + "\n\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .setColor(0x28A745)
                .addField("🎲 **" + translationManager.translate("help.fun.games", userLang) + "**", 
                    "```yaml\n🎲 /roll - " + translationManager.translate("help.fun.roll_desc", userLang) +
                    "\n🪙 /coinflip - " + translationManager.translate("help.fun.coinflip_desc", userLang) +
                    "\n🎱 /8ball - " + translationManager.translate("help.fun.8ball_desc", userLang) +
                    "\n🎯 /choose - " + translationManager.translate("help.fun.choose_desc", userLang) +
                    "\n🎪 /random - " + translationManager.translate("help.fun.random_desc", userLang) +
                    "\n🃏 /card - " + translationManager.translate("help.fun.card_desc", userLang) + "\n```", false)
                .addField("💬 **" + translationManager.translate("help.fun.social", userLang) + "**", 
                    "```yaml\n😄 /meme - " + translationManager.translate("help.fun.meme_desc", userLang) +
                    "\n💬 /say - " + translationManager.translate("help.fun.say_desc", userLang) +
                    "\n🎭 /joke - " + translationManager.translate("help.fun.joke_desc", userLang) +
                    "\n📸 /gif - " + translationManager.translate("help.fun.gif_desc", userLang) +
                    "\n❤️ /love - " + translationManager.translate("help.fun.love_desc", userLang) +
                    "\n🎉 /celebrate - " + translationManager.translate("help.fun.celebrate_desc", userLang) + "\n```", false)
                .setFooter(translationManager.translate("help.fun.footer", userLang))
                .setTimestamp(java.time.Instant.now());
    }

    public static EmbedBuilder getConfigCommandsEmbed(String userLang) {
        return new EmbedBuilder()
                .setTitle("🔧 " + translationManager.translate("help.config.title", userLang))
                .setDescription(translationManager.translate("help.config.description", userLang) + "\n\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .setColor(0x6C757D)
                .addField("⚙️ **" + translationManager.translate("help.config.basic", userLang) + "**", 
                    "```yaml\n🔧 /config - " + translationManager.translate("help.config.config_desc", userLang) +
                    "\n📝 /setlang - " + translationManager.translate("help.config.setlang_desc", userLang) +
                    "\n🔔 /logging - " + translationManager.translate("help.config.logging_desc", userLang) +
                    "\n⚙️ /settings - " + translationManager.translate("help.config.settings_desc", userLang) +
                    "\n🎚️ /prefix - " + translationManager.translate("help.config.prefix_desc", userLang) + "\n```", false)
                .addField("🔧 **" + translationManager.translate("help.config.advanced", userLang) + "**", 
                    "```yaml\n📋 /automod - " + translationManager.translate("help.config.automod_desc", userLang) +
                    "\n🎚️ /levels - " + translationManager.translate("help.config.levels_desc", userLang) +
                    "\n🔐 /permissions - " + translationManager.translate("help.config.permissions_desc", userLang) +
                    "\n🎭 /roles - " + translationManager.translate("help.config.roles_desc", userLang) +
                    "\n⏰ /automation - " + translationManager.translate("help.config.automation_desc", userLang) + "\n```", false)
                .addField("⚖️ **" + translationManager.translate("help.config.requirements", userLang) + "**", 
                    "```diff\n- " + translationManager.translate("help.config.req_admin", userLang) +
                    "\n- " + translationManager.translate("help.config.req_manage", userLang) +
                    "\n- " + translationManager.translate("help.config.req_setup", userLang) + "\n```", false)
                .setFooter(translationManager.translate("help.config.footer", userLang))
                .setTimestamp(java.time.Instant.now());
    }

    public static EmbedBuilder getOverviewEmbed(String userLang) {
        return new EmbedBuilder()
                .setTitle("📊 " + translationManager.translate("help.overview.title", userLang))
                .setDescription(translationManager.translate("help.overview.description", userLang) + "\n\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .setColor(0x6F42C1)
                .addField("📚 **" + translationManager.translate("help.overview.basic", userLang) + " (7)**", 
                    "```yaml\nhelp, ping, info, about, invite, support, uptime\n```", true)
                .addField("🛡️ **" + translationManager.translate("help.overview.moderation", userLang) + " (34)**", 
                    "```yaml\nban, tempban, unban, kick, massban, nick, role, mute, unmute, tempmute, timeout, warn, unwarn, warnings, clearwarnings, purge, slowmode, lock, unlock, lockdown, unlockdown, voicekick, voiceban, voiceunban, logs, modstats, logstats, exportlogs, clearlogs, modconfig, automod, addfilter, setlogchannel, setauditchannel\n```", true)
                .addField("⚙️ **" + translationManager.translate("help.overview.utility", userLang) + " (12)**", 
                    "```yaml\nuserinfo, serverinfo, roleinfo, stats, avatar, created, shorturl, embed, color, poll, remind, search\n```", true)
                .addField("🎮 **" + translationManager.translate("help.overview.fun", userLang) + " (12)**", 
                    "```yaml\nroll, coinflip, 8ball, choose, random, card, meme, say, joke, gif, love, celebrate\n```", true)
                .addField("🔧 **" + translationManager.translate("help.overview.config", userLang) + " (10)**", 
                    "```yaml\nconfig, setlang, logging, settings, prefix, automod, levels, permissions, roles, automation\n```", true)
                .addField("📈 **" + translationManager.translate("help.overview.total", userLang) + "**", 
                    "```yaml\n" + translationManager.translate("help.overview.total_desc", userLang, "75", "5") + "\n```", true)
                .setFooter(translationManager.translate("help.overview.footer", userLang))
                .setTimestamp(java.time.Instant.now());
    }

    /**
     * Beregner uptime string baseret på JVM uptime
     */
    private static String getUptimeString() {
        long uptimeMillis = ManagementFactory.getRuntimeMXBean().getUptime();
        long seconds = uptimeMillis / 1000;
        
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        
        StringBuilder uptime = new StringBuilder();
        if (days > 0) uptime.append(days).append("d ");
        if (hours > 0) uptime.append(hours).append("h ");
        if (minutes > 0) uptime.append(minutes).append("m ");
        uptime.append(secs).append("s");
        
        return uptime.toString();
    }
}
