# 📁 Axion Bot - Forbedret Mappestruktur

Dette dokument beskriver den nye og forbedrede mappestruktur for Axion Bot projektet.

## 🏗️ **Projektstruktur**

```
src/main/java/com/axion/bot/
├── AxionBot.java                 # Hovedklasse
├── SlashCommandHandler.java      # Central kommando dispatcher  
├── SlashCommandRegistrar.java    # Kommando registrering
├── CommandHandler.java           # Legacy command handler
│
├── commands/                     # 🎯 Kommando håndtering
│   ├── basic/                    # Grundlæggende kommandoer
│   │   └── BasicCommands.java    # ping, hello, info, time
│   ├── moderation/               # Moderation kommandoer
│   │   └── ModerationCommands.java # ban, kick, warn osv.
│   └── utility/                  # Hjælpe kommandoer
│       └── HelpCommands.java     # help, modhelp
│
├── moderation/                   # 🛡️ Moderation system
│   ├── ModerationManager.java    # Core moderation logic
│   ├── ModerationCommands.java   # Legacy moderation commands
│   ├── ModerationConfig.java     # Konfiguration
│   ├── ModerationAction.java     # Action definitioner
│   └── ModerationResult.java     # Result håndtering
│
├── utils/                        # 🔧 Fælles hjælpeklasser
│   ├── EmbedUtils.java          # Discord embed utilities
│   └── CommandUtils.java        # Command validation utils
│
├── config/                       # ⚙️ Konfiguration
│   └── BotConfig.java           # Central bot konfiguration
│
├── database/                     # 🗄️ Database håndtering
│   └── DatabaseManager.java     # Database connection og setup
│
└── events/                       # 📡 Event listeners
    └── EventListener.java        # Discord event håndtering
```

## 🎯 **Formål med ny struktur**

### **1. commands/** - Kommando organisering
- **basic/**: Grundlæggende bot funktioner (ping, hello, info)
- **moderation/**: Alle moderation relaterede kommandoer
- **utility/**: Hjælpe kommandoer og support funktioner

### **2. utils/** - Fælles funktionalitet
- **EmbedUtils**: Standardiserede Discord embeds med farver og styling
- **CommandUtils**: Fælles validering og permission checks

### **3. config/** - Central konfiguration
- **BotConfig**: Alle bot indstillinger på ét sted
- Feature flags og konstanter

### **4. database/** - Database håndtering
- **DatabaseManager**: Connection pooling og table setup
- Fremtidssikret til persistent data storage

### **5. events/** - Event håndtering
- **EventListener**: Guild join, member join, message events
- Auto-moderation hooks

## ✨ **Fordele ved ny struktur**

### **🔧 Vedligeholdelse**
- Lettere at finde og redigere kode
- Separate ansvar for hver komponent
- Reduceret code duplication

### **📈 Skalerbarhed**
- Nem at tilføje nye kommando kategorier
- Modulær arkitektur
- Plugin-ready struktur

### **🎨 Konsistens**
- Standardiserede embeds på tværs af alle kommandoer
- Ensartet error handling
- Consistent styling og farver

### **🚀 Performance**
- Optimerede imports
- Lazy loading muligheder
- Database connection pooling

## 🔄 **Migration Status**

### ✅ **Færdige komponenter:**
- [x] EmbedUtils med alle farver og emojis
- [x] CommandUtils med permission checks
- [x] BotConfig med centrale indstillinger og invite links
- [x] BasicCommands (ping, hello, info, time)
- [x] HelpCommands (help, modhelp, invite, support, about)
- [x] DatabaseManager setup
- [x] EventListener framework
- [x] Invite system med bot invite links
- [x] Support server integration
- [x] About kommando med detaljeret bot information

### 🔄 **I Progress:**
- [ ] Fuld migration af SlashCommandHandler
- [ ] Refactoring af ModerationCommands
- [ ] Database integration

### 📋 **Fremtidige forbedringer:**
- [ ] Plugin system
- [ ] Custom command framework
- [ ] Advanced auto-moderation
- [ ] Web dashboard integration

## 📚 **Brug af nye klasser**

### **EmbedUtils eksempel:**
```java
// Success embed
EmbedBuilder embed = EmbedUtils.createSuccessEmbed("Success!", "Operation completed");

// Error embed med custom felter
EmbedBuilder error = EmbedUtils.createErrorEmbed("Error", "Something went wrong")
    .addField("Solution", "Try again later", false);
```

### **CommandUtils eksempel:**
```java
// Permission check
if (!CommandUtils.hasModeratorPermissions(event)) {
    // Handle no permission
}

// User formatting
String userMention = CommandUtils.formatUserMention(user);
```

### **BotConfig eksempel:**
```java
// Få bot information
String version = BotConfig.BOT_VERSION;
int maxWarnings = BotConfig.MAX_WARNING_COUNT;
```

## 🔗 **Nye Invite & Support Features**

### **Invite System**
- **`/invite`** - Viser bot invite link med alle nødvendige permissions
- **BotConfig.INVITE_URL** - Centraliseret invite link management
- **Permission guide** - Klar forklaring af hvilke tilladelser botten har brug for

### **Support Integration** 
- **`/support`** - Support server link og FAQ
- **`/about`** - Detaljeret bot information med statistikker
- **Community links** - GitHub, website, support server
- **Bug reporting** - Klare instruktioner til bug rapportering

### **Enhanced Help System**
- **Opdateret `/help`** - Inkluderer alle nye kommandoer
- **Feature showcase** - Fremhæver bot's hovedfunktioner
- **Quick start guide** - Trin-for-trin setup instruktioner

### **Bot Promotion Features**
- **Statistics display** - Server count, user count, uptime
- **Technology showcase** - Java 17, JDA, modern tech stack
- **Feature highlights** - Auto-moderation, embeds, slash commands
- **Social proof** - GitHub stars, community support

## 🎉 **Resultat**

Den nye struktur gør Axion Bot mere professionel, vedligeholdbar og klar til fremtidige udvidelser! Med invite og support features er botten nu klar til distribution og community building.
