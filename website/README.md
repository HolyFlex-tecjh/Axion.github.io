# Axion Bot Website 🚀

En moderne, responsiv og tilgængelig website for Axion Discord Bot med avancerede funktioner og optimeret ydeevne.

## 🌟 Funktioner

### 🎨 Design & UI
- **Moderne glassmorphism design** med gradient baggrunde
- **Responsive layout** der fungerer perfekt på alle enheder
- **Mørk tema** med fuld accessibility support
- **Avancerede animationer** og mikrointeraktioner
- **Particle effects** for visuelt appeal

### ⚡ Performance
- **Service Worker** for caching og offline funktionalitet
- **Lazy loading** af billeder og indhold
- **Optimeret JavaScript** med debouncing og throttling
- **Minificerede assets** for hurtigere load times
- **Progressive Web App** funktionalitet

### 🔧 Tekniske Funktioner
- **Avanceret søgning** med highlighting og relevance scoring
- **Intelligente filtre** for kommandoer og kategorier
- **Favorit system** med localStorage persistering
- **Keyboard shortcuts** for power users
- **Copy-to-clipboard** funktionalitet

### 🌐 Accessibility
- **WCAG 2.1 AA compliant** design
- **Screen reader** optimeret
- **Keyboard navigation** support
- **High contrast mode** support
- **Reduced motion** respekt

## 📁 Fil Struktur

```
website/
├── index.html              # Hovedside
├── commands.html           # Kommando oversigt
├── login.html             # Login side
├── dashboard.html         # Admin dashboard
├── pricing.html           # Priser
├── styles.css             # Basis styles
├── enhanced-styles.css    # Avancerede styles
├── modern-utilities.css   # Moderne utility classes
├── commands.css           # Kommando-specifikke styles
├── enhanced-v2.js         # Forbedret JavaScript
├── commands.js            # Kommando funktionalitet
├── config.js              # Konfiguration
├── sw.js                  # Service Worker
├── assets/
│   ├── axion-logo.svg     # Logo
│   └── favicon.ico        # Favicon
└── README.md              # Dette dokument
```

## 🚀 Installation & Opsætning

### Forudsætninger
- Moderne webbrowser med ES6+ support
- Webserver (Apache, Nginx, eller development server)
- HTTPS for Service Worker funktionalitet (i produktion)

### Trin-for-trin Guide

1. **Klon eller download** filerne til din webserver
2. **Konfigurer** `config.js` med dine specifikke indstillinger
3. **Test** websitet lokalt
4. **Deploy** til produktionsserver

### Lokal Development

```bash
# Brug Python's built-in server
python -m http.server 8000

# Eller Node.js http-server
npx http-server -p 8000

# Åbn i browser
open http://localhost:8000
```

## 🎯 Konfiguration

### config.js
Hovedkonfigurationsfilen indeholder:
- Site information
- Bot statistikker
- API endpoints
- Theme indstillinger
- Kommando kategorier
- Pricing plans

### CSS Variabler
Tilpas udseendet via CSS custom properties i `enhanced-styles.css`:
```css
:root {
    --primary-color: #5865f2;
    --secondary-color: #8b5cf6;
    --accent-color: #00d4aa;
    /* ... flere variabler */
}
```

## 🔧 Customization

### Farver
Skift hovedfarver ved at opdatere CSS variablerne:
```css
:root {
    --primary-color: #din-farve;
    --secondary-color: #din-farve;
    --accent-color: #din-farve;
}
```

### Logo
Erstat `assets/axion-logo.svg` med dit eget logo

### Indhold
Opdater tekster og indhold direkte i HTML filerne

### Kommandoer
Tilføj nye kommandoer ved at udvide `commands.html` med:
```html
<div class="command-item" data-category="kategori" data-permission="niveau">
    <h3 class="command-name">!kommando</h3>
    <p class="command-desc">Beskrivelse</p>
    <div class="command-example">
        <code>!kommando parameter</code>
    </div>
</div>
```

## 📱 Responsive Design

Websitet er optimeret for:
- **Desktop** (1200px+)
- **Tablet** (768px - 1199px)
- **Mobile** (320px - 767px)

Breakpoints:
```css
@media (max-width: 1200px) { /* Desktop */ }
@media (max-width: 1024px) { /* Tablet landscape */ }
@media (max-width: 768px) { /* Tablet portrait */ }
@media (max-width: 480px) { /* Mobile */ }
```

## 🌐 Browser Support

- **Chrome** 80+
- **Firefox** 75+
- **Safari** 13+
- **Edge** 80+

## ⚡ Performance Tips

1. **Optimér billeder** - Brug WebP format hvor muligt
2. **Minificér CSS/JS** - Brug build tools i produktion
3. **Aktivér compression** - Gzip/Brotli på serveren
4. **Brug CDN** - For hurtigere loading globalt
5. **Monitor performance** - Brug Lighthouse og Web Vitals

## 🔧 Maintenance

### Opdatering af Statistikker
Opdater bot statistikker i `config.js`:
```javascript
stats: {
    servers: 50000,
    users: 2500000,
    commands: 150,
    uptime: 99.9
}
```

### Tilføjelse af Nye Funktioner
1. Opdater `config.js` med nye data
2. Tilføj HTML markup
3. Implementer JavaScript funktionalitet
4. Tilføj CSS styling

### Backup
Regelmæssig backup af:
- HTML filer
- CSS filer
- JavaScript filer
- Billeder og assets
- Konfigurationsfiler

## 🐛 Troubleshooting

### Almindelige Problemer

**Service Worker virker ikke**
- Tjek at du bruger HTTPS
- Verify at `sw.js` er tilgængelig
- Tjek browser console for fejl

**Animationer virker ikke**
- Kontroller at `enhanced-v2.js` er loaded
- Verify CSS supports animations
- Tjek for JavaScript fejl

**Søgning fungerer ikke**
- Kontroller at `commands.js` er loaded
- Verify HTML structure er korrekt
- Tjek for console errors

## 🤝 Contributing

Vil du bidrage til websitet?

1. **Fork** projektet
2. **Opret** en feature branch
3. **Commit** dine ændringer
4. **Push** til branchen
5. **Opret** en Pull Request

## 📄 License

Dette projekt er licensed under [MIT License](LICENSE).

## 📞 Support

Har du brug for hjælp?
- **Discord**: discord.gg/axionbot
- **Email**: support@axion-bot.com
- **Documentation**: docs.axion-bot.com

---

**Lavet med ❤️ af Axion Bot Team**

*Sidste opdatering: December 2024*
