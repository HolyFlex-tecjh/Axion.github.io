package com.axion.bot.utils;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Utility klasse til almindelige command funktioner
 */
public class CommandUtils {

    /**
     * Tjekker om brugeren har moderator tilladelser
     */
    public static boolean hasModeratorPermissions(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        if (member == null) return false;
        
        return member.hasPermission(Permission.MODERATE_MEMBERS) ||
               member.hasPermission(Permission.KICK_MEMBERS) ||
               member.hasPermission(Permission.BAN_MEMBERS) ||
               member.hasPermission(Permission.MANAGE_SERVER);
    }

    /**
     * Tjekker om brugeren har administrator tilladelser
     */
    public static boolean hasAdminPermissions(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        if (member == null) return false;
        
        return member.hasPermission(Permission.ADMINISTRATOR) ||
               member.hasPermission(Permission.MANAGE_SERVER);
    }

    /**
     * Tjekker om brugeren kan moderere en anden bruger
     */
    public static boolean canModerate(Member moderator, Member target) {
        if (moderator == null || target == null) return false;
        if (moderator.equals(target)) return false;
        
        // Tjek hierarki
        if (!moderator.canInteract(target)) return false;
        
        return true;
    }

    /**
     * Formaterer en bruger mention med navn
     */
    public static String formatUserMention(net.dv8tion.jda.api.entities.User user) {
        return user.getAsMention() + " (" + user.getName() + ")";
    }

    /**
     * Genererer en standard "ingen tilladelse" besked
     */
    public static String getNoPermissionMessage() {
        return "Du har ikke tilladelse til at bruge denne kommando!";
    }

    /**
     * Genererer en standard "manglende parameter" besked
     */
    public static String getMissingParameterMessage(String parameter) {
        return "Du skal angive " + parameter + "!";
    }
}
