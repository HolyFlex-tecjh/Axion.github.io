# 🛡️ Axion Bot Moderation System

Axion Bot kommer nu med et kraftfuldt og intelligent moderation system, der automatisk beskytter din Discord server mod spam, toxic indhold, og andre uønskede aktiviteter.

## 🚀 Funktioner

### Auto-Moderation
- **Spam Detection**: Automatisk detektering af spam beskeder baseret på frekvens og indhold
- **Toxic Content Detection**: Intelligent filtrering af upassende sprog og indhold
- **Link Protection**: Beskyttelse mod link spam og mistænkelige URLs
- **Custom Filters**: Mulighed for at tilføje brugerdefinerede ord og mønstre
- **Raid Protection**: Beskyttelse mod koordinerede angreb

### Moderation Kommandoer
- **Ban/Kick**: Fjern problematiske brugere fra serveren
- **Timeout**: Giv brugere midlertidige timeouts
- **Warn System**: Advarselssystem med automatisk eskalering
- **Message Purge**: Hurtig sletning af flere beskeder
- **Konfiguration**: Juster moderation indstillinger

## 📋 Kommando Oversigt

### Bruger Moderation
```
!ban @bruger [årsag]           - Ban en bruger permanent
!kick @bruger [årsag]          - Kick en bruger fra serveren
!timeout @bruger <min> [årsag] - Giv timeout (1-10080 minutter)
!warn @bruger [årsag]          - Giv en advarsel til brugeren
!unwarn @bruger                - Fjern alle advarsler
!warnings @bruger              - Vis antal advarsler
```

### Kanal Moderation
```
!purge <antal>                 - Slet op til 100 beskeder
```

### Konfiguration & Statistikker
```
!modconfig                     - Vis moderation indstillinger
!modstats                      - Vis moderation statistikker
!addfilter <ord/mønster>       - Tilføj custom filter
!modhelp                       - Vis alle moderation kommandoer
```

## ⚙️ Konfiguration

### Standard Indstillinger
Axion Bot kommer med fornuftige standard indstillinger:
- **Spam Protection**: ✅ Aktiveret (max 5 beskeder/minut)
- **Toxic Detection**: ✅ Aktiveret
- **Link Protection**: ✅ Aktiveret (max 2 links/besked)
- **Auto-Timeout**: ✅ Aktiveret
- **Auto-Ban**: ❌ Deaktiveret (kan aktiveres)

### Konfiguration Niveauer

#### Mild Konfiguration (Lenient)
- Færre restriktioner
- Højere tærskel for spam (8 beskeder/minut)
- Kun grundlæggende beskyttelse

#### Standard Konfiguration (Default)
- Balanceret tilgang
- Moderat beskyttelse
- Anbefalet for de fleste servere

#### Streng Konfiguration (Strict)
- Maksimal beskyttelse
- Lavere tærskel for spam (3 beskeder/minut)
- Auto-ban aktiveret

## 🔧 Opsætning

### 1. Bot Permissions
Sørg for at Axion Bot har følgende tilladelser:
- `Manage Messages` - For at slette spam beskeder
- `Moderate Members` - For at give timeouts
- `Kick Members` - For at kicke brugere
- `Ban Members` - For at banne brugere
- `View Audit Log` - For logging

### 2. Rolle Hierarki
Placer Axion Bot's rolle højt nok til at moderere andre brugere, men under administrator roller.

### 3. Kanal Opsætning
Overvej at oprette en dedikeret moderation log kanal for at holde styr på alle handlinger.

## 🛡️ Auto-Moderation Funktioner

### Spam Detection
- **Besked Frekvens**: Detekterer brugere der sender for mange beskeder
- **Identiske Beskeder**: Blokerer gentagne beskeder
- **Eskalering**: Automatisk eskalering fra advarsel til timeout til ban

### Toxic Content Detection
- **Bannede Ord**: Filtrerer upassende sprog
- **Mistænkelige Mønstre**: Detekterer spam mønstre som gentagne tegn
- **Store Bogstaver**: Blokerer excessive brug af store bogstaver

### Link Protection
- **Link Spam**: Begrænser antal links per besked
- **Discord Invites**: Kan blokere Discord invite links
- **Mistænkelige URLs**: Detekterer potentielt farlige links

## 📊 Moderation Workflow

### Automatisk Eskalering
1. **Første Overtrædelse**: Besked slettet + advarsel
2. **Anden Overtrædelse**: Timeout (5 minutter)
3. **Tredje Overtrædelse**: Længere timeout eller kick
4. **Fjerde Overtrædelse**: Ban (hvis aktiveret)

### Manuel Moderation
Moderatorer kan altid gribe ind manuelt med kommandoer for at:
- Give øjeblikkelige bans for alvorlige overtrædelser
- Justere timeout varighed
- Fjerne advarsler for rehabiliterede brugere

## 🔍 Logging & Monitoring

### Automatisk Logging
Alle moderation handlinger logges automatisk med:
- Tidsstempel
- Bruger information
- Årsag til handling
- Moderator (hvis manuel)

### Statistikker
Hold styr på server sundhed med:
- Antal beskeder slettet
- Advarsler givet
- Timeouts og bans
- Spam blokeret

## 🚨 Fejlfinding

### Almindelige Problemer

**Bot kan ikke slette beskeder**
- Tjek at botten har `Manage Messages` tilladelse
- Sørg for at bot rollen er højere end brugerens rolle

**Auto-moderation virker ikke**
- Verificer at alle nødvendige intents er aktiveret
- Tjek bot permissions i kanalen

**Kommandoer virker ikke**
- Sørg for at brugeren har moderation tilladelser
- Tjek at kommandoen er stavet korrekt

### Support
Hvis du oplever problemer:
1. Tjek bot logs for fejlmeddelelser
2. Verificer alle tilladelser og roller
3. Test med `/ping` slash kommandoen først
4. Opret et issue på GitHub hvis problemet fortsætter

## 🔮 Fremtidige Funktioner

- [ ] Database integration for persistent data
- [ ] Web dashboard for konfiguration
- [ ] Machine learning baseret toxic detection
- [ ] Integration med eksterne moderation APIs
- [ ] Automatisk backup af moderation logs
- [ ] Custom punishment workflows
- [ ] Whitelist system for trusted brugere

## 📝 Eksempler

### Grundlæggende Moderation
```
# Ban en bruger for spam
!ban @SpamBot Gentagen spam efter advarsler

# Giv timeout for upassende sprog
!timeout @ToxicUser 30 Upassende sprog i chat

# Slet de sidste 10 beskeder
!purge 10
```

### Konfiguration
```
# Tilføj custom filter
!addfilter badword

# Vis nuværende indstillinger
!modconfig

# Tjek moderation statistikker
!modstats
```

---

**Axion Bot Moderation System** - Beskyt din server med intelligent auto-moderation! 🛡️