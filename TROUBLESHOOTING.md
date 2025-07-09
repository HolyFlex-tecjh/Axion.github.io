# 🔧 Slash Command Troubleshooting Guide

Hvis dine nye slash kommandoer (`/invite`, `/support`, `/about`) ikke virker på Discord, følg denne guide.

## 🚨 **Almindelige Problemer og Løsninger**

### **1. Slash kommandoer vises ikke i Discord**

**Årsag:** Kommandoerne er ikke synkroniseret med Discord endnu.

**Løsning:**
```bash
# Stop botten
# Genstart botten for at force-registrere kommandoer
mvn clean compile
mvn exec:java -Dexec.mainClass="com.axion.bot.AxionBot"
```

**Alternativt:**
- Brug `/listcommands` for at se alle registrerede kommandoer
- Brug `/forcesync` for at force-synkronisere

### **2. Kommandoer er registreret men virker ikke**

**Årsag:** Discord cache eller timing problemer.

**Løsning:**
1. **Wait it out** - Globale slash kommandoer kan tage op til 1 time at synkronisere
2. **Restart Discord** - Luk og genåbn Discord appen
3. **Clear Discord cache:**
   - Windows: `%appdata%/discord/Cache`
   - Mac: `~/Library/Application Support/discord/Cache`

### **3. Bot svarer ikke på kommandoer**

**Årsag:** Bot kører ikke den nyeste kode.

**Løsning:**
```bash
# Stop existing bot process
# Build latest code
mvn clean package

# Run latest version
java -jar target/axion-bot-1.0.0.jar
```

### **4. "Unknown interaction" fejl**

**Årsag:** Bot'en modtager kommandoer den ikke kender.

**Løsning:**
1. Tjek at alle nye kommandoer er i `SlashCommandHandler.java`
2. Tjek at kommandoerne er registreret i `SlashCommandRegistrar.java`
3. Restart bot'en

## 🔍 **Debug Steps**

### **Step 1: Verificer Registrering**
```java
// Check SlashCommandRegistrar.java
Commands.slash("invite", "Få invite link til at tilføje botten til din server"),
Commands.slash("support", "Få support og hjælp med botten"),
Commands.slash("about", "Detaljeret information om botten"),
```

### **Step 2: Verificer Handler**
```java
// Check SlashCommandHandler.java
case "invite":
    HelpCommands.handleInvite(event);
    break;
case "support":
    HelpCommands.handleSupport(event);
    break;
case "about":
    HelpCommands.handleAbout(event);
    break;
```

### **Step 3: Tjek Bot Permissions**
Bot'en skal have disse permissions:
- ✅ `applications.commands` scope
- ✅ `Send Messages` permission
- ✅ `Use Slash Commands` permission

### **Step 4: Brug Debug Kommandoer**

**Tjek registrerede kommandoer:**
```
/listcommands
```

**Force synkroniser:**
```
/forcesync
```

*Note: Debug kommandoerne bruger nu den nye udvikler konfiguration system*

## ⚙️ **Konfiguration Fix**

### **Konfigurer Udviklere i config.properties:**

1. **Få din Discord User ID:**
   - Højreklik på dig selv i Discord
   - "Copy User ID" (kræver Developer Mode)

2. **Opdater config.properties:**
   ```properties
   # Bot ejer (kun én person)
   bot.owner.id=123456789012345678
   
   # Udviklere (kommasepareret liste)
   bot.developers.ids=123456789012345678,987654321098765432
   ```

3. **Genstart bot:**
   ```bash
   mvn clean compile exec:java
   ```

**Bemærk:** Kun registrerede udviklere kan bruge `/listcommands` og `/forcesync` kommandoerne.

## 🕐 **Timing Issues**

### **Global Commands vs Guild Commands**

**Nuværende setup:** Global commands (1-60 minutter sync tid)

**Quick fix for testing:**
Skift til guild-specific commands for hurtigere testing:

```java
// I SlashCommandRegistrar.java
public static void registerGuildCommands(JDA jda, String guildId) {
    Guild guild = jda.getGuildById(guildId);
    if (guild != null) {
        guild.updateCommands().addCommands(
            // samme kommandoer som før
        ).queue();
    }
}
```

**Guild commands synkroniserer øjeblikkeligt!**

## ✅ **Verification Checklist**

Før du rapporterer bugs, tjek:

- [ ] Bot kører den nyeste kode
- [ ] Kommandoerne er i både Registrar og Handler
- [ ] Discord er genstartet
- [ ] Ventet mindst 5 minutter efter bot restart
- [ ] Bot har korrekte permissions på serveren
- [ ] `/listcommands` viser de nye kommandoer

## 🆘 **Stadig Problemer?**

Hvis kommandoerne stadig ikke virker:

1. **Check bot logs** for fejlmeddelelser
2. **Test med guild commands** for øjeblikkelig sync
3. **Verify bot token** og permissions
4. **Check Discord API status** på Discord Developer Portal

## 📝 **Quick Fix Commands**

**Build og restart:**
```bash
mvn clean compile exec:java -Dexec.mainClass="com.axion.bot.AxionBot"
```

**Check specific command:**
```bash
# I Discord
/listcommands
# Look for invite, support, about
```

**Force immediate sync (guild only):**
```java
// Add to SlashCommandRegistrar.java for testing
public static void registerTestGuildCommands(JDA jda, String testGuildId) {
    jda.getGuildById(testGuildId).updateCommands().addCommands(
        Commands.slash("invite", "Test invite command"),
        Commands.slash("support", "Test support command"),
        Commands.slash("about", "Test about command")
    ).queue();
}
```

## 🎯 **Expected Timeline**

- **Code changes:** Øjeblikkelig (efter restart)
- **Guild commands:** 0-5 sekunder
- **Global commands:** 1-60 minutter
- **Discord cache:** Op til 10 minutter
