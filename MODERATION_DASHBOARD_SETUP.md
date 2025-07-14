# Moderation Dashboard Database Integration

## Overview
The moderation dashboard has been successfully connected to the database system. The integration includes:

### Database Integration Features
- **Moderation Configuration Storage**: All moderation settings are stored in the `moderation_configs` table
- **Moderation Logs**: All moderation actions are logged to the `moderation_logs` table
- **Statistics Tracking**: Real-time statistics for guilds, configurations, and active filters
- **Template Management**: Pre-configured moderation templates stored in the database

### API Endpoints Connected to Database
- `/api/moderation/guilds` - Retrieves all guilds with moderation configurations
- `/api/moderation/config/guild/{guildId}` - Get/Save guild-specific moderation configurations
- `/api/moderation/stats` - Real-time moderation statistics
- `/api/moderation/logs` - Moderation action logs
- `/api/moderation/config/templates` - Moderation configuration templates

### Database Tables Created
1. **moderation_configs** - Stores guild moderation configurations
2. **moderation_config_backups** - Backup configurations
3. **moderation_config_templates** - Pre-defined templates
4. **moderation_logs** - All moderation actions and violations
5. **server_config** - Server-specific settings
6. **temp_bans** - Temporary ban management
7. **user_languages** - User language preferences

## Files Modified/Created

### Core Integration Files
- `ModerationConfigurationController.java` - Main web controller with database integration
- `ModerationDashboardServer.java` - Standalone server launcher
- `DatabaseService.java` - Database operations for moderation system
- `ModerationManager.java` - Core moderation logic with database logging

### Web Dashboard
- `moderation-dashboard.html` - Frontend dashboard interface
- All API endpoints properly connected to database backend

## Running the Dashboard

### Prerequisites
Ensure you have the following dependencies in your project:
```xml
<!-- Add to pom.xml if using Maven -->
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
    <version>5.0.1</version>
</dependency>
<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <version>3.42.0.0</version>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-core</artifactId>
    <version>1.11.0</version>
</dependency>
```

### Starting the Dashboard
1. **Standalone Mode**:
   ```bash
   java -cp "path/to/classes:path/to/dependencies/*" com.axion.bot.web.ModerationDashboardServer
   ```

2. **Custom Database URL**:
   ```bash
   java -Ddatabase.url="jdbc:sqlite:your_database.db" -Dserver.port=8080 com.axion.bot.web.ModerationDashboardServer
   ```

3. **Integrated with Bot**:
   ```java
   // In your main bot class
   ModerationDashboardServer dashboardServer = new ModerationDashboardServer("jdbc:sqlite:bot.db");
   dashboardServer.start(8080);
   ```

### Accessing the Dashboard
Once started, access the dashboard at:
- **URL**: `http://localhost:8080/moderation-dashboard.html`
- **API Base**: `http://localhost:8080/api/moderation/`

## Database Schema

The system automatically creates all necessary tables on first run:

```sql
-- Moderation configurations per guild
CREATE TABLE moderation_configs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    guild_id TEXT NOT NULL,
    config_data TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Moderation action logs
CREATE TABLE moderation_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    guild_id TEXT NOT NULL,
    user_id TEXT NOT NULL,
    action TEXT NOT NULL,
    reason TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- And more tables for comprehensive moderation management...
```

## Features

### Real-time Dashboard
- Live moderation statistics
- Guild management interface
- Configuration testing and validation
- Template-based setup

### Database-Backed Operations
- Persistent moderation configurations
- Complete audit trail of all actions
- Backup and restore capabilities
- Multi-guild support

### API Integration
- RESTful API for all operations
- JSON-based configuration management
- Real-time data updates
- Comprehensive error handling

## Troubleshooting

### Common Issues
1. **Database Connection**: Ensure SQLite file permissions are correct
2. **Port Conflicts**: Change the server port if 8080 is in use
3. **Missing Dependencies**: Install required JAR files

### Logs
Check console output for detailed error messages and connection status.

## Security Notes
- Database file should be secured with appropriate file permissions
- Consider using environment variables for sensitive configuration
- Regular backups of the moderation database are recommended

The moderation system is now fully integrated with the database and ready for production use!