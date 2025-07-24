# GDPR Compliance Manager

A comprehensive GDPR (General Data Protection Regulation) compliance system for the Axion Discord bot, ensuring full compliance with EU data protection regulations.

## Overview

The GDPR Compliance Manager provides a complete solution for managing user data in accordance with GDPR requirements, including:

- **User Consent Management** - Track and manage user consent for data processing
- **Data Retention Policies** - Automated data lifecycle management
- **Data Processing Activities** - GDPR Article 30 compliance documentation
- **User Rights Implementation** - Data export, deletion, and anonymization
- **Audit Trail** - Complete logging of all GDPR-related activities
- **Automated Compliance** - Scheduled data cleanup and consent monitoring

## Features

### 🔒 User Consent Management
- Record explicit user consent for different data processing purposes
- Track consent withdrawal and updates
- Consent expiration monitoring (2-year best practice)
- Multiple consent methods support (slash commands, reactions, etc.)

### 📋 Data Processing Activities (Article 30)
- Complete documentation of all data processing activities
- Legal basis tracking for each processing purpose
- Data categories and recipient management
- International data transfer documentation

### ⏰ Data Retention Policies
- Configurable retention periods for different data types
- Automated data cleanup based on retention rules
- Legal compliance retention (up to 7 years for certain data)
- Dry-run capability for testing cleanup operations

### 👤 User Rights (Articles 15-22)
- **Right of Access** - Data export in human-readable or JSON format
- **Right to Rectification** - Data correction capabilities
- **Right to Erasure** - Complete data deletion
- **Right to Data Portability** - Structured data export
- **Right to Object** - Consent withdrawal

### 🔍 Audit and Compliance
- Complete audit trail of all GDPR operations
- Compliance status monitoring
- Automated compliance reports
- Data breach notification support

## Architecture

### Core Components

1. **GDPRComplianceManager** - Main service class coordinating all GDPR operations
2. **UserConsent** - Represents user consent records with validation
3. **DataRetentionPolicy** - Manages data retention rules and cleanup
4. **DataProcessingActivity** - GDPR Article 30 compliance documentation
5. **GDPRSlashCommands** - User-facing slash commands for GDPR operations

### Database Schema

The system uses a comprehensive database schema with the following key tables:

- `user_consent` - User consent records
- `user_consent_purposes` - Specific consent purposes
- `data_retention_policies` - Retention policy definitions
- `data_processing_activities` - Article 30 processing records
- `gdpr_audit_log` - Complete audit trail
- `data_deletion_requests` - User deletion requests
- `data_export_requests` - User export requests
- `gdpr_settings` - Per-guild configuration

## Installation

### 1. Database Setup

Run the GDPR schema migration:

```sql
-- Execute the schema file
source src/main/resources/db/migration/gdpr_schema.sql
```

### 2. Integration

Add the GDPR Compliance Manager to your bot:

```java
// Initialize the GDPR manager
GDPRComplianceManager gdprManager = new GDPRComplianceManager(databaseService);

// Register slash commands
GDPRSlashCommands gdprCommands = new GDPRSlashCommands(gdprManager);
List<CommandData> commands = GDPRSlashCommands.createCommands();
// Register commands with JDA

// Handle command events
public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
    switch (event.getName()) {
        case "gdpr" -> gdprCommands.handleGDPRCommand(event);
        case "gdpr-admin" -> gdprCommands.handleGDPRAdminCommand(event);
    }
}
```

### 3. Configuration

Configure GDPR settings for each guild:

```java
// Set up default GDPR settings
gdprManager.initializeGuildSettings(guildId, {
    autoConsentRequired: true,
    consentReminderDays: 365,
    autoCleanupEnabled: true,
    cleanupSchedule: "WEEKLY"
});
```

## Usage

### User Commands

Users can manage their data using the `/gdpr` command:

#### Consent Management
```
/gdpr consent action:give purposes:MODERATION,ANALYTICS
/gdpr consent action:withdraw
/gdpr consent action:update purposes:MODERATION
```

#### Data Export
```
/gdpr export format:readable
/gdpr export format:json
```

#### Data Deletion
```
/gdpr delete confirm:true
/gdpr anonymize confirm:true
```

#### Status Check
```
/gdpr status
/gdpr policy type:retention
```

### Admin Commands

Administrators can use the `/gdpr-admin` command:

#### Processing Activities
```
/gdpr-admin activities format:summary
/gdpr-admin activities format:gdpr
```

#### Compliance Audit
```
/gdpr-admin audit detailed:true
```

#### Data Cleanup
```
/gdpr-admin cleanup dry_run:true
/gdpr-admin cleanup dry_run:false
```

#### User Management
```
/gdpr-admin user-data user:@user action:view
/gdpr-admin user-data user:@user action:export
/gdpr-admin user-data user:@user action:delete
```

## Data Processing Purposes

The system supports the following data processing purposes:

- **MODERATION** - Server moderation and safety (2 years retention)
- **ANALYTICS** - Server analytics and insights (1 year retention)
- **SECURITY** - Security monitoring and threat detection (3 years retention)
- **PERSONALIZATION** - User experience personalization (6 months retention)
- **COMMUNICATION** - Communication features (3 months retention)
- **LEGAL_COMPLIANCE** - Legal and regulatory compliance (7 years retention)

## Compliance Features

### GDPR Articles Covered

- **Article 6** - Lawfulness of processing (legal basis tracking)
- **Article 7** - Conditions for consent (consent management)
- **Article 13-14** - Information to be provided (privacy notices)
- **Article 15** - Right of access (data export)
- **Article 16** - Right to rectification (data correction)
- **Article 17** - Right to erasure (data deletion)
- **Article 18** - Right to restriction (processing limitation)
- **Article 20** - Right to data portability (structured export)
- **Article 21** - Right to object (consent withdrawal)
- **Article 25** - Data protection by design (built-in privacy)
- **Article 30** - Records of processing (activity documentation)
- **Article 32** - Security of processing (audit trail)

### Best Practices Implemented

1. **Privacy by Design** - GDPR compliance built into the system architecture
2. **Data Minimization** - Only collect and process necessary data
3. **Purpose Limitation** - Clear purposes for each data processing activity
4. **Storage Limitation** - Automated data retention and cleanup
5. **Accuracy** - Data correction and update mechanisms
6. **Integrity and Confidentiality** - Secure data handling and audit trails
7. **Accountability** - Complete documentation and audit capabilities

## Security Measures

- **Encrypted Storage** - All sensitive data encrypted at rest
- **Access Controls** - Role-based access to GDPR functions
- **Audit Logging** - Complete audit trail of all operations
- **Data Anonymization** - Secure anonymization algorithms
- **Secure Deletion** - Cryptographic data deletion methods

## Monitoring and Alerts

### Automated Monitoring
- Consent expiration alerts
- Data retention compliance checks
- Failed operation notifications
- Compliance status reports

### Audit Reports
- Monthly compliance summaries
- Data processing activity reports
- User rights exercise tracking
- Security incident documentation

## API Reference

### GDPRComplianceManager Methods

```java
// Consent Management
CompletableFuture<Boolean> recordConsent(String userId, String guildId, Set<DataProcessingPurpose> purposes, String method)
CompletableFuture<Boolean> withdrawConsent(String userId, String guildId)
CompletableFuture<UserConsent> getUserConsent(String userId, String guildId)

// Data Export
CompletableFuture<String> exportUserData(String userId, String guildId, String format)

// Data Deletion
CompletableFuture<Boolean> deleteUserData(String userId, String guildId)
CompletableFuture<Boolean> anonymizeUserData(String userId, String guildId)

// Data Retention
CompletableFuture<Integer> cleanupExpiredData(String guildId, boolean dryRun)

// Audit and Compliance
void logGDPRAction(String userId, String guildId, String action, String details)
CompletableFuture<List<DataProcessingActivity>> getProcessingActivities(String guildId)
```

## Configuration Options

### Guild Settings

```java
public class GDPRSettings {
    private boolean autoConsentRequired = true;
    private int consentReminderDays = 365;
    private String dataRetentionPolicyId = "default-policy";
    private String privacyPolicyUrl;
    private String dataControllerContact;
    private String dpoContact; // Data Protection Officer
    private boolean autoCleanupEnabled = true;
    private String cleanupSchedule = "WEEKLY";
    private String notificationChannelId;
}
```

### Retention Policies

```java
// Create custom retention policy
Map<DataProcessingPurpose, Duration> customPeriods = new HashMap<>();
customPeriods.put(DataProcessingPurpose.MODERATION, Duration.ofDays(365));
customPeriods.put(DataProcessingPurpose.ANALYTICS, Duration.ofDays(180));

DataRetentionPolicy customPolicy = new DataRetentionPolicy(
    "custom-policy",
    "Custom Retention Policy",
    "Shorter retention periods for privacy-focused servers",
    customPeriods,
    dataTypes,
    "User consent and legitimate interest",
    "admin-user-id"
);
```

## Troubleshooting

### Common Issues

1. **Consent Not Recorded**
   - Check database connectivity
   - Verify user and guild IDs
   - Check audit logs for errors

2. **Data Export Fails**
   - Verify user has data to export
   - Check export format parameter
   - Review database permissions

3. **Cleanup Not Working**
   - Verify retention policies are active
   - Check cleanup schedule configuration
   - Review audit logs for cleanup operations

### Debug Mode

Enable debug logging for detailed operation tracking:

```java
// Enable debug logging
Logger logger = LoggerFactory.getLogger(GDPRComplianceManager.class);
logger.setLevel(Level.DEBUG);
```

## Legal Considerations

⚠️ **Important Legal Notice**: This system provides technical tools for GDPR compliance but does not constitute legal advice. Organizations should:

1. Consult with legal experts for GDPR compliance strategy
2. Conduct Data Protection Impact Assessments (DPIAs)
3. Appoint a Data Protection Officer (DPO) if required
4. Implement appropriate privacy policies and notices
5. Establish procedures for handling data subject requests
6. Ensure staff training on GDPR requirements

## Contributing

When contributing to the GDPR Compliance Manager:

1. Ensure all changes maintain GDPR compliance
2. Add appropriate audit logging for new operations
3. Update documentation for new features
4. Include unit tests for compliance-critical functions
5. Follow data minimization principles

## License

This GDPR Compliance Manager is part of the Axion Discord bot project and is subject to the same license terms.

---

**Disclaimer**: This system is designed to assist with GDPR compliance but does not guarantee full legal compliance. Organizations are responsible for ensuring their overall GDPR compliance strategy meets all legal requirements.