# 🤖 Axion Bot - Setup Guide

Denne guide hjælper dig med at sætte Axion Bot op korrekt og konfigurere invite links.

## 📋 **Forudsætninger**

- Java 17 eller nyere
- Discord Developer Account
- Discord Server med Administrator tilladelser

## 🔧 **Setup Proces**

### **1. Discord Bot Setup**

1. **Opret Discord Application:**
   - Gå til [Discord Developer Portal](https://discord.com/developers/applications)
   - Klik "New Application"
   - Navngiv din application "Axion Bot"

2. **Opret Bot:**
   - Gå til "Bot" tab
   - Klik "Add Bot"
   - Kopier Bot Token (gem det sikkert!)

3. **Konfigurer Bot:**
   - Enable "Message Content Intent" (hvis nødvendigt)
   - Disable "Public Bot" (hvis du vil holde den privat)

### **2. Konfigurer Invite Links**

1. **Få Bot Client ID:**
   - Gå til "General Information" tab
   - Kopier "Application ID" (Client ID)

2. **Opdater BotConfig.java:**
   ```java
   public static final String INVITE_URL = "https://discord.com/api/oauth2/authorize?client_id=DIN_BOT_ID&permissions=1375845727494&scope=bot%20applications.commands";
   ```
   
   Erstat `DIN_BOT_ID` med din bot's Client ID.

### **3. Permissions Calculator**

De nødvendige permissions for Axion Bot:

| Permission | Værdi | Grund |
|------------|-------|-------|
| Administrator | ✅ | Fuld adgang (anbefalet) |
| Moderate Members | ✅ | Timeout funktioner |
| Kick Members | ✅ | Kick kommando |
| Ban Members | ✅ | Ban kommando |
| Manage Messages | ✅ | Purge kommando |
| Send Messages | ✅ | Bot svar |
| Use Slash Commands | ✅ | Slash kommandoer |
| Read Message History | ✅ | Purge funktionalitet |
| View Channels | ✅ | Basis funktionalitet |

**Total Permission Value:** `1375845727494`

### **4. Environment Setup**

1. **Opret config.properties:**
   ```properties
   bot.token=DIN_BOT_TOKEN
   bot.invite.url=https://discord.com/api/oauth2/authorize?client_id=DIN_BOT_ID&permissions=1375845727494&scope=bot%20applications.commands
   support.server=https://discord.gg/DIN_INVITE_CODE
   ```

2. **Environment Variables (alternativ):**
   ```bash
   export DISCORD_BOT_TOKEN="din_bot_token"
   export BOT_CLIENT_ID="din_client_id"
   ```

### **5. Support Server Setup**

1. **Opret Support Server:**
   - Opret en ny Discord server
   - Konfigurer channels (#support, #announcements, #bot-testing)
   - Inviter din bot

2. **Få Invite Link:**
   - Højreklik på server navn
   - "Invite People"
   - "Edit invite link"
   - Sæt til "Never expire"
   - Kopier link

3. **Opdater Konfiguration:**
   ```java
   public static final String SUPPORT_SERVER = "https://discord.gg/DIN_INVITE_CODE";
   ```

## 🚀 **Deploy Process**

### **1. Local Testing**
```bash
# Kompiler projekt
mvn clean compile

# Kør tests
mvn test

# Start bot
mvn exec:java -Dexec.mainClass="com.axion.bot.AxionBot"
```

### **2. Production Deploy**
```bash
# Build JAR
mvn clean package

# Kør production
java -jar target/axion-bot-1.0.0.jar
```

## ✅ **Verifikation Checklist**

- [ ] Bot token konfigureret korrekt
- [ ] Invite URL opdateret med korrekt Client ID
- [ ] Support server oprettet og konfigureret
- [ ] Permissions testet på test server
- [ ] Alle slash kommandoer registreret
- [ ] `/invite` kommando returnerer korrekt link
- [ ] `/support` kommando peger på rigtig server
- [ ] `/about` kommando viser korrekte statistikker

## 🔧 **Maintenance**

### **Opdater Invite Links:**
Hvis du ændrer permissions:
1. Brug [Discord Permissions Calculator](https://discordapi.com/permissions.html)
2. Opdater `INVITE_PERMISSIONS` i BotConfig.java
3. Regenerer invite URL
4. Test på test server først

### **Monitor Bot Health:**
- Check logs for fejl
- Monitor memory usage
- Verificer slash kommando synkronisering
- Test invite links regelmæssigt

## 📞 **Support**

Hvis du støder på problemer:
1. Check Discord Developer Portal for fejl
2. Verificer bot permissions på server
3. Test invite link i incognito browser
4. Check bot logs for detaljerede fejlmeddelelser

## 🎉 **Success!**

Når alt er konfigureret korrekt, kan brugere:
- Bruge `/invite` for at få bot invite link
- Bruge `/support` for at joine support server
- Bruge `/about` for detaljeret bot information
- Invitere botten til nye servere med alle nødvendige permissions!
