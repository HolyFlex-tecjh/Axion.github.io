# Translation Keys Needed for HelpCommands.java

**VIGTIG**: Alle disse translation keys skal tilføjes til dine translation-filer (f.eks. `en.json`, `da.json`, osv.) for at HelpCommands.java kan fungere korrekt.

## General Help System Keys

### Main Help Command (`/help`)
```
help.title
help.interactive_menu
help.description
help.how_to_use
help.select_category
help.view_commands
help.available_categories
help.ping
help.memory
help.uptime
help.quick_links
help.modhelp_desc
help.support_desc
help.invite_desc
help.bot_status
```

### Category Selection Dropdown
```
help.select_placeholder
help.category.basic
help.category.basic_desc
help.category.moderation
help.category.moderation_desc
help.category.utility
help.category.utility_desc
help.category.fun
help.category.fun_desc
help.category.config
help.category.config_desc
help.category.overview
help.category.overview_desc
```

## ModHelp Command (`/modhelp`)
```
help.modhelp.title
help.modhelp.description
help.modhelp.user_commands
help.modhelp.warning_system
help.modhelp.channel_management
help.modhelp.config_stats
help.modhelp.auto_moderation
help.modhelp.permissions
help.modhelp.pro_tips
help.modhelp.footer

# Command descriptions
help.modhelp.ban_desc
help.modhelp.kick_desc
help.modhelp.mute_desc
help.modhelp.clear_desc
help.modhelp.warn_desc
help.modhelp.warnings_desc
help.modhelp.unwarn_desc
help.modhelp.modlog_desc

# Features
help.modhelp.channel_lock
help.modhelp.bulk_delete
help.modhelp.message_pin
help.modhelp.channel_config
help.modhelp.config_modlog
help.modhelp.config_automod
help.modhelp.config_roles
help.modhelp.spam_protection
help.modhelp.link_filter
help.modhelp.profanity_filter
help.modhelp.anti_raid

# Requirements & Tips
help.modhelp.requires_perms
help.modhelp.admin_mod_role
help.modhelp.specific_perms
help.modhelp.tip_reason
help.modhelp.tip_temp
help.modhelp.tip_modlog
```

## Invite Command (`/invite`)
```
help.invite.title
help.invite.description
help.invite.bot_link
help.invite.click_to_invite
help.invite.permissions
help.invite.features
help.invite.setup_guide
help.invite.footer

# Permissions
help.invite.admin_perms
help.invite.manage_perms
help.invite.mod_capabilities

# Features
help.invite.feature_moderation
help.invite.feature_automod
help.invite.feature_stats
help.invite.feature_multilang

# Setup Steps
help.invite.step1
help.invite.step2
help.invite.step3
help.invite.step4
```

## Support Command (`/support`)
```
help.support.title
help.support.description
help.support.server
help.support.join_server
help.support.get_help
help.support.bugs
help.support.found_bug
help.support.features
help.support.feature_ideas
help.support.share_ideas
help.support.documentation
help.support.website
help.support.faq
help.support.faq_desc
help.support.quick_answers
help.support.footer
```

## About Command (`/about`)
```
help.about.title
help.about.description
help.about.live_stats
help.about.system_info
help.about.tech_stack
help.about.main_features
help.about.important_links
help.about.support_community
help.about.why_choose
help.about.footer

# Stats Labels
help.about.servers
help.about.users
help.about.ping
help.about.version
help.about.uptime
help.about.memory

# Features
help.about.feature_moderation
help.about.feature_automod
help.about.feature_stats
help.about.feature_multilang
help.about.feature_settings

# Links
help.about.invite_bot
help.about.support_server
help.about.github_repo
help.about.website

# Community
help.about.join_community
help.about.get_support
help.about.events

# Why Choose
help.about.reliable
help.about.regular_updates
help.about.active_support
help.about.open_source
```

## Category Helper Methods

### Basic Commands
```
help.basic.title
help.basic.description
help.basic.commands
help.basic.footer

# Command descriptions
help.basic.help_desc
help.basic.ping_desc
help.basic.info_desc
help.basic.about_desc
help.basic.invite_desc
help.basic.support_desc
help.basic.uptime_desc
```

### Moderation Commands
```
help.moderation.title
help.moderation.description
help.moderation.user_management
help.moderation.message_management
help.moderation.requirements
help.moderation.footer

# Command descriptions
help.moderation.ban_desc
help.moderation.kick_desc
help.moderation.mute_desc
help.moderation.unmute_desc
help.moderation.warn_desc
help.moderation.unwarn_desc
help.moderation.clear_desc
help.moderation.modlog_desc
help.moderation.warnings_desc
help.moderation.lock_desc
help.moderation.unlock_desc

# Requirements
help.moderation.req_permissions
help.moderation.req_admin
help.moderation.req_discord
```

### Utility Commands
```
help.utility.title
help.utility.description
help.utility.information
help.utility.tools
help.utility.footer

# Command descriptions
help.utility.userinfo_desc
help.utility.serverinfo_desc
help.utility.roleinfo_desc
help.utility.stats_desc
help.utility.avatar_desc
help.utility.created_desc
help.utility.shorturl_desc
help.utility.embed_desc
help.utility.color_desc
help.utility.poll_desc
help.utility.remind_desc
help.utility.search_desc
```

### Fun Commands
```
help.fun.title
help.fun.description
help.fun.games
help.fun.social
help.fun.footer

# Command descriptions
help.fun.roll_desc
help.fun.coinflip_desc
help.fun.8ball_desc
help.fun.choose_desc
help.fun.random_desc
help.fun.card_desc
help.fun.meme_desc
help.fun.say_desc
help.fun.joke_desc
help.fun.gif_desc
help.fun.love_desc
help.fun.celebrate_desc
```

### Configuration Commands
```
help.config.title
help.config.description
help.config.basic
help.config.advanced
help.config.requirements
help.config.footer

# Command descriptions
help.config.config_desc
help.config.setlang_desc
help.config.logging_desc
help.config.settings_desc
help.config.prefix_desc
help.config.automod_desc
help.config.levels_desc
help.config.permissions_desc
help.config.roles_desc
help.config.automation_desc

# Requirements
help.config.req_admin
help.config.req_manage
help.config.req_setup
```

### Overview
```
help.overview.title
help.overview.description
help.overview.basic
help.overview.moderation
help.overview.utility
help.overview.fun
help.overview.config
help.overview.total
help.overview.total_desc
help.overview.footer
```

## Eksempel på engelsk translation (en.json):

```json
{
  "help": {
    "title": "Bot Command Help",
    "interactive_menu": "Interactive Command Menu",
    "description": "Welcome to {0}! Use the dropdown menu below to explore all available commands organized by category.",
    "select_placeholder": "Select a command category...",
    "category": {
      "basic": "Basic Commands",
      "basic_desc": "Essential bot commands like ping, info, invite",
      "moderation": "Moderation Commands", 
      "moderation_desc": "Server moderation tools - ban, kick, warn, mute",
      "utility": "Utility Commands",
      "utility_desc": "Useful utility commands - userinfo, serverinfo, stats",
      "fun": "Fun Commands",
      "fun_desc": "Entertainment commands - roll, coinflip, 8ball, meme",
      "config": "Configuration Commands",
      "config_desc": "Bot setup and configuration commands",
      "overview": "All Commands Overview",
      "overview_desc": "Quick overview of all available commands"
    }
  }
}
```

**NOTE**: For at få den interactive dropdown-menu til at fungere, skal du også implementere en interaction handler i din hovedbot, der lytter efter "help-menu" selection events og kalder de relevante embed-metoder.
