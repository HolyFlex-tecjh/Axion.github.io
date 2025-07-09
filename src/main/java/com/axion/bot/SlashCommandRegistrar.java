package com.axion.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registrerer slash kommandoer for Axion Bot
 */
public class SlashCommandRegistrar {
    private static final Logger logger = LoggerFactory.getLogger(SlashCommandRegistrar.class);
    
    /**
     * Registrerer alle slash kommandoer globalt
     */
    public static void registerGlobalCommands(JDA jda) {
        logger.info("Registrerer globale slash kommandoer...");
        
        jda.updateCommands().addCommands(
            // Grundlæggende kommandoer
            Commands.slash("ping", "Teste om botten svarer"),
            
            Commands.slash("hello", "Få en hilsen fra botten"),
            
            Commands.slash("info", "Vis information om botten"),
            
            Commands.slash("help", "Vis tilgængelige kommandoer"),
            
            Commands.slash("time", "Vis nuværende tid"),
            
            Commands.slash("uptime", "Vis hvor længe botten har kørt"),
            
            Commands.slash("modhelp", "Vis moderation kommandoer"),
            
            Commands.slash("invite", "Få invite link til at tilføje botten til din server"),
            
            Commands.slash("support", "Få support og hjælp med botten"),
            
            Commands.slash("about", "Detaljeret information om botten"),
            
            // Debug kommandoer (kun for udviklere)
            Commands.slash("listcommands", "Vis alle registrerede slash kommandoer"),
            
            Commands.slash("forcesync", "Force synkroniser slash kommandoer"),
            
            // Moderation kommandoer
            Commands.slash("ban", "Ban en bruger fra serveren")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren der skal bannes", true),
                    new OptionData(OptionType.STRING, "reason", "Årsag til ban", false)
                ),
            
            Commands.slash("kick", "Kick en bruger fra serveren")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren der skal kickes", true),
                    new OptionData(OptionType.STRING, "reason", "Årsag til kick", false)
                ),
            
            Commands.slash("timeout", "Giv en bruger timeout")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren der skal have timeout", true),
                    new OptionData(OptionType.INTEGER, "duration", "Varighed i minutter (1-10080)", true)
                        .setMinValue(1)
                        .setMaxValue(10080), // Max 7 dage
                    new OptionData(OptionType.STRING, "reason", "Årsag til timeout", false)
                ),
            
            Commands.slash("warn", "Advar en bruger")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren der skal advares", true),
                    new OptionData(OptionType.STRING, "reason", "Årsag til advarsel", true)
                ),
            
            Commands.slash("unwarn", "Fjern alle advarsler fra en bruger")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren hvis advarsler skal fjernes", true)
                ),
            
            Commands.slash("warnings", "Vis antal advarsler for en bruger")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren hvis advarsler skal vises", true)
                ),
            
            Commands.slash("purge", "Slet et antal beskeder fra kanalen")
                .addOptions(
                    new OptionData(OptionType.INTEGER, "amount", "Antal beskeder at slette (1-100)", true)
                        .setMinValue(1)
                        .setMaxValue(100)
                ),
            
            Commands.slash("modconfig", "Konfigurer moderation indstillinger")
                .addOptions(
                    new OptionData(OptionType.STRING, "level", "Moderation niveau", false)
                        .addChoice("Mild", "mild")
                        .addChoice("Standard", "standard")
                        .addChoice("Streng", "strict")
                ),
            
            Commands.slash("modstats", "Vis moderation statistikker"),
            
            Commands.slash("addfilter", "Tilføj et ord til custom filteret")
                .addOptions(
                    new OptionData(OptionType.STRING, "word", "Ordet der skal filtreres", true)
                ),
            
            // Udvidede moderation kommandoer
            Commands.slash("mute", "Mute en bruger (fjern tale rettigheder)")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren der skal mutes", true),
                    new OptionData(OptionType.STRING, "reason", "Årsag til mute", false)
                ),
            
            Commands.slash("unmute", "Unmute en bruger (gendan tale rettigheder)")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren der skal unmutes", true)
                ),
            
            Commands.slash("slowmode", "Sæt slowmode for kanalen")
                .addOptions(
                    new OptionData(OptionType.INTEGER, "seconds", "Sekunder mellem beskeder (0-21600)", true)
                        .setMinValue(0)
                        .setMaxValue(21600)
                ),
            
            Commands.slash("lock", "Lås kanalen (forhindre beskeder)")
                .addOptions(
                    new OptionData(OptionType.STRING, "reason", "Årsag til låsning", false)
                ),
            
            Commands.slash("unlock", "Lås kanalen op (tillad beskeder igen)")
                .addOptions(
                    new OptionData(OptionType.STRING, "reason", "Årsag til oplåsning", false)
                ),
            
            Commands.slash("unban", "Unban en bruger")
                .addOptions(
                    new OptionData(OptionType.STRING, "userid", "Bruger ID der skal unbans", true),
                    new OptionData(OptionType.STRING, "reason", "Årsag til unban", false)
                ),
            
            Commands.slash("massban", "Ban flere brugere på én gang")
                .addOptions(
                    new OptionData(OptionType.STRING, "userids", "Bruger IDs adskilt af komma", true),
                    new OptionData(OptionType.STRING, "reason", "Årsag til masseban", false)
                ),
            
            Commands.slash("nick", "Skift nickname på en bruger")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren hvis nickname skal ændres", true),
                    new OptionData(OptionType.STRING, "nickname", "Nyt nickname (tom for at fjerne)", false)
                ),
            
            Commands.slash("role", "Giv eller fjern en rolle fra en bruger")
                .addOptions(
                    new OptionData(OptionType.STRING, "action", "Handling", true)
                        .addChoice("Giv", "add")
                        .addChoice("Fjern", "remove"),
                    new OptionData(OptionType.USER, "user", "Brugeren", true),
                    new OptionData(OptionType.ROLE, "role", "Rollen", true)
                ),
            
            // Sprog kommandoer
            Commands.slash("setlanguage", "Skift dit sprog")
                .addOptions(
                    new OptionData(OptionType.STRING, "language", "Sprogkode (f.eks. da, en, de)", true)
                ),
            
            Commands.slash("languages", "Vis tilgængelige sprog"),
            
            Commands.slash("resetlanguage", "Nulstil dit sprog til standard (engelsk)"),
            
            // Yderligere moderation kommandoer
            Commands.slash("clearwarnings", "Fjern alle advarsler fra en bruger")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren hvis advarsler skal fjernes", true)
                ),
            
            Commands.slash("serverinfo", "Vis detaljeret information om serveren"),
            
            Commands.slash("userinfo", "Vis detaljeret information om en bruger")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren der skal vises information om", false)
                ),
            
            Commands.slash("avatar", "Vis en brugers avatar")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren hvis avatar skal vises", false)
                ),
            
            Commands.slash("lockdown", "Lås hele serveren ned (emergency)")
                .addOptions(
                    new OptionData(OptionType.STRING, "reason", "Årsag til lockdown", false)
                ),
            
            Commands.slash("unlockdown", "Fjern server lockdown")
                .addOptions(
                    new OptionData(OptionType.STRING, "reason", "Årsag til at fjerne lockdown", false)
                ),
            
            Commands.slash("automod", "Konfigurer automatisk moderation")
                .addOptions(
                    new OptionData(OptionType.STRING, "action", "Handling", true)
                        .addChoice("Enable", "enable")
                        .addChoice("Disable", "disable")
                        .addChoice("Status", "status")
                        .addChoice("Config", "config")
                ),
            
            Commands.slash("tempban", "Midlertidig ban af en bruger")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren der skal tempbans", true),
                    new OptionData(OptionType.STRING, "duration", "Varighed (f.eks. 1d, 2h, 30m)", true),
                    new OptionData(OptionType.STRING, "reason", "Årsag til tempban", false)
                ),
            
            Commands.slash("tempmute", "Midlertidig mute af en bruger")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren der skal tempmutes", true),
                    new OptionData(OptionType.STRING, "duration", "Varighed (f.eks. 1d, 2h, 30m)", true),
                    new OptionData(OptionType.STRING, "reason", "Årsag til tempmute", false)
                ),
            
            Commands.slash("voicekick", "Kick en bruger fra voice kanal")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren der skal voice kickes", true),
                    new OptionData(OptionType.STRING, "reason", "Årsag til voice kick", false)
                ),
            
            Commands.slash("voiceban", "Ban en bruger fra alle voice kanaler")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren der skal voice bans", true),
                    new OptionData(OptionType.STRING, "reason", "Årsag til voice ban", false)
                ),
            
            Commands.slash("voiceunban", "Unban en bruger fra voice kanaler")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren der skal voice unbans", true)
                ),
            
            // Logging kommandoer
            Commands.slash("logs", "Vis moderation logs")
                .addOptions(
                    new OptionData(OptionType.STRING, "type", "Type af logs", false)
                        .addChoice("Alle", "all")
                        .addChoice("Bans", "ban")
                        .addChoice("Kicks", "kick")
                        .addChoice("Timeouts", "timeout")
                        .addChoice("Advarsler", "warn")
                        .addChoice("Auto-mod", "automod"),
                    new OptionData(OptionType.USER, "user", "Vis logs for specifik bruger", false),
                    new OptionData(OptionType.INTEGER, "limit", "Antal logs at vise (1-50)", false)
                        .setMinValue(1)
                        .setMaxValue(50)
                ),
            
            Commands.slash("setlogchannel", "Sæt moderation log kanal")
                .addOptions(
                    new OptionData(OptionType.CHANNEL, "channel", "Kanalen til moderation logs", true)
                        .setChannelTypes(ChannelType.TEXT)
                ),
            
            Commands.slash("setauditchannel", "Sæt audit log kanal")
                .addOptions(
                    new OptionData(OptionType.CHANNEL, "channel", "Kanalen til audit logs", true)
                        .setChannelTypes(ChannelType.TEXT)
                ),
            
            Commands.slash("clearlogs", "Ryd alle logs for serveren")
                .addOptions(
                    new OptionData(OptionType.BOOLEAN, "confirm", "Bekræft at du vil rydde alle logs", true)
                ),
            
            Commands.slash("exportlogs", "Eksporter logs til fil")
                .addOptions(
                    new OptionData(OptionType.STRING, "format", "Filformat", true)
                        .addChoice("JSON", "json")
                        .addChoice("CSV", "csv")
                        .addChoice("TXT", "txt"),
                    new OptionData(OptionType.INTEGER, "days", "Antal dage tilbage (1-30)", false)
                        .setMinValue(1)
                        .setMaxValue(30)
                ),
            
            Commands.slash("logstats", "Vis log statistikker")
                .addOptions(
                    new OptionData(OptionType.STRING, "period", "Tidsperiode", false)
                        .addChoice("I dag", "today")
                        .addChoice("Denne uge", "week")
                        .addChoice("Denne måned", "month")
                        .addChoice("Alt", "all")
                ),
            
            Commands.slash("logconfig", "Konfigurer logging indstillinger")
                .addOptions(
                    new OptionData(OptionType.STRING, "setting", "Indstilling", true)
                        .addChoice("Enable logging", "enable")
                        .addChoice("Disable logging", "disable")
                        .addChoice("Detailed logging", "detailed")
                        .addChoice("Retention days", "retention")
                        .addChoice("View config", "view"),
                    new OptionData(OptionType.INTEGER, "value", "Værdi (for retention days)", false)
                        .setMinValue(1)
                        .setMaxValue(365)
                ),
            
            // Yderligere moderation kommandoer
            Commands.slash("clearwarnings", "Fjern alle advarsler fra en bruger")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren hvis advarsler skal fjernes", true)
                ),
            
            Commands.slash("serverinfo", "Vis detaljeret information om serveren"),
            
            Commands.slash("userinfo", "Vis detaljeret information om en bruger")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren der skal vises information om", false)
                ),
            
            Commands.slash("avatar", "Vis en brugers avatar")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren hvis avatar skal vises", false)
                ),
            
            Commands.slash("lockdown", "Lås hele serveren ned (emergency)")
                .addOptions(
                    new OptionData(OptionType.STRING, "reason", "Årsag til lockdown", false)
                ),
            
            Commands.slash("unlockdown", "Fjern server lockdown")
                .addOptions(
                    new OptionData(OptionType.STRING, "reason", "Årsag til at fjerne lockdown", false)
                ),
            
            Commands.slash("automod", "Konfigurer automatisk moderation")
                .addOptions(
                    new OptionData(OptionType.STRING, "action", "Handling", true)
                        .addChoice("Enable", "enable")
                        .addChoice("Disable", "disable")
                        .addChoice("Status", "status")
                        .addChoice("Config", "config")
                ),
            
            Commands.slash("tempban", "Midlertidig ban af en bruger")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren der skal tempbans", true),
                    new OptionData(OptionType.STRING, "duration", "Varighed (f.eks. 1d, 2h, 30m)", true),
                    new OptionData(OptionType.STRING, "reason", "Årsag til tempban", false)
                ),
            
            Commands.slash("tempmute", "Midlertidig mute af en bruger")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren der skal tempmutes", true),
                    new OptionData(OptionType.STRING, "duration", "Varighed (f.eks. 1d, 2h, 30m)", true),
                    new OptionData(OptionType.STRING, "reason", "Årsag til tempmute", false)
                ),
            
            Commands.slash("voicekick", "Kick en bruger fra voice kanal")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren der skal voice kickes", true),
                    new OptionData(OptionType.STRING, "reason", "Årsag til voice kick", false)
                ),
            
            Commands.slash("voiceban", "Ban en bruger fra alle voice kanaler")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren der skal voice bans", true),
                    new OptionData(OptionType.STRING, "reason", "Årsag til voice ban", false)
                ),
            
            Commands.slash("voiceunban", "Unban en bruger fra voice kanaler")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren der skal voice unbans", true)
                ),
            
            // Logging kommandoer
            Commands.slash("logs", "Vis moderation logs")
                .addOptions(
                    new OptionData(OptionType.STRING, "type", "Type af logs", false)
                        .addChoice("Alle", "all")
                        .addChoice("Bans", "ban")
                        .addChoice("Kicks", "kick")
                        .addChoice("Timeouts", "timeout")
                        .addChoice("Advarsler", "warn")
                        .addChoice("Auto-mod", "automod"),
                    new OptionData(OptionType.USER, "user", "Vis logs for specifik bruger", false),
                    new OptionData(OptionType.INTEGER, "limit", "Antal logs at vise (1-50)", false)
                        .setMinValue(1)
                        .setMaxValue(50)
                ),
            
            Commands.slash("setlogchannel", "Sæt moderation log kanal")
                .addOptions(
                    new OptionData(OptionType.CHANNEL, "channel", "Kanalen til moderation logs", true)
                        .setChannelTypes(ChannelType.TEXT)
                ),
            
            Commands.slash("setauditchannel", "Sæt audit log kanal")
                .addOptions(
                    new OptionData(OptionType.CHANNEL, "channel", "Kanalen til audit logs", true)
                        .setChannelTypes(ChannelType.TEXT)
                ),
            
            Commands.slash("clearlogs", "Ryd alle logs for serveren")
                .addOptions(
                    new OptionData(OptionType.BOOLEAN, "confirm", "Bekræft at du vil rydde alle logs", true)
                ),
            
            Commands.slash("exportlogs", "Eksporter logs til fil")
                .addOptions(
                    new OptionData(OptionType.STRING, "format", "Filformat", true)
                        .addChoice("JSON", "json")
                        .addChoice("CSV", "csv")
                        .addChoice("TXT", "txt"),
                    new OptionData(OptionType.INTEGER, "days", "Antal dage tilbage (1-30)", false)
                        .setMinValue(1)
                        .setMaxValue(30)
                ),
            
            Commands.slash("logstats", "Vis log statistikker")
                .addOptions(
                    new OptionData(OptionType.STRING, "period", "Tidsperiode", false)
                        .addChoice("I dag", "today")
                        .addChoice("Denne uge", "week")
                        .addChoice("Denne måned", "month")
                        .addChoice("Alt", "all")
                ),
            
            Commands.slash("logconfig", "Konfigurer logging indstillinger")
                .addOptions(
                    new OptionData(OptionType.STRING, "setting", "Indstilling", true)
                        .addChoice("Enable logging", "enable")
                        .addChoice("Disable logging", "disable")
                        .addChoice("Detailed logging", "detailed")
                        .addChoice("Retention days", "retention")
                        .addChoice("View config", "view"),
                    new OptionData(OptionType.INTEGER, "value", "Værdi (for retention days)", false)
                        .setMinValue(1)
                        .setMaxValue(365)
                ),
            
            // Yderligere moderation kommandoer (kun for udviklere)
            Commands.slash("devinfo", "Vis udvikler information (kun for udviklere)"),
            
            Commands.slash("devstats", "Vis detaljerede bot statistikker (kun for udviklere)")
        ).queue(
            success -> logger.info("Slash kommandoer registreret succesfuldt!"),
            error -> logger.error("Fejl ved registrering af slash kommandoer: {}", error.getMessage())
        );
    }
    
    // Fjernet duplikeret metode registerGuildCommands for at undgå fejl om dublet-signaturer.
    
    /**
     * Registrerer slash kommandoer for en specifik guild (hurtigere opdatering)
     */
    public static void registerGuildCommands(JDA jda, String guildId) {
        logger.info("Registrerer guild-specifikke slash kommandoer for guild: {}", guildId);
        
        jda.getGuildById(guildId).updateCommands().addCommands(
            // Samme kommandoer som globale, men opdateres øjeblikkeligt for denne guild
            Commands.slash("ping", "Teste om botten svarer"),
            Commands.slash("hello", "Få en hilsen fra botten"),
            Commands.slash("info", "Vis information om botten"),
            Commands.slash("help", "Vis tilgængelige kommandoer"),
            Commands.slash("time", "Vis nuværende tid"),
            Commands.slash("uptime", "Vis hvor længe botten har kørt"),
            Commands.slash("modhelp", "Vis moderation kommandoer"),
            Commands.slash("invite", "Få invite link til at tilføje botten til din server"),
            Commands.slash("support", "Få support og hjælp med botten"),
            Commands.slash("about", "Detaljeret information om botten"),
            
            // Debug kommandoer (kun for udviklere)
            Commands.slash("listcommands", "Vis alle registrerede slash kommandoer"),
            Commands.slash("forcesync", "Force synkroniser slash kommandoer"),
            
            Commands.slash("ban", "Ban en bruger fra serveren")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren der skal bannes", true),
                    new OptionData(OptionType.STRING, "reason", "Årsag til ban", false)
                ),
            
            Commands.slash("kick", "Kick en bruger fra serveren")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren der skal kickes", true),
                    new OptionData(OptionType.STRING, "reason", "Årsag til kick", false)
                ),
            
            Commands.slash("timeout", "Giv en bruger timeout")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren der skal have timeout", true),
                    new OptionData(OptionType.INTEGER, "duration", "Varighed i minutter (1-10080)", true)
                        .setMinValue(1)
                        .setMaxValue(10080),
                    new OptionData(OptionType.STRING, "reason", "Årsag til timeout", false)
                ),
            
            Commands.slash("warn", "Advar en bruger")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren der skal advares", true),
                    new OptionData(OptionType.STRING, "reason", "Årsag til advarsel", true)
                ),
            
            Commands.slash("unwarn", "Fjern alle advarsler fra en bruger")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren hvis advarsler skal fjernes", true)
                ),
            
            Commands.slash("warnings", "Vis antal advarsler for en bruger")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren hvis advarsler skal vises", true)
                ),
            
            Commands.slash("purge", "Slet et antal beskeder fra kanalen")
                .addOptions(
                    new OptionData(OptionType.INTEGER, "amount", "Antal beskeder at slette (1-100)", true)
                        .setMinValue(1)
                        .setMaxValue(100)
                ),
            
            Commands.slash("modconfig", "Konfigurer moderation indstillinger")
                .addOptions(
                    new OptionData(OptionType.STRING, "level", "Moderation niveau", false)
                        .addChoice("Mild", "mild")
                        .addChoice("Standard", "standard")
                        .addChoice("Streng", "strict")
                ),
            
            Commands.slash("modstats", "Vis moderation statistikker"),
            
            Commands.slash("addfilter", "Tilføj et ord til custom filteret")
                .addOptions(
                    new OptionData(OptionType.STRING, "word", "Ordet der skal filtreres", true)
                ),
            
            // Udvidede moderation kommandoer
            Commands.slash("mute", "Mute en bruger (fjern tale rettigheder)")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren der skal mutes", true),
                    new OptionData(OptionType.STRING, "reason", "Årsag til mute", false)
                ),
            
            Commands.slash("unmute", "Unmute en bruger (gendan tale rettigheder)")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren der skal unmutes", true)
                ),
            
            Commands.slash("slowmode", "Sæt slowmode for kanalen")
                .addOptions(
                    new OptionData(OptionType.INTEGER, "seconds", "Sekunder mellem beskeder (0-21600)", true)
                        .setMinValue(0)
                        .setMaxValue(21600)
                ),
            
            Commands.slash("lock", "Lås kanalen (forhindre beskeder)")
                .addOptions(
                    new OptionData(OptionType.STRING, "reason", "Årsag til låsning", false)
                ),
            
            Commands.slash("unlock", "Lås kanalen op (tillad beskeder igen)")
                .addOptions(
                    new OptionData(OptionType.STRING, "reason", "Årsag til oplåsning", false)
                ),
            
            Commands.slash("unban", "Unban en bruger")
                .addOptions(
                    new OptionData(OptionType.STRING, "userid", "Bruger ID der skal unbans", true),
                    new OptionData(OptionType.STRING, "reason", "Årsag til unban", false)
                ),
            
            Commands.slash("massban", "Ban flere brugere på én gang")
                .addOptions(
                    new OptionData(OptionType.STRING, "userids", "Bruger IDs adskilt af komma", true),
                    new OptionData(OptionType.STRING, "reason", "Årsag til masseban", false)
                ),
            
            Commands.slash("nick", "Skift nickname på en bruger")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren hvis nickname skal ændres", true),
                    new OptionData(OptionType.STRING, "nickname", "Nyt nickname (tom for at fjerne)", false)
                ),
            
            Commands.slash("role", "Giv eller fjern en rolle fra en bruger")
                .addOptions(
                    new OptionData(OptionType.STRING, "action", "Handling", true)
                        .addChoice("Giv", "add")
                        .addChoice("Fjern", "remove"),
                    new OptionData(OptionType.USER, "user", "Brugeren", true),
                    new OptionData(OptionType.ROLE, "role", "Rollen", true)
                ),
            
            // Sprog kommandoer
            Commands.slash("setlanguage", "Skift dit sprog")
                .addOptions(
                    new OptionData(OptionType.STRING, "language", "Sprogkode (f.eks. da, en, de)", true)
                ),
            
            Commands.slash("languages", "Vis tilgængelige sprog"),
            
            Commands.slash("resetlanguage", "Nulstil dit sprog til standard (engelsk)")
        ).queue(
            success -> logger.info("Guild slash kommandoer registreret succesfuldt for guild: {}", guildId),
            error -> logger.error("Fejl ved registrering af guild slash kommandoer: {}", error.getMessage())
        );
    }
    
    /**
     * Sletter alle globale slash kommandoer
     */
    public static void clearGlobalCommands(JDA jda) {
        logger.info("Sletter alle globale slash kommandoer...");
        
        jda.updateCommands().queue(
            success -> logger.info("Globale slash kommandoer slettet succesfuldt!"),
            error -> logger.error("Fejl ved sletning af globale slash kommandoer: {}", error.getMessage())
        );
    }
    
    /**
     * Sletter alle guild-specifikke slash kommandoer
     */
    public static void clearGuildCommands(JDA jda, String guildId) {
        logger.info("Sletter guild slash kommandoer for guild: {}", guildId);
        
        jda.getGuildById(guildId).updateCommands().queue(
            success -> logger.info("Guild slash kommandoer slettet succesfuldt for guild: {}", guildId),
            error -> logger.error("Fejl ved sletning af guild slash kommandoer: {}", error.getMessage())
        );
    }
    
    /**
     * Dynamically refreshes all slash commands by clearing and re-registering them
     * This method provides better control over the sync process for forcesync command
     */
    public static void refreshAllCommands(JDA jda, java.util.function.Consumer<Boolean> callback) {
        logger.info("Starting dynamic refresh of all slash commands...");
        
        // First clear all existing commands
        jda.updateCommands().queue(
            clearSuccess -> {
                logger.info("Successfully cleared existing commands, now re-registering...");
                
                // Re-register all commands
                registerGlobalCommands(jda);
                
                // Wait a moment then verify
                java.util.concurrent.ScheduledExecutorService scheduler = 
                    java.util.concurrent.Executors.newSingleThreadScheduledExecutor();
                
                scheduler.schedule(() -> {
                    jda.retrieveCommands().queue(
                        commands -> {
                            logger.info("Command refresh completed successfully. Total commands: {}", commands.size());
                            callback.accept(true);
                            scheduler.shutdown();
                        },
                        error -> {
                            logger.error("Failed to verify refreshed commands: {}", error.getMessage());
                            callback.accept(false);
                            scheduler.shutdown();
                        }
                    );
                }, 2, java.util.concurrent.TimeUnit.SECONDS);
            },
            clearError -> {
                logger.error("Failed to clear existing commands during refresh: {}", clearError.getMessage());
                callback.accept(false);
            }
        );
    }
    
    /**
     * Gets the total count of registered commands for verification purposes
     */
    public static void getCommandCount(JDA jda, java.util.function.Consumer<Integer> callback) {
        jda.retrieveCommands().queue(
            commands -> callback.accept(commands.size()),
            error -> {
                logger.error("Failed to retrieve command count: {}", error.getMessage());
                callback.accept(-1);
            }
        );
    }
}