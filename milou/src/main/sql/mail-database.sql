CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE mails (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(36) NOT NULL UNIQUE,
    sender_id INT NOT NULL,
    subject VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    sent_date TIMESTAMP NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,
    deleted_by_id INT NULL,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (deleted_by_id) REFERENCES users(id) ON DELETE SET NULL
);

-- First, drop the existing index if it exists (since there's an incomplete alter statement in the original)
DROP INDEX  idx_mail_recipients_read ON mail_recipients;

-- Drop the existing table if it exists
DROP TABLE IF EXISTS mail_recipients;

-- Create the new table with the additional columns
CREATE TABLE mail_recipients (
    mail_id INT NOT NULL,
    recipient_id INT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,
    PRIMARY KEY (mail_id, recipient_id),
    FOREIGN KEY (mail_id) REFERENCES mails(id) ON DELETE CASCADE,
    FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Recreate the index for better performance on read status checks
CREATE INDEX idx_mail_recipients_read ON mail_recipients (mail_id, recipient_id, is_read);

-- First, drop the existing index if it exists (since there's an incomplete alter statement in the original)
DROP INDEX  idx_mail_recipients_read ON mail_recipients;

-- Drop the existing table if it exists
DROP TABLE  mail_recipients;

-- Create the new table with the additional columns
CREATE TABLE mail_recipients (
    mail_id INT NOT NULL,
    recipient_id INT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,
    PRIMARY KEY (mail_id, recipient_id),
    FOREIGN KEY (mail_id) REFERENCES mails(id) ON DELETE CASCADE,
    FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Recreate the index for better performance on read status checks
CREATE INDEX idx_mail_recipients_read ON mail_recipients (mail_id, recipient_id, is_read);