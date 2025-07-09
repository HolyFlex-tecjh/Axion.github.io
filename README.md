# Axion Bot 🛡️

**En avanceret Discord bot med kraftfuld auto-moderation og omfattende server management**

Axion Bot er en professionel Discord bot udviklet i Java med JDA (Java Discord API), designet til at levere robust auto-moderation, omfattende server management og en intuitiv brugeroplevelse. Botten kombinerer avancerede algoritmer med brugervenlige kommandoer for at sikre en sikker og velorganiseret Discord server.

## 🎯 Hvad kan Axion Bot?

Axion Bot er bygget til at være din servers ultimative værktøj til:

- **🛡️ Automatisk beskyttelse** - Intelligent spam detection, toxic content filtering og raid protection
- **⚖️ Professionel moderation** - Komplet suite af moderation værktøjer med detaljeret logging
- **🌍 Flersproget support** - Understøtter 40+ sprog med lokaliserede beskeder
- **📊 Detaljerede statistikker** - Omfattende tracking af server aktivitet og moderation metrics
- **🎮 Brugervenlig interface** - Moderne slash commands med intuitiv navigation
- **🔧 Konfigurerbar experience** - Tilpasselige moderation niveauer og custom filters

## 🚀 Hovedfunktioner

### 🛡️ Intelligent Auto-Moderation System
Axion Bot's auto-moderation system bruger avancerede algoritmer til at beskytte din server:

- **🚨 Spam Detection** - Real-time detektering af message spam, emoji spam og mention spam
- **🔍 Toxic Content Filter** - AI-drevet filtrering af upassende indhold, fornærmelser og trusler
- **🔗 Link Protection** - Automatisk scanning og blokering af mistænkelige URLs og phishing links
- **🎭 Custom Filters** - Brugerdefinerede ord- og mønsterfiltre med regex support
- **⚡ Auto-Ban System** - Intelligent eskalering: Advarsel → Timeout → Kick → Ban
- **🛡️ Raid Protection** - Beskyttelse mod koordinerede angreb og mass-joins

### 🔧 Professionelle Moderation Værktøjer
Komplet suite af moderation kommandoer for server administratorer:

- **⚖️ Ban/Kick/Timeout** - Komplet bruger moderation med grund logging
- **⚠️ Warn System** - Avanceret advarselssystem med persistent tracking
- **🧹 Message Purge** - Hurtig og sikker sletning af beskeder (1-100 ad gangen)
- **📊 Moderation Stats** - Detaljerede statistikker og performance metrics
- **🎛️ Konfigurerbare Niveauer** - Mild, Standard, eller Strict moderation
- **📝 Audit Logging** - Komplet logging af alle moderation handlinger

### 🌍 Flersproget Support
Axion Bot understøtter 40+ sprog med fuldt lokaliserede beskeder:

- **🗣️ Automatisk Sprog Detection** - Intelligent detektering af brugerens foretrukne sprog
- **📱 Lokaliserede Kommandoer** - Alle kommandoer og svar tilgængelige på brugerens sprog
- **🔄 Dynamic Language Switching** - Brugere kan skifte sprog på farten
- **🌐 Regional Customization** - Tilpasset til regionale normer og kulturelle forskelle

### 🤖 Avancerede Funktioner
- **💬 Slash Commands** - Moderne Discord slash kommando system med auto-completion
- **⚡ Ping & Latency** - Real-time performance monitoring og diagnostik
- **🎉 Velkommen System** - Personaliserede velkomstbeskeder til nye medlemmer
- **📊 Server Analytics** - Detaljeret analyse af server aktivitet og trends
- **🔧 Developer Tools** - Avancerede værktøjer for bot udviklere og server ejere
- **⏰ Tidsplanlægning** - Automatiserede opgaver og påmindelser

## � Tekniske Specifikationer

### 🔧 Bygget med
- **Java 17+** - Moderne, performant og sikker
- **JDA (Java Discord API)** - Officiel Discord integration
- **Maven** - Dependency management og build automation
- **SLF4J** - Professionel logging framework
- **JSON** - Konfiguration og data serialization

### 🌐 Understøttede Sprog
Axion Bot understøtter fuldt lokaliserede beskeder på 40+ sprog:
- **Europæiske**: Dansk, Engelsk, Fransk, Tysk, Spansk, Italiensk, Nederlandsk, Polsk, Russisk, Svensk, Norsk, Finsk, Tjekkisk, Slovakisk, Ungarsk, Rumænsk, Bulgarsk, Kroatisk, Serbisk, Slovensk, Græsk, Ukrainsk
- **Asiatiske**: Kinesisk, Japansk, Koreansk, Hindi, Bengali, Thai, Vietnamesisk, Indonesisk, Malaiisk, Tamil, Telugu, Kannada, Malayalam, Gujarati, Marathi, Punjabi, Urdu
- **Mellemøstlige**: Arabisk, Hebraisk, Persisk
- **Andre**: Swahili, Nepalesisk, Sinhala, Birmansk, Mongolsk, Tibetansk, Khmer, Lao

### ⚡ Performance
- **Hurtig respons tid** - Optimeret til sub-100ms kommando respons
- **Lav memory footprint** - Effektiv ressource håndtering
- **Høj oppetid** - Robust error handling og auto-recovery
- **Skalerbar arkitektur** - Understøtter tusindvis af samtidige brugere

## �📋 Krav og Forudsætninger

### 🖥️ System Krav
- **Java 17** eller nyere (LTS anbefalet)
- **Maven 3.6+** til build management
- **4GB RAM** minimum (8GB anbefalet)
- **Stabil internetforbindelse** (minimum 10 Mbps)

### 🔑 Discord Setup
- **Discord bot token** fra Discord Developer Portal
- **Bot permissions** konfigureret korrekt
- **Server Boost** (anbefalet for optimal performance)

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

## 🤖 Kommando Reference

### 🔰 Grundlæggende Kommandoer
Disse kommandoer er tilgængelige for alle brugere:

| Kommando | Beskrivelse | Eksempel |
|----------|-------------|----------|
| `/ping` | Test bot respons tid og server latency | `/ping` |
| `/hello` | Få en personlig hilsen fra botten | `/hello` |
| `/info` | Vis detaljeret information om botten | `/info` |
| `/time` | Vis nuværende tid og dato | `/time` |
| `/help` | Komplet oversigt over alle kommandoer | `/help` |
| `/modhelp` | Se alle moderation kommandoer | `/modhelp` |

### 🛡️ Moderation Kommandoer
Disse kommandoer kræver moderation permissions:

#### 👮‍♂️ Bruger Management
| Kommando | Beskrivelse | Permissions | Eksempel |
|----------|-------------|-------------|----------|
| `/ban` | Ban en bruger permanent fra serveren | `Ban Members` | `/ban user:@user reason:Spam` |
| `/kick` | Kick en bruger fra serveren | `Kick Members` | `/kick user:@user reason:Toxic behavior` |
| `/timeout` | Giv bruger timeout (1-28 dage) | `Moderate Members` | `/timeout user:@user duration:60 reason:Spam` |
| `/warn` | Giv bruger en officiel advarsel | `Manage Messages` | `/warn user:@user reason:Inappropriate content` |
| `/unwarn` | Fjern alle advarsler fra bruger | `Manage Messages` | `/unwarn user:@user` |
| `/warnings` | Vis antal advarsler for bruger | `Manage Messages` | `/warnings user:@user` |

#### 🧹 Message Management
| Kommando | Beskrivelse | Permissions | Eksempel |
|----------|-------------|-------------|----------|
| `/purge` | Slet 1-100 beskeder i kanal | `Manage Messages` | `/purge amount:50` |

#### ⚙️ Moderation Konfiguration
| Kommando | Beskrivelse | Permissions | Eksempel |
|----------|-------------|-------------|----------|
| `/modconfig` | Sæt moderation niveau | `Administrator` | `/modconfig level:strict` |
| `/modstats` | Vis moderation statistikker | `Manage Messages` | `/modstats` |
| `/addfilter` | Tilføj ord til custom filter | `Administrator` | `/addfilter word:badword` |

### 👨‍💻 Udvikler Kommandoer
Kun tilgængelige for registrerede udviklere:

| Kommando | Beskrivelse | Adgang |
|----------|-------------|---------|
| `/devinfo` | Vis detaljeret bot information og udvikler detaljer | Owner + Developers |
| `/devstats` | Vis avancerede bot statistikker og performance metrics | Owner + Developers |

### 🤖 Auto-Moderation Features
Botten overvåger automatisk alle beskeder 24/7 for:

- **📊 Spam Detection**: Detekterer message flooding, emoji spam og mention spam
- **🔍 Toxic Content**: AI-drevet filtrering af upassende indhold og trusler
- **🔗 Link Protection**: Automatisk scanning af mistænkelige URLs og phishing links
- **📝 Custom Filters**: Brugerdefinerede ord og mønstre med regex support
- **⚡ Smart Escalation**: Automatisk progression fra advarsel til ban baseret på sværhedsgrad

#### 🎛️ Moderation Niveauer
- **Mild**: Lempelig approach, fokus på advarsler
- **Standard**: Balanced approach, timeout for gentagne overtrædelser
- **Strict**: Nul tolerance, hurtig eskalering til ban

**📖 For detaljeret dokumentation, se [MODERATION_GUIDE.md](MODERATION_GUIDE.md)**

## 📁 Projektstruktur

```
Axion-Bot/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/axion/
│   │   │       ├── AxionBot.java              # Hovedklasse og bot startup
│   │   │       ├── CommandHandler.java        # Slash kommando håndtering
│   │   │       ├── moderation/
│   │   │       │   ├── AutoModerationSystem.java
│   │   │       │   ├── SpamDetector.java
│   │   │       │   └── ToxicContentFilter.java
│   │   │       ├── commands/
│   │   │       │   ├── BasicCommands.java
│   │   │       │   ├── ModerationCommands.java
│   │   │       │   └── DeveloperCommands.java
│   │   │       └── utils/
│   │   │           ├── DatabaseManager.java
│   │   │           └── LanguageManager.java
│   │   └── resources/
│   │       ├── config.properties              # Bot konfiguration
│   │       └── translations/                  # Sprogoversættelser (40+ sprog)
│   │           ├── en.properties
│   │           ├── da.properties
│   │           └── ...
│   └── test/
│       └── java/
│           └── com/axion/
│               └── AxionBotTest.java           # Unit tests
├── website/                                   # Web dashboard (fremtidige feature)
├── docs/                                      # Dokumentation
│   ├── MODERATION_GUIDE.md
│   ├── SETUP_GUIDE.md
│   └── TROUBLESHOOTING.md
├── pom.xml                                    # Maven konfiguration
└── README.md                                  # Denne fil
```

## � Sikkerhed og Compliance

### 🛡️ Datasikkerhed
- **Ingen gemning af sensitive data** - Axion Bot gemmer aldrig private beskeder eller brugerdata
- **Krypteret kommunikation** - Alle forbindelser bruger TLS/SSL encryption
- **Minimal data collection** - Kun nødvendige moderation metrics gemmes
- **GDPR compliant** - Fuld overholdelse af europæisk databeskyttelse

### 🔐 Permissions og Adgang
- **Principle of least privilege** - Botten anmoder kun om nødvendige permissions
- **Role-based access control** - Forskellige kommandoer kræver specifikke roller
- **Audit logging** - Alle moderation handlinger logges for transparens

### 🌍 Privacy og Etik
- **Transparent moderation** - Alle handlinger logges og kan auditeres
- **No backdoors** - Åben kildekode sikrer ingen skjulte funktioner
- **Respekt for brugerrettigheder** - Fokus på fair og konsistent håndtering

## 🔧 Udvikling og Bidrag

### 🚀 Kom i gang som udvikler

1. **Fork og klon projektet:**
   ```bash
   git clone https://github.com/din-bruger/Axion-Bot.git
   cd Axion-Bot
   ```

2. **Opsæt udviklingsmiljø:**
   ```bash
   mvn clean install
   mvn test
   ```

3. **Opret feature branch:**
   ```bash
   git checkout -b feature/amazing-feature
   ```

### 🛠️ Tilføjelse af nye kommandoer

```java
// Eksempel på tilføjelse af ny kommando
@SlashCommand(name = "minkkommando", description = "Min nye kommando")
public void handleMinKommando(SlashCommandInteractionEvent event) {
    event.reply("Min nye kommando virker perfekt!").setEphemeral(true).queue();
}
```

### 🧪 Test Guidelines
- **Unit tests** for alle nye funktioner
- **Integration tests** for kommando håndtering
- **Performance tests** for auto-moderation algoritmer

```bash
# Kør alle tests
mvn test

# Kør kun unit tests
mvn test -Dtest=*Test

# Kør med coverage report
mvn jacoco:report
```

### 📝 Code Standards
- **Java Code Conventions** - Følg standard Java kodningsstil
- **Javadoc** - Dokumenter alle public methods og classes
- **Error handling** - Robust error handling og logging
- **Security first** - Sikkerhed er første prioritet

## 🤝 Bidrag til Projektet

Vi værdsætter bidrag fra fællesskabet! Her er hvordan du kan hjælpe:

### 🐛 Bug Reports
1. Tjek eksisterende [Issues](https://github.com/din-bruger/Axion-Bot/issues)
2. Opret detaljeret bug report med:
   - Trin til at reproducere
   - Forventet vs faktisk adfærd
   - Java version og OS information
   - Relevante log uddrag

### 💡 Feature Requests
1. Diskuter idéen i [Discussions](https://github.com/din-bruger/Axion-Bot/discussions)
2. Opret feature request med:
   - Detaljeret beskrivelse
   - Use cases og eksempler
   - Mulige implementeringsmetoder

### 🔄 Pull Requests
1. Fork projektet
2. Opret feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit dine ændringer (`git commit -m 'Add: Amazing new feature'`)
4. Push til branch (`git push origin feature/AmazingFeature`)
5. Åbn en Pull Request med detaljeret beskrivelse

### 🏷️ Commit Messages
Vi følger [Conventional Commits](https://www.conventionalcommits.org/):
- `feat:` - Ny funktionalitet
- `fix:` - Bug fix
- `docs:` - Dokumentation ændringer
- `style:` - Code style ændringer
- `refactor:` - Code refactoring
- `test:` - Test ændringer

## � Roadmap og Fremtidige Funktioner

### 🚀 Version 2.0 - Q3 2025
- [ ] **Web Dashboard** - Komplet web-baseret konfiguration og analytics
- [ ] **Database Integration** - Persistent data med PostgreSQL/MySQL support
- [ ] **Machine Learning** - AI-baseret toxic content detection
- [ ] **Webhook Integration** - Custom webhooks for eksterne systemer
- [ ] **API Endpoints** - RESTful API for tredjepartsintegrationer

### 🎵 Version 2.1 - Q4 2025
- [ ] **Musik System** - Komplet musik bot funktionalitet
- [ ] **Voice Channel Management** - Automatisk voice channel moderation
- [ ] **Streaming Integration** - Twitch/YouTube notifikationer
- [ ] **Custom Emoji Reactions** - Intelligent emoji-baserede svar

### 🔮 Version 3.0 - Q1 2026
- [ ] **Multi-Server Management** - Centraliseret administration af flere servere
- [ ] **Advanced Analytics** - Detaljeret brugeradfærd og engagement metrics
- [ ] **Plugin System** - Tredjepartsudviklere kan oprette custom plugins
- [ ] **Mobile App** - Dedicated mobile app til server administration

### ✅ Implementerede Features
- [x] **Slash Commands System** ✅ **IMPLEMENTERET**
- [x] **Auto-Moderation System** ✅ **IMPLEMENTERET**
- [x] **Moderation Commands** ✅ **IMPLEMENTERET**
- [x] **Multi-Language Support** ✅ **IMPLEMENTERET**
- [x] **Developer Commands** ✅ **IMPLEMENTERET**
- [x] **Intelligent Spam Detection** ✅ **IMPLEMENTERET**
- [x] **Toxic Content Filter** ✅ **IMPLEMENTERET**
- [x] **Raid Protection** ✅ **IMPLEMENTERET**

## 🆘 Support og Hjælp

### 🔧 Teknisk Support
Hvis du oplever problemer eller har spørgsmål:

1. **📖 Dokumentation**: Tjek vores omfattende guides:
   - [SETUP_GUIDE.md](SETUP_GUIDE.md) - Komplet setup guide
   - [MODERATION_GUIDE.md](MODERATION_GUIDE.md) - Detaljeret moderation dokumentation
   - [TROUBLESHOOTING.md](TROUBLESHOOTING.md) - Løsninger på almindelige problemer

2. **🐛 Bug Reports**: Opret et [issue](https://github.com/din-bruger/Axion-Bot/issues) med:
   - Detaljeret beskrivelse af problemet
   - Trin til at reproducere
   - Log uddrag (hvis relevant)
   - System information (OS, Java version)

3. **💬 Community**: Join vores Discord server for real-time hjælp
   - [Discord Server](https://discord.gg/axion-bot)
   - Aktive udviklere og community membres
   - Hurtig response tid

### 🏷️ Issue Labels
- `bug` - Fejl i koden
- `enhancement` - Nye funktioner
- `documentation` - Dokumentation forbedringer
- `help wanted` - Hjælp ønskes fra community
- `question` - Spørgsmål til funktionalitet
- `good first issue` - Perfekt for nye bidragydere

## 📞 Kontakt

### � Team
- **Lead Developer**: [GitHub Profile](https://github.com/din-bruger)
- **Community Manager**: [Discord: @community#1234](https://discord.com/users/...)
- **Security Specialist**: [security@axion-bot.com](mailto:security@axion-bot.com)

### 🌐 Links
- **Website**: [https://axion-bot.com](https://axion-bot.com)
- **Documentation**: [https://docs.axion-bot.com](https://docs.axion-bot.com)
- **Status Page**: [https://status.axion-bot.com](https://status.axion-bot.com)
- **Discord Server**: [https://discord.gg/axion-bot](https://discord.gg/axion-bot)

## 📜 Licens

Dette projekt er licenseret under **MIT License** - se [LICENSE](LICENSE) filen for komplette detaljer.

### 🔓 MIT License Resumé
- ✅ **Kommerciel brug** - Fri til at bruge i kommercielle projekter
- ✅ **Modification** - Fri til at modificere og tilpasse
- ✅ **Distribution** - Fri til at distribuere og dele
- ✅ **Private use** - Fri til privat brug
- ❌ **Liability** - Ingen garanti eller ansvar fra udviklerne
- ❌ **Warranty** - Ingen garanti for funktionalitet

## 🙏 Anerkendelser

### 🚀 Teknologier
- **[JDA (Java Discord API)](https://github.com/DV8FromTheWorld/JDA)** - Discord bot framework
- **[SLF4J](https://www.slf4j.org/)** - Logging framework
- **[Maven](https://maven.apache.org/)** - Build automation
- **[Discord.js](https://discord.js.org/)** - Inspiration for kommando struktur

### 💝 Bidragydere
Tak til alle der har bidraget til Axion Bot:
- [Alle bidragydere](https://github.com/din-bruger/Axion-Bot/graphs/contributors)
- Community feedback og bug reports
- Beta testers og tidlige adoptere

### 🌟 Speciel Tak
- **Discord Developer Community** - For support og vejledning
- **OpenAI** - For AI-baserede moderationsalgoritmer
- **Java Community** - For fantastiske open source libraries

---

<div align="center">

**Axion Bot** - *Sikker, professionel Discord server management*

[![Made with Java](https://img.shields.io/badge/Made%20with-Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)](https://java.com)
[![Discord](https://img.shields.io/badge/Discord-7289DA?style=for-the-badge&logo=discord&logoColor=white)](https://discord.gg/axion-bot)
[![MIT License](https://img.shields.io/badge/License-MIT-green.svg?style=for-the-badge)](LICENSE)
[![GitHub Stars](https://img.shields.io/github/stars/din-bruger/Axion-Bot?style=for-the-badge)](https://github.com/din-bruger/Axion-Bot/stargazers)

**Udviklet med ❤️ af Discord Community**

*Axion Bot version 1.0.0 - Opbygning af sikrere Discord fællesskaber*

</div>