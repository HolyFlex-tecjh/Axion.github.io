# Axion Bot 🛡️

**En kraftfuld Discord bot med avanceret auto-moderation system**

En grundlæggende Discord bot udviklet i Java med JDA (Java Discord API).

## 🚀 Funktioner

### 🛡️ Auto-Moderation System
- **Spam Detection** - Automatisk detektering og blokering af spam
- **Toxic Content Filter** - Intelligent filtrering af upassende indhold
- **Link Protection** - Beskyttelse mod link spam og mistænkelige URLs
- **Custom Filters** - Brugerdefinerede ord og mønstre
- **Auto-Ban System** - Automatisk eskalering fra advarsel til ban
- **Raid Protection** - Beskyttelse mod koordinerede angreb

### 🔧 Moderation Kommandoer
- **Ban/Kick/Timeout** - Komplet bruger moderation
- **Warn System** - Advarselssystem med tracking
- **Message Purge** - Hurtig sletning af beskeder
- **Moderation Stats** - Detaljerede statistikker

### 🤖 Grundlæggende Funktioner
- **Slash Commands** - Moderne Discord slash kommando system
- **Ping kommando** - Teste bot respons tid (`/ping`)
- **Hilsen kommandoer** - Venlig velkomst til nye brugere (`/hello`)
- **Info kommando** - Vis bot information og statistikker (`/info`)
- **Hjælp kommando** - Komplet kommando oversigt (`/help`)
- **Tid kommando** - Vis nuværende tid (`/time`)

## 📋 Forudsætninger

- Java 17 eller nyere
- Maven 3.6 eller nyere
- Discord bot token

### Bot Permissions
Sørg for at din bot har følgende tilladelser:
- `Send Messages`
- `Manage Messages` (for auto-moderation)
- `Moderate Members` (for timeouts)
- `Kick Members` (for kick kommandoer)
- `Ban Members` (for ban kommandoer)
- `View Audit Log` (for logging)

## 🛠️ Installation

1. **Klon projektet:**
   ```bash
   git clone https://github.com/din-bruger/Axion.git
   cd Axion
   ```

2. **Konfigurer din Discord bot:**
   - Gå til [Discord Developer Portal](https://discord.com/developers/applications)
   - Opret en ny applikation og bot
   - Kopier bot tokenet
   - Tilføj tokenet til `src/main/resources/config.properties`:
     ```properties
     discord.token=DIT_DISCORD_TOKEN_HER
     bot.owner.id=DIN_DISCORD_USER_ID
     bot.developers.ids=DEVELOPER_ID_1,DEVELOPER_ID_2,DEVELOPER_ID_3
     ```

### 👥 Konfiguration af Udviklere

Axion Bot understøtter flere udviklere med specielle tilladelser:

1. **Bot Ejer (Owner):** Den primære ejer af botten
2. **Udviklere:** Brugere med adgang til udvikler kommandoer

**Sådan finder du Discord User IDs:**
1. Aktiver Developer Mode i Discord (User Settings → Advanced → Developer Mode)
2. Højreklik på en bruger og vælg "Copy ID"

**Konfiguration i config.properties:**
```properties
# Bot ejer (kun én person)
bot.owner.id=123456789012345678

# Udviklere (kommasepareret liste)
bot.developers.ids=123456789012345678,987654321098765432,456789012345678901
```

**Udvikler Kommandoer:**
- `/devinfo` - Vis bot information og udvikler detaljer
- `/devstats` - Detaljerede bot statistikker og performance metrics

**Bemærk:** Kun registrerede udviklere kan bruge disse kommandoer.

3. **Byg projektet:**
   ```bash
   mvn clean package
   ```

4. **Kør botten:**
   ```bash
   java -jar target/axion-bot-1.0.0.jar
   ```

## 🤖 Kommandoer

### Grundlæggende Slash Kommandoer

- `/ping` - Test om botten svarer
- `/hello` - Få en hilsen fra botten
- `/info` - Vis information om botten
- `/time` - Vis nuværende tid
- `/help` - Vis alle kommandoer
- `/modhelp` - Se moderation kommandoer

### 🛡️ Moderation Slash Kommandoer
- `/ban user:[bruger] reason:[årsag]` - Ban en bruger permanent
- `/kick user:[bruger] reason:[årsag]` - Kick en bruger fra serveren
- `/timeout user:[bruger] duration:[minutter] reason:[årsag]` - Giv bruger timeout
- `/warn user:[bruger] reason:[årsag]` - Giv bruger en advarsel
- `/unwarn user:[bruger]` - Fjern alle advarsler fra bruger
- `/warnings user:[bruger]` - Vis antal advarsler for bruger
- `/purge amount:[antal]` - Slet et antal beskeder (1-100)
- `/modconfig level:[niveau]` - Sæt moderation niveau (mild/standard/strict)
- `/modstats` - Vis moderation statistikker
- `/addfilter word:[ord]` - Tilføj ord til custom filter
- `/modhelp` - Vis alle moderation kommandoer

### 👨‍💻 Udvikler Kommandoer (Kun for registrerede udviklere)
- `/devinfo` - Vis detaljeret bot information og udvikler detaljer
- `/devstats` - Vis avancerede bot statistikker og performance metrics

### 🤖 Auto-Moderation
Botten overvåger automatisk alle beskeder for:
- Spam (for mange beskeder på kort tid)
- Toxic indhold (upassende sprog)
- Link spam (for mange links)
- Custom filters (brugerdefinerede ord)

**Se [MODERATION_GUIDE.md](MODERATION_GUIDE.md) for detaljeret dokumentation**

## 📁 Projektstruktur

```
src/
├── main/
│   ├── java/
│   │   └── com/axion/bot/
│   │       ├── AxionBot.java          # Hovedklasse
│   │       └── CommandHandler.java    # Kommando håndtering
│   └── resources/
│       └── config.properties          # Konfigurationsfil
└── test/
    └── java/
        └── com/axion/bot/
            └── AxionBotTest.java       # Test klasse
```

## 🔧 Udvikling

### Tilføjelse af nye kommandoer

1. Åbn `CommandHandler.java`
2. Tilføj din nye kommando til `switch` statement i `onMessageReceived` metoden
3. Opret en ny metode til at håndtere kommandoen
4. Opdater hjælp kommandoen med den nye kommando

Eksempel:
```java
case "minkkommando":
    handleMinKommando(event);
    break;

private void handleMinKommando(MessageReceivedEvent event) {
    event.getChannel().sendMessage("Min nye kommando virker!").queue();
}
```

### Kørsel af tests

```bash
mvn test
```

### Logging

Botten bruger SLF4J til logging. Log niveau kan ændres i `config.properties`.

## 🤝 Bidrag

1. Fork projektet
2. Opret en feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit dine ændringer (`git commit -m 'Add some AmazingFeature'`)
4. Push til branch (`git push origin feature/AmazingFeature`)
5. Åbn en Pull Request

## 📝 Licens

Dette projekt er licenseret under MIT License - se [LICENSE](LICENSE) filen for detaljer.

## 🆘 Support

Hvis du har problemer eller spørgsmål:
1. Tjek [Issues](https://github.com/din-bruger/Axion/issues) siden
2. Opret et nyt issue hvis dit problem ikke allerede er rapporteret
3. Tag gerne "help wanted" eller "question" labels

## 🔮 Fremtidige funktioner

- [ ] Slash kommandoer support
- [ ] Database integration for persistent moderation data
- [ ] Musik funktionalitet
- [x] **Moderation kommandoer** ✅ **IMPLEMENTERET**
- [x] **Auto-moderation system** ✅ **IMPLEMENTERET**
- [ ] Web dashboard for konfiguration
- [ ] Webhook integration
- [ ] Custom emoji reactions
- [ ] Machine learning baseret toxic detection

---

**Udviklet med ❤️ i Java**