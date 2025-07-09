# 🛡️ Advanced Moderation System Guide

Denne guide beskriver det avancerede moderation system i Axion Discord Bot med alle nye funktioner og forbedringer.

## 📋 Indholdsfortegnelse

1. [Oversigt](#oversigt)
2. [Nye Funktioner](#nye-funktioner)
3. [Kommandoer](#kommandoer)
4. [Konfiguration](#konfiguration)
5. [Auto-Moderation](#auto-moderation)
6. [Dashboard & Statistikker](#dashboard--statistikker)
7. [Appeal System](#appeal-system)
8. [Bedste Praksis](#bedste-praksis)

## 🎯 Oversigt

Det avancerede moderation system tilbyder:

- **Intelligent Spam Detection** - Avanceret spam detection med mønstergenkendelse
- **Progressiv Eskalering** - Automatisk eskalering baseret på brugerens overtrædelseshistorik
- **Midlertidige Bans** - Fleksible midlertidige bans med automatisk ophævelse
- **Detaljeret Logging** - Omfattende logging af alle moderation handlinger
- **Appeal System** - Brugere kan appellere moderation beslutninger
- **Real-time Dashboard** - Live statistikker og system sundhed
- **Vedhæftning Scanning** - Automatisk scanning af mistænkelige filer

## 🆕 Nye Funktioner

### 🔄 Progressiv Eskalering
Systemet husker brugerens tidligere overtrædelser og eskalerer straffe automatisk:
- **1. overtrædelse**: Advarsel
- **2. overtrædelse**: Besked sletning + advarsel
- **3. overtrædelse**: 1 times timeout
- **4. overtrædelse**: 6 timers timeout
- **5+ overtrædelser**: Midlertidig ban (1-7 dage)

### ⏰ Midlertidige Bans
- Automatisk ophævelse efter udløb
- Fleksibel varighed (1 time til 7 dage)
- Tracking af aktive bans
- Notifikationer ved udløb

### 📊 Avanceret Spam Detection
- **Identiske beskeder**: Detecterer gentagne beskeder
- **Hurtig typing**: Identificerer unormalt hurtig besked frekvens
- **Mønster genkendelse**: Finder spam mønstre i tekst
- **Koordineret spam**: Detecterer spam på tværs af kanaler

### 📎 Vedhæftning Moderation
- **Mistænkelige filtyper**: Blokerer potentielt farlige filer (.exe, .bat, etc.)
- **Størrelse begrænsninger**: Kontrollerer fil størrelse
- **Indhold scanning**: Analyserer vedhæftninger for malware signaturer

### 📈 Real-time Dashboard
- **System status**: Live status af alle moderation komponenter
- **Statistikker**: Detaljerede metrics og trends
- **Sundhed monitoring**: System sundhed og performance
- **Aktive handlinger**: Oversigt over igangværende moderation

## 🎮 Kommandoer

### Hovedkommandoer

#### `/moderation dashboard [type]`
Viser moderation dashboard med forskellige visninger:
- `overview` - Generel oversigt (standard)
- `stats` - Detaljerede statistikker
- `health` - System sundhed

```
/moderation dashboard type:overview
```

#### `/moderation stats`
Viser detaljerede moderation statistikker:
- Antal sporede brugere
- Aktive overtrædelser
- Moderation handlinger
- Effektivitetsrater

#### `/moderation health`
Viser system sundhed og anbefalinger:
- Sundhedsscore (0-100)
- Performance indikatorer
- System anbefalinger

#### `/moderation tempbans`
Viser alle aktive midlertidige bans:
- Bruger information
- Udløbstid
- Årsag til ban

#### `/moderation logs [limit]`
Viser seneste moderation handlinger:
- Konfigurerbar antal (standard: 10)
- Detaljerede log entries
- Tidsstempler og årsager

### Bruger Kommandoer

#### `/user tempban <user> <hours> [reason]`
Midlertidigt banner en bruger:
```
/user tempban user:@ProblematicUser hours:24 reason:Spam i #general
```

#### `/user violations <user>`
Viser brugerens overtrædelseshistorik:
```
/user violations user:@SomeUser
```

#### `/user reset <user>`
Nulstiller brugerens overtrædelser:
```
/user reset user:@ReformedUser
```

#### `/user history <user>`
Viser brugerens komplette moderation historik:
```
/user history user:@SomeUser
```

### Appeal System

#### `/appeal submit <reason>`
Indsender en appel for moderation handling:
```
/appeal submit reason:Jeg blev fejlagtigt bannet for spam
```

#### `/appeal review <user> <decision> [notes]`
Behandler en brugers appel (kun moderatorer):
```
/appeal review user:@AppealingUser decision:approve notes:Fejlagtig automatisk handling
```

#### `/appeal list`
Viser alle ventende appeals (kun moderatorer)

### Konfiguration

#### `/moderation config [setting] [value]`
Viser eller ændrer konfiguration:
```
/moderation config
/moderation config setting:max_messages_per_minute value:5
```

## ⚙️ Konfiguration

### Grundlæggende Indstillinger

```java
// Opret standard konfiguration
ModerationConfig config = ModerationConfig.createDefault();

// Eller streng konfiguration
ModerationConfig strictConfig = ModerationConfig.createStrict();

// Eller mild konfiguration
ModerationConfig mildConfig = ModerationConfig.createMild();
```

### Avancerede Indstillinger

```java
// Aktiver avancerede funktioner
config.setAdvancedSpamDetectionEnabled(true);
config.setAttachmentScanningEnabled(true);
config.setEscalationEnabled(true);
config.setTempBanEnabled(true);

// Konfigurer tidsgrænser
config.setMaxTempBanHours(168); // 7 dage
config.setViolationDecayHours(24); // Overtrædelser forsvinder efter 24 timer

// Detaljeret logging
config.setDetailedLogging(true);

// Tilføj trusted domæner
config.addTrustedDomain("youtube.com");
config.addTrustedDomain("github.com");
```

### Konfigurationsniveauer

#### Standard (Balanced)
- Moderat spam beskyttelse
- Grundlæggende toxic content detection
- Tillader 5 beskeder per minut
- 3 advarsler før ban
- Avancerede funktioner aktiveret

#### Streng (Strict)
- Aggressiv spam beskyttelse
- Streng toxic content detection
- Tillader kun 3 beskeder per minut
- 2 advarsler før ban
- Alle avancerede funktioner aktiveret
- Detaljeret logging

#### Mild (Lenient)
- Mild spam beskyttelse
- Basis toxic content detection
- Tillader 8 beskeder per minut
- 5 advarsler før ban
- Avancerede funktioner deaktiveret

## 🤖 Auto-Moderation

### Spam Detection

Systemet detecterer automatisk:
- **Besked frekvens**: For mange beskeder på kort tid
- **Identiske beskeder**: Gentagne eller næsten identiske beskeder
- **Spam mønstre**: Tekst mønstre der indikerer spam
- **Hurtig typing**: Unormalt hurtig besked frekvens
- **Koordineret spam**: Spam på tværs af flere kanaler

### Toxic Content Detection

- **Bannede ord**: Automatisk detection af upassende sprog
- **Mistænkelige mønstre**: Genkendelse af problematiske tekstmønstre
- **Trusler og chikane**: Detection af trusler og chikanerende adfærd
- **Personlige oplysninger**: Beskyttelse mod deling af private data

### Link Protection

- **Mistænkelige domæner**: Blokering af kendte malware/phishing sites
- **Discord invites**: Kontrol af Discord server invitations
- **Link spam**: Detection af overdreven link deling
- **Trusted domæner**: Whitelist af sikre domæner

### Attachment Scanning

- **Farlige filtyper**: Automatisk blokering af .exe, .bat, .scr, etc.
- **Størrelse kontrol**: Begrænsning af fil størrelse
- **Malware scanning**: Grundlæggende malware detection
- **Indhold analyse**: Scanning af fil indhold for mistænkelige signaturer

## 📊 Dashboard & Statistikker

### System Dashboard

Dashboardet viser:
- **System status**: Status for alle moderation komponenter
- **Live statistikker**: Real-time metrics og trends
- **Aktive handlinger**: Igangværende moderation aktiviteter
- **Konfiguration**: Nuværende system indstillinger

### Detaljerede Statistikker

- **Bruger metrics**: Antal sporede brugere og overtrædelser
- **Moderation handlinger**: Total antal og typer af handlinger
- **Effektivitetsrater**: System performance og effektivitet
- **Trend analyse**: Historiske data og mønstre

### System Sundhed

Sundhedsmonitoring inkluderer:
- **Sundhedsscore**: Samlet system sundhed (0-100)
- **Performance indikatorer**: Key metrics for system performance
- **Anbefalinger**: Automatiske forslag til forbedringer
- **Alerts**: Advarsler om potentielle problemer

## 📝 Appeal System

### For Brugere

1. **Indsend Appeal**: Brug `/appeal submit` med en detaljeret forklaring
2. **Vent på behandling**: Appeals behandles af moderatorer
3. **Modtag svar**: Få besked om beslutningen

### For Moderatorer

1. **Se ventende appeals**: Brug `/appeal list`
2. **Behandl appeals**: Brug `/appeal review` til at godkende/afvise
3. **Tilføj noter**: Inkluder forklaring for beslutningen

### Appeal Proces

- **Automatisk logging**: Alle appeals logges automatisk
- **Tidsstempler**: Præcis tracking af indsendelse og behandling
- **Notifikationer**: Automatiske notifikationer til brugere
- **Historik**: Komplet historik af alle appeals

## 🎯 Bedste Praksis

### Konfiguration

1. **Start med standard**: Brug standard konfiguration og juster efter behov
2. **Monitor performance**: Hold øje med system sundhed og statistikker
3. **Juster gradvist**: Lav små ændringer og observer effekten
4. **Test indstillinger**: Test nye indstillinger i en test-server først

### Moderation

1. **Brug progressiv eskalering**: Lad systemet håndtere gentagne overtrædere
2. **Behandl appeals hurtigt**: Hurtig behandling forbedrer brugeroplevelsen
3. **Dokumenter beslutninger**: Tilføj altid noter til manuelle handlinger
4. **Monitor trends**: Hold øje med mønstre i overtrædelser

### Vedligeholdelse

1. **Regelmæssig gennemgang**: Gennemgå logs og statistikker regelmæssigt
2. **Opdater konfiguration**: Juster indstillinger baseret på server aktivitet
3. **Rens gamle data**: Fjern gamle logs og overtrædelser periodisk
4. **Backup konfiguration**: Gem backup af vigtige konfigurationer

## 🔧 Fejlfinding

### Almindelige Problemer

**Problem**: For mange false positives
**Løsning**: Reducer følsomhed i konfiguration eller tilføj whitelisted brugere

**Problem**: Systemet er for langsomt
**Løsning**: Deaktiver avancerede funktioner eller øg tærskelværdier

**Problem**: Appeals behandles ikke
**Løsning**: Tjek moderator permissions og notifikations indstillinger

### Performance Optimering

1. **Juster tærskelværdier**: Øg værdier for at reducere CPU belastning
2. **Begræns logging**: Deaktiver detaljeret logging hvis ikke nødvendigt
3. **Rens gamle data**: Fjern gamle logs og statistikker regelmæssigt
4. **Monitor memory**: Hold øje med memory forbrug ved høj aktivitet

## 📞 Support

For hjælp med moderation systemet:
1. Tjek denne guide først
2. Se system sundhed for automatiske anbefalinger
3. Kontakt server administratorer
4. Rapporter bugs via GitHub issues

---

*Dette moderation system er designet til at være kraftfuldt men brugervenligt. Start med standard indstillinger og tilpas efter din servers behov.*