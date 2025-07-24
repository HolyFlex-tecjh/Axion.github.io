-- GDPR Compliance Database Schema
-- This file contains the database schema for GDPR compliance features

-- User Consent Table
-- Stores user consent records for data processing
CREATE TABLE IF NOT EXISTS user_consent (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id VARCHAR(20) NOT NULL,
    guild_id VARCHAR(20) NOT NULL,
    consent_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    consent_method VARCHAR(50) NOT NULL,
    ip_address VARCHAR(45), -- IPv6 compatible
    user_agent TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    withdrawal_timestamp TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_user_guild (user_id, guild_id),
    INDEX idx_consent_timestamp (consent_timestamp),
    INDEX idx_active_consent (is_active),
    UNIQUE KEY unique_user_guild_consent (user_id, guild_id, consent_timestamp)
);

-- User Consent Purposes Table
-- Stores the specific purposes for which consent was given
CREATE TABLE IF NOT EXISTS user_consent_purposes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    consent_id BIGINT NOT NULL,
    purpose VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (consent_id) REFERENCES user_consent(id) ON DELETE CASCADE,
    INDEX idx_consent_purpose (consent_id, purpose),
    UNIQUE KEY unique_consent_purpose (consent_id, purpose)
);

-- Data Retention Policies Table
-- Stores data retention policies for different data types and purposes
CREATE TABLE IF NOT EXISTS data_retention_policies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    policy_id VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    legal_basis TEXT NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_policy_id (policy_id),
    INDEX idx_active_policies (is_active)
);

-- Data Retention Policy Rules Table
-- Stores specific retention rules for each policy
CREATE TABLE IF NOT EXISTS data_retention_policy_rules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    policy_id BIGINT NOT NULL,
    purpose VARCHAR(50) NOT NULL,
    retention_days INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (policy_id) REFERENCES data_retention_policies(id) ON DELETE CASCADE,
    INDEX idx_policy_purpose (policy_id, purpose),
    UNIQUE KEY unique_policy_purpose (policy_id, purpose)
);

-- Data Retention Policy Data Types Table
-- Stores data types that each policy applies to
CREATE TABLE IF NOT EXISTS data_retention_policy_data_types (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    policy_id BIGINT NOT NULL,
    data_type VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (policy_id) REFERENCES data_retention_policies(id) ON DELETE CASCADE,
    INDEX idx_policy_data_type (policy_id, data_type),
    UNIQUE KEY unique_policy_data_type (policy_id, data_type)
);

-- Data Processing Activities Table
-- Stores records of data processing activities as required by GDPR Article 30
CREATE TABLE IF NOT EXISTS data_processing_activities (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    activity_id VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    purpose VARCHAR(50) NOT NULL,
    legal_basis TEXT NOT NULL,
    controller VARCHAR(255) NOT NULL,
    processor VARCHAR(255),
    retention_period VARCHAR(255) NOT NULL,
    security_measures TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_activity_id (activity_id),
    INDEX idx_purpose (purpose),
    INDEX idx_active_activities (is_active)
);

-- Data Processing Activity Categories Table
-- Stores data categories for each processing activity
CREATE TABLE IF NOT EXISTS data_processing_activity_categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    activity_id BIGINT NOT NULL,
    data_category VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (activity_id) REFERENCES data_processing_activities(id) ON DELETE CASCADE,
    INDEX idx_activity_category (activity_id, data_category),
    UNIQUE KEY unique_activity_category (activity_id, data_category)
);

-- Data Processing Activity Subjects Table
-- Stores data subject categories for each processing activity
CREATE TABLE IF NOT EXISTS data_processing_activity_subjects (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    activity_id BIGINT NOT NULL,
    data_subject VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (activity_id) REFERENCES data_processing_activities(id) ON DELETE CASCADE,
    INDEX idx_activity_subject (activity_id, data_subject),
    UNIQUE KEY unique_activity_subject (activity_id, data_subject)
);

-- Data Processing Activity Recipients Table
-- Stores recipients for each processing activity
CREATE TABLE IF NOT EXISTS data_processing_activity_recipients (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    activity_id BIGINT NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (activity_id) REFERENCES data_processing_activities(id) ON DELETE CASCADE,
    INDEX idx_activity_recipient (activity_id, recipient),
    UNIQUE KEY unique_activity_recipient (activity_id, recipient)
);

-- Data Processing Activity Transfers Table
-- Stores international data transfer information
CREATE TABLE IF NOT EXISTS data_processing_activity_transfers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    activity_id BIGINT NOT NULL,
    transfer_details TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (activity_id) REFERENCES data_processing_activities(id) ON DELETE CASCADE,
    INDEX idx_activity_transfer (activity_id)
);

-- Data Processing Activity Additional Info Table
-- Stores additional information for processing activities
CREATE TABLE IF NOT EXISTS data_processing_activity_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    activity_id BIGINT NOT NULL,
    info_key VARCHAR(255) NOT NULL,
    info_value TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (activity_id) REFERENCES data_processing_activities(id) ON DELETE CASCADE,
    INDEX idx_activity_info (activity_id, info_key),
    UNIQUE KEY unique_activity_info (activity_id, info_key)
);

-- GDPR Audit Log Table
-- Stores audit trail for GDPR-related actions
CREATE TABLE IF NOT EXISTS gdpr_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id VARCHAR(20),
    guild_id VARCHAR(20),
    action VARCHAR(100) NOT NULL,
    details TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    performed_by VARCHAR(20), -- User ID of who performed the action
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_user_guild_audit (user_id, guild_id),
    INDEX idx_action (action),
    INDEX idx_timestamp (timestamp),
    INDEX idx_performed_by (performed_by)
);

-- Data Deletion Requests Table
-- Stores user requests for data deletion
CREATE TABLE IF NOT EXISTS data_deletion_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id VARCHAR(20) NOT NULL,
    guild_id VARCHAR(20) NOT NULL,
    request_type ENUM('DELETE', 'ANONYMIZE') NOT NULL DEFAULT 'DELETE',
    status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED') NOT NULL DEFAULT 'PENDING',
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL,
    processed_by VARCHAR(20), -- User ID of who processed the request
    notes TEXT,
    
    INDEX idx_user_guild_deletion (user_id, guild_id),
    INDEX idx_status (status),
    INDEX idx_requested_at (requested_at)
);

-- Data Export Requests Table
-- Stores user requests for data export
CREATE TABLE IF NOT EXISTS data_export_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id VARCHAR(20) NOT NULL,
    guild_id VARCHAR(20) NOT NULL,
    export_format VARCHAR(20) NOT NULL DEFAULT 'readable',
    status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED') NOT NULL DEFAULT 'PENDING',
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL,
    processed_by VARCHAR(20), -- User ID of who processed the request
    export_size BIGINT, -- Size of exported data in bytes
    download_url VARCHAR(500), -- URL for downloading the export (if applicable)
    expires_at TIMESTAMP, -- When the download link expires
    notes TEXT,
    
    INDEX idx_user_guild_export (user_id, guild_id),
    INDEX idx_status (status),
    INDEX idx_requested_at (requested_at),
    INDEX idx_expires_at (expires_at)
);

-- GDPR Settings Table
-- Stores GDPR configuration settings per guild
CREATE TABLE IF NOT EXISTS gdpr_settings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    guild_id VARCHAR(20) NOT NULL UNIQUE,
    auto_consent_required BOOLEAN NOT NULL DEFAULT TRUE,
    consent_reminder_days INT NOT NULL DEFAULT 365,
    data_retention_policy_id VARCHAR(100),
    privacy_policy_url VARCHAR(500),
    data_controller_contact TEXT,
    dpo_contact TEXT, -- Data Protection Officer contact
    auto_cleanup_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    cleanup_schedule VARCHAR(50) NOT NULL DEFAULT 'WEEKLY',
    notification_channel_id VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_guild_id (guild_id)
);

-- Insert default data retention policy
INSERT IGNORE INTO data_retention_policies (policy_id, name, description, legal_basis, created_by, is_active)
VALUES (
    'default-policy',
    'Default GDPR Retention Policy',
    'Default data retention policy compliant with GDPR requirements',
    'Legitimate interest and legal compliance',
    'system',
    TRUE
);

-- Insert default retention policy rules
INSERT IGNORE INTO data_retention_policy_rules (policy_id, purpose, retention_days)
SELECT p.id, 'MODERATION', 730 FROM data_retention_policies p WHERE p.policy_id = 'default-policy'
UNION ALL
SELECT p.id, 'ANALYTICS', 365 FROM data_retention_policies p WHERE p.policy_id = 'default-policy'
UNION ALL
SELECT p.id, 'SECURITY', 1095 FROM data_retention_policies p WHERE p.policy_id = 'default-policy'
UNION ALL
SELECT p.id, 'PERSONALIZATION', 180 FROM data_retention_policies p WHERE p.policy_id = 'default-policy'
UNION ALL
SELECT p.id, 'COMMUNICATION', 90 FROM data_retention_policies p WHERE p.policy_id = 'default-policy'
UNION ALL
SELECT p.id, 'LEGAL_COMPLIANCE', 2555 FROM data_retention_policies p WHERE p.policy_id = 'default-policy';

-- Insert default data types for the policy
INSERT IGNORE INTO data_retention_policy_data_types (policy_id, data_type)
SELECT p.id, 'user_messages' FROM data_retention_policies p WHERE p.policy_id = 'default-policy'
UNION ALL
SELECT p.id, 'moderation_logs' FROM data_retention_policies p WHERE p.policy_id = 'default-policy'
UNION ALL
SELECT p.id, 'user_behavior_data' FROM data_retention_policies p WHERE p.policy_id = 'default-policy'
UNION ALL
SELECT p.id, 'analytics_data' FROM data_retention_policies p WHERE p.policy_id = 'default-policy'
UNION ALL
SELECT p.id, 'security_logs' FROM data_retention_policies p WHERE p.policy_id = 'default-policy'
UNION ALL
SELECT p.id, 'user_preferences' FROM data_retention_policies p WHERE p.policy_id = 'default-policy';

-- Insert default processing activities
INSERT IGNORE INTO data_processing_activities (
    activity_id, name, description, purpose, legal_basis, controller, processor,
    retention_period, security_measures, is_active
)
VALUES 
(
    'moderation-001',
    'Server Moderation',
    'Processing user data for server moderation purposes including warnings, bans, and content filtering',
    'MODERATION',
    'Legitimate interest (maintaining server safety and order)',
    'Server Owner',
    'Axion Bot',
    '2 years from last moderation action',
    'Encrypted storage, access controls, audit logging',
    TRUE
),
(
    'analytics-001',
    'Server Analytics',
    'Processing aggregated user data for server analytics and insights',
    'ANALYTICS',
    'Legitimate interest (server optimization and management)',
    'Server Owner',
    'Axion Bot',
    '1 year from collection',
    'Data anonymization, encrypted storage, access controls',
    TRUE
),
(
    'security-001',
    'Security Monitoring',
    'Processing user data for security monitoring and threat detection',
    'SECURITY',
    'Legitimate interest (protecting server and users from security threats)',
    'Server Owner',
    'Axion Bot',
    '3 years from incident',
    'Encrypted storage, access controls, audit logging, data minimization',
    TRUE
);

-- Create views for easier data access

-- View for active user consents with purposes
CREATE OR REPLACE VIEW active_user_consents AS
SELECT 
    uc.id,
    uc.user_id,
    uc.guild_id,
    uc.consent_timestamp,
    uc.consent_method,
    uc.is_active,
    GROUP_CONCAT(ucp.purpose) as purposes
FROM user_consent uc
LEFT JOIN user_consent_purposes ucp ON uc.id = ucp.consent_id
WHERE uc.is_active = TRUE
GROUP BY uc.id, uc.user_id, uc.guild_id, uc.consent_timestamp, uc.consent_method, uc.is_active;

-- View for complete data processing activities
CREATE OR REPLACE VIEW complete_processing_activities AS
SELECT 
    dpa.id,
    dpa.activity_id,
    dpa.name,
    dpa.description,
    dpa.purpose,
    dpa.legal_basis,
    dpa.controller,
    dpa.processor,
    dpa.retention_period,
    dpa.security_measures,
    dpa.is_active,
    dpa.created_at,
    GROUP_CONCAT(DISTINCT dpac.data_category) as data_categories,
    GROUP_CONCAT(DISTINCT dpas.data_subject) as data_subjects,
    GROUP_CONCAT(DISTINCT dpar.recipient) as recipients
FROM data_processing_activities dpa
LEFT JOIN data_processing_activity_categories dpac ON dpa.id = dpac.activity_id
LEFT JOIN data_processing_activity_subjects dpas ON dpa.id = dpas.activity_id
LEFT JOIN data_processing_activity_recipients dpar ON dpa.id = dpar.activity_id
GROUP BY dpa.id, dpa.activity_id, dpa.name, dpa.description, dpa.purpose, 
         dpa.legal_basis, dpa.controller, dpa.processor, dpa.retention_period, 
         dpa.security_measures, dpa.is_active, dpa.created_at;

-- View for retention policy summary
CREATE OR REPLACE VIEW retention_policy_summary AS
SELECT 
    drp.id,
    drp.policy_id,
    drp.name,
    drp.description,
    drp.legal_basis,
    drp.is_active,
    drp.created_at,
    GROUP_CONCAT(DISTINCT CONCAT(drpr.purpose, ':', drpr.retention_days, 'd')) as retention_rules,
    GROUP_CONCAT(DISTINCT drpdt.data_type) as data_types
FROM data_retention_policies drp
LEFT JOIN data_retention_policy_rules drpr ON drp.id = drpr.policy_id
LEFT JOIN data_retention_policy_data_types drpdt ON drp.id = drpdt.policy_id
GROUP BY drp.id, drp.policy_id, drp.name, drp.description, drp.legal_basis, drp.is_active, drp.created_at;

-- Indexes for performance optimization
CREATE INDEX IF NOT EXISTS idx_gdpr_audit_log_composite ON gdpr_audit_log(user_id, guild_id, timestamp);
CREATE INDEX IF NOT EXISTS idx_deletion_requests_composite ON data_deletion_requests(user_id, guild_id, status);
CREATE INDEX IF NOT EXISTS idx_export_requests_composite ON data_export_requests(user_id, guild_id, status);

-- Add comments to tables for documentation
ALTER TABLE user_consent COMMENT = 'Stores user consent records for GDPR compliance';
ALTER TABLE user_consent_purposes COMMENT = 'Stores specific purposes for user consent';
ALTER TABLE data_retention_policies COMMENT = 'Stores data retention policies';
ALTER TABLE data_processing_activities COMMENT = 'Stores GDPR Article 30 processing activities';
ALTER TABLE gdpr_audit_log COMMENT = 'Audit trail for GDPR-related actions';
ALTER TABLE data_deletion_requests COMMENT = 'User requests for data deletion/anonymization';
ALTER TABLE data_export_requests COMMENT = 'User requests for data export';
ALTER TABLE gdpr_settings COMMENT = 'GDPR configuration settings per guild';