@echo off
REM Axion Bot Website Build Script for Windows
REM This script optimizes the website for production deployment

echo 🚀 Building Axion Bot Website...

REM Create build directory
if exist build rmdir /s /q build
mkdir build

REM Copy all files to build directory
xcopy /e /i /y . build\
cd build

REM Remove unnecessary files
if exist .git rmdir /s /q .git
if exist node_modules rmdir /s /q node_modules
if exist build.sh del build.sh
if exist build.bat del build.bat
if exist README.md del README.md

REM Create production config
echo // Production Configuration > config.production.js
echo const AxionConfig = { >> config.production.js
echo     ...window.AxionConfig, >> config.production.js
echo. >> config.production.js
echo     // Override for production >> config.production.js
echo     api: { >> config.production.js
echo         baseUrl: 'https://api.axion-bot.com', >> config.production.js
echo         version: 'v1' >> config.production.js
echo     }, >> config.production.js
echo. >> config.production.js
echo     performance: { >> config.production.js
echo         lazyLoading: true, >> config.production.js
echo         caching: true, >> config.production.js
echo         compression: true, >> config.production.js
echo         minification: true, >> config.production.js
echo         serviceWorker: true >> config.production.js
echo     }, >> config.production.js
echo. >> config.production.js
echo     analytics: { >> config.production.js
echo         enabled: true, >> config.production.js
echo         googleAnalytics: 'YOUR_GA_ID' >> config.production.js
echo     } >> config.production.js
echo }; >> config.production.js
echo. >> config.production.js
echo window.AxionConfig = AxionConfig; >> config.production.js

REM Generate sitemap.xml
echo ^<?xml version="1.0" encoding="UTF-8"?^> > sitemap.xml
echo ^<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9"^> >> sitemap.xml
echo     ^<url^> >> sitemap.xml
echo         ^<loc^>https://axion-bot.com/^</loc^> >> sitemap.xml
echo         ^<changefreq^>weekly^</changefreq^> >> sitemap.xml
echo         ^<priority^>1.0^</priority^> >> sitemap.xml
echo     ^</url^> >> sitemap.xml
echo     ^<url^> >> sitemap.xml
echo         ^<loc^>https://axion-bot.com/commands^</loc^> >> sitemap.xml
echo         ^<changefreq^>weekly^</changefreq^> >> sitemap.xml
echo         ^<priority^>0.8^</priority^> >> sitemap.xml
echo     ^</url^> >> sitemap.xml
echo     ^<url^> >> sitemap.xml
echo         ^<loc^>https://axion-bot.com/pricing^</loc^> >> sitemap.xml
echo         ^<changefreq^>monthly^</changefreq^> >> sitemap.xml
echo         ^<priority^>0.7^</priority^> >> sitemap.xml
echo     ^</url^> >> sitemap.xml
echo     ^<url^> >> sitemap.xml
echo         ^<loc^>https://axion-bot.com/login^</loc^> >> sitemap.xml
echo         ^<changefreq^>monthly^</changefreq^> >> sitemap.xml
echo         ^<priority^>0.6^</priority^> >> sitemap.xml
echo     ^</url^> >> sitemap.xml
echo ^</urlset^> >> sitemap.xml

REM Generate robots.txt
echo User-agent: * > robots.txt
echo Allow: / >> robots.txt
echo Disallow: /dashboard/ >> robots.txt
echo Disallow: /admin/ >> robots.txt
echo. >> robots.txt
echo Sitemap: https://axion-bot.com/sitemap.xml >> robots.txt

REM Create web.config for IIS servers
echo ^<?xml version="1.0" encoding="UTF-8"?^> > web.config
echo ^<configuration^> >> web.config
echo     ^<system.webServer^> >> web.config
echo         ^<staticContent^> >> web.config
echo             ^<mimeMap fileExtension=".svg" mimeType="image/svg+xml" /^> >> web.config
echo             ^<mimeMap fileExtension=".woff" mimeType="font/woff" /^> >> web.config
echo             ^<mimeMap fileExtension=".woff2" mimeType="font/woff2" /^> >> web.config
echo         ^</staticContent^> >> web.config
echo         ^<httpCompression^> >> web.config
echo             ^<dynamicTypes^> >> web.config
echo                 ^<add mimeType="text/*" enabled="true" /^> >> web.config
echo                 ^<add mimeType="message/*" enabled="true" /^> >> web.config
echo                 ^<add mimeType="application/javascript" enabled="true" /^> >> web.config
echo                 ^<add mimeType="application/json" enabled="true" /^> >> web.config
echo             ^</dynamicTypes^> >> web.config
echo             ^<staticTypes^> >> web.config
echo                 ^<add mimeType="text/*" enabled="true" /^> >> web.config
echo                 ^<add mimeType="message/*" enabled="true" /^> >> web.config
echo                 ^<add mimeType="application/javascript" enabled="true" /^> >> web.config
echo                 ^<add mimeType="application/json" enabled="true" /^> >> web.config
echo             ^</staticTypes^> >> web.config
echo         ^</httpCompression^> >> web.config
echo         ^<rewrite^> >> web.config
echo             ^<rules^> >> web.config
echo                 ^<rule name="Redirect to HTTPS" stopProcessing="true"^> >> web.config
echo                     ^<match url=".*" /^> >> web.config
echo                     ^<conditions^> >> web.config
echo                         ^<add input="{HTTPS}" pattern="off" ignoreCase="true" /^> >> web.config
echo                     ^</conditions^> >> web.config
echo                     ^<action type="Redirect" url="https://{HTTP_HOST}{REQUEST_URI}" redirectType="Permanent" /^> >> web.config
echo                 ^</rule^> >> web.config
echo             ^</rules^> >> web.config
echo         ^</rewrite^> >> web.config
echo     ^</system.webServer^> >> web.config
echo ^</configuration^> >> web.config

REM File statistics
echo.
echo 📊 Build Statistics:
dir /s /-c | find "File(s)"
echo.

REM List large files
echo 📋 Large files (^>100KB):
forfiles /m *.* /c "cmd /c if @fsize GTR 102400 echo @path: @fsize bytes" 2>nul

echo.
echo ✅ Build complete! Files are ready for deployment in the 'build' directory.
echo 🌐 Deploy the contents of the 'build' directory to your web server.

REM Create deployment archive (requires 7-zip or similar)
if exist "C:\Program Files\7-Zip\7z.exe" (
    "C:\Program Files\7-Zip\7z.exe" a -tzip axion-website-%date:~-4,4%%date:~-10,2%%date:~-7,2%.zip *
    echo 📦 Deployment archive created: axion-website-%date:~-4,4%%date:~-10,2%%date:~-7,2%.zip
) else (
    echo 📦 Install 7-Zip to create deployment archive automatically
)

pause
