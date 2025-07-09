#!/bin/bash

# Axion Bot Website Build Script
# This script optimizes the website for production deployment

echo "🚀 Building Axion Bot Website..."

# Create build directory
mkdir -p build
rm -rf build/*

# Copy all files to build directory
cp -r . build/
cd build

# Remove unnecessary files
rm -rf .git
rm -rf node_modules
rm -f build.sh
rm -f README.md

# Optimize CSS (if you have a CSS minifier)
# csso enhanced-styles.css > enhanced-styles.min.css
# csso commands.css > commands.min.css
# csso modern-utilities.css > modern-utilities.min.css

# Optimize JavaScript (if you have a JS minifier)
# uglifyjs enhanced-v2.js > enhanced-v2.min.js
# uglifyjs commands.js > commands.min.js

# Create production config
cat > config.production.js << 'EOF'
// Production Configuration
const AxionConfig = {
    ...window.AxionConfig,
    
    // Override for production
    api: {
        baseUrl: 'https://api.axion-bot.com',
        version: 'v1'
    },
    
    performance: {
        lazyLoading: true,
        caching: true,
        compression: true,
        minification: true,
        serviceWorker: true
    },
    
    analytics: {
        enabled: true,
        googleAnalytics: 'YOUR_GA_ID'
    }
};

window.AxionConfig = AxionConfig;
EOF

# Update HTML files to use minified versions (if available)
# sed -i 's/enhanced-v2.js/enhanced-v2.min.js/g' *.html
# sed -i 's/commands.js/commands.min.js/g' *.html
# sed -i 's/enhanced-styles.css/enhanced-styles.min.css/g' *.html

# Generate sitemap.xml
cat > sitemap.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
    <url>
        <loc>https://axion-bot.com/</loc>
        <changefreq>weekly</changefreq>
        <priority>1.0</priority>
    </url>
    <url>
        <loc>https://axion-bot.com/commands</loc>
        <changefreq>weekly</changefreq>
        <priority>0.8</priority>
    </url>
    <url>
        <loc>https://axion-bot.com/pricing</loc>
        <changefreq>monthly</changefreq>
        <priority>0.7</priority>
    </url>
    <url>
        <loc>https://axion-bot.com/login</loc>
        <changefreq>monthly</changefreq>
        <priority>0.6</priority>
    </url>
</urlset>
EOF

# Generate robots.txt
cat > robots.txt << 'EOF'
User-agent: *
Allow: /
Disallow: /dashboard/
Disallow: /admin/

Sitemap: https://axion-bot.com/sitemap.xml
EOF

# Create .htaccess for Apache servers
cat > .htaccess << 'EOF'
# Enable compression
<IfModule mod_deflate.c>
    AddOutputFilterByType DEFLATE text/plain
    AddOutputFilterByType DEFLATE text/html
    AddOutputFilterByType DEFLATE text/xml
    AddOutputFilterByType DEFLATE text/css
    AddOutputFilterByType DEFLATE application/xml
    AddOutputFilterByType DEFLATE application/xhtml+xml
    AddOutputFilterByType DEFLATE application/rss+xml
    AddOutputFilterByType DEFLATE application/javascript
    AddOutputFilterByType DEFLATE application/x-javascript
</IfModule>

# Enable caching
<IfModule mod_expires.c>
    ExpiresActive On
    ExpiresByType image/jpg "access plus 1 month"
    ExpiresByType image/jpeg "access plus 1 month"
    ExpiresByType image/gif "access plus 1 month"
    ExpiresByType image/png "access plus 1 month"
    ExpiresByType text/css "access plus 1 month"
    ExpiresByType application/pdf "access plus 1 month"
    ExpiresByType application/javascript "access plus 1 month"
    ExpiresByType application/x-javascript "access plus 1 month"
    ExpiresByType application/x-shockwave-flash "access plus 1 month"
    ExpiresByType image/x-icon "access plus 1 year"
    ExpiresDefault "access plus 2 days"
</IfModule>

# Security headers
<IfModule mod_headers.c>
    Header always set X-Frame-Options DENY
    Header always set X-Content-Type-Options nosniff
    Header always set X-XSS-Protection "1; mode=block"
    Header always set Referrer-Policy "strict-origin-when-cross-origin"
    Header always set Content-Security-Policy "default-src 'self'; script-src 'self' 'unsafe-inline' https://cdnjs.cloudflare.com https://fonts.googleapis.com; style-src 'self' 'unsafe-inline' https://fonts.googleapis.com https://cdnjs.cloudflare.com; font-src 'self' https://fonts.gstatic.com; img-src 'self' data:; connect-src 'self' https://api.axion-bot.com"
</IfModule>

# Redirect HTTP to HTTPS
RewriteEngine On
RewriteCond %{HTTPS} off
RewriteRule ^(.*)$ https://%{HTTP_HOST}%{REQUEST_URI} [L,R=301]

# Pretty URLs
RewriteEngine On
RewriteCond %{REQUEST_FILENAME} !-f
RewriteCond %{REQUEST_FILENAME} !-d
RewriteRule ^commands$ commands.html [L]
RewriteRule ^pricing$ pricing.html [L]
RewriteRule ^login$ login.html [L]
RewriteRule ^dashboard$ dashboard.html [L]
EOF

# File size check
echo "📊 Build Statistics:"
echo "Total files: $(find . -type f | wc -l)"
echo "Total size: $(du -sh . | cut -f1)"

# List large files
echo "📋 Large files (>100KB):"
find . -type f -size +100k -exec ls -lh {} \; | awk '{print $9 ": " $5}'

echo "✅ Build complete! Files are ready for deployment in the 'build' directory."
echo "🌐 Deploy the contents of the 'build' directory to your web server."

# Optional: Create deployment archive
tar -czf axion-website-$(date +%Y%m%d).tar.gz *
echo "📦 Deployment archive created: axion-website-$(date +%Y%m%d).tar.gz"
