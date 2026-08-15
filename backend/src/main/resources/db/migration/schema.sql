-- =============================================================================
-- POR Banking System - Palaging Overtime si Rodney
-- schema.sql - Database + Table Definitions
-- MySQL 8.0+
--
-- Usage:
--   mysql -u root -p < sql/schema.sql
--
-- Run this file ONCE to create the database and all tables.
-- To rebuild from scratch, run reset.sql first.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- 1. Create the database (safe to run multiple times)
-- ---------------------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS banking_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE banking_db;

-- ---------------------------------------------------------------------------
-- 2. Disable FK checks while creating tables (avoids order-dependency issues)
-- ---------------------------------------------------------------------------
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================================================
-- TABLE: customers
--
-- Registration profile for each customer. Authentication secrets are kept in
-- customer_credentials rather than mixed with personal or banking data.
-- =============================================================================
CREATE TABLE IF NOT EXISTS customers (
    customer_id     BIGINT          NOT NULL AUTO_INCREMENT
                                    COMMENT 'Surrogate primary key',
    first_name      VARCHAR(100)    NOT NULL
                                    COMMENT 'Customer legal first name',
    last_name       VARCHAR(100)    NOT NULL
                                    COMMENT 'Customer legal last name',
    email           VARCHAR(254)    NOT NULL
                                    COMMENT 'Unique login and contact email',
    phone_number    VARCHAR(30)     NOT NULL
                                    COMMENT 'Customer contact number',
    date_of_birth   DATE            NOT NULL
                                    COMMENT 'Customer date of birth',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE'
                                    COMMENT 'Registration status',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
                                    ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_customers         PRIMARY KEY (customer_id),
    CONSTRAINT uq_customers_email   UNIQUE      (email),
    CONSTRAINT chk_customers_status CHECK       (
        status IN ('ACTIVE', 'INACTIVE', 'BLOCKED')
    ),

    INDEX idx_customers_created_at (created_at)

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  AUTO_INCREMENT = 1
  COMMENT = 'Registered customer information';


-- =============================================================================
-- TABLE: customer_credentials
--
-- One-to-one authentication record for a registered customer. Only a secure
-- password hash is stored; plaintext and confirmation passwords are never kept.
-- =============================================================================
CREATE TABLE IF NOT EXISTS customer_credentials (
    credential_id          BIGINT          NOT NULL AUTO_INCREMENT,
    customer_id            BIGINT          NOT NULL,
    password_hash          VARCHAR(255)    NOT NULL,
    role                   VARCHAR(20)     NOT NULL DEFAULT 'CUSTOMER',
    failed_login_attempts  SMALLINT        NOT NULL DEFAULT 0,
    locked_until           DATETIME            NULL,
    terms_accepted_at      DATETIME        NOT NULL,
    last_login_at          DATETIME            NULL,
    created_at             DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
                                            ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_customer_credentials
        PRIMARY KEY (credential_id),
    CONSTRAINT uq_credentials_customer
        UNIQUE (customer_id),
    CONSTRAINT fk_credentials_customer
        FOREIGN KEY (customer_id) REFERENCES customers (customer_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_credentials_role
        CHECK (role IN ('CUSTOMER', 'TELLER', 'ADMIN')),
    CONSTRAINT chk_failed_login_attempts
        CHECK (failed_login_attempts >= 0)

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  AUTO_INCREMENT = 1
  COMMENT = 'Secure customer authentication data';


-- =============================================================================
-- TABLE: accounts
--
-- Master record for every bank account.
-- One row per account; balance is kept here as a running total so that
-- balance-inquiry queries never need to aggregate the transactions table.
-- =============================================================================
CREATE TABLE IF NOT EXISTS accounts (
    account_id      BIGINT          NOT NULL AUTO_INCREMENT
                                    COMMENT 'Surrogate primary key',
    customer_id     BIGINT          NOT NULL
                                    COMMENT 'Registered owner of this account',
    account_number  VARCHAR(20)     NOT NULL
                                    COMMENT 'Human-readable unique identifier (e.g. ACC-0001000001)',
    account_name    VARCHAR(100)    NOT NULL
                                    COMMENT 'Full legal name of the account holder',
    balance         DECIMAL(15, 2)  NOT NULL DEFAULT 0.00
                                    COMMENT 'Current available balance - always >= 0',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
                                    COMMENT 'Row creation timestamp (UTC)',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
                                    ON UPDATE CURRENT_TIMESTAMP
                                    COMMENT 'Last modification timestamp (auto-updated)',

    -- Constraints
    CONSTRAINT pk_accounts          PRIMARY KEY (account_id),
    CONSTRAINT uq_account_number    UNIQUE      (account_number),
    CONSTRAINT chk_balance_positive CHECK       (balance >= 0),
    CONSTRAINT fk_accounts_customer FOREIGN KEY (customer_id)
        REFERENCES customers (customer_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    -- Indexes
    INDEX idx_accounts_customer_id (customer_id),
    INDEX idx_accounts_created_at (created_at)

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  AUTO_INCREMENT = 1
  COMMENT = 'Bank account master data';

-- The uq_account_number constraint already creates an index for account-number
-- lookups. Keeping a second index on the same column would be redundant.


-- =============================================================================
-- TABLE: transactions
--
-- Append-only ledger - rows are NEVER updated or deleted after insertion.
-- Every financial event produces at least one row.
-- A fund transfer produces exactly TWO rows:
--   • TRANSFER_OUT on the sender's account
--   • TRANSFER_IN  on the receiver's account
-- Both rows reference their own generated reference_number; the sender's
-- reference_number appears in the receiver's `remarks` field for traceability.
-- =============================================================================
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id      BIGINT          NOT NULL AUTO_INCREMENT
                                        COMMENT 'Surrogate primary key',
    account_number      VARCHAR(20)     NOT NULL
                                        COMMENT 'Owning account (denormalised for query speed)',
    transaction_type    ENUM(
                            'DEPOSIT',
                            'WITHDRAW',
                            'TRANSFER_IN',
                            'TRANSFER_OUT'
                        )               NOT NULL
                                        COMMENT 'Category of financial event',
    amount              DECIMAL(15, 2)  NOT NULL
                                        COMMENT 'Absolute (positive) monetary amount',
    balance_after       DECIMAL(15, 2)  NOT NULL
                                        COMMENT 'Account balance snapshot immediately after this event',
    reference_number    VARCHAR(30)     NOT NULL
                                        COMMENT 'Globally unique business reference (TXNyyyyMMddHHmmss + seq)',
    remarks             VARCHAR(255)        NULL
                                        COMMENT 'Optional description - e.g. counterparty info for transfers',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
                                        COMMENT 'Event timestamp (UTC)',

    -- Constraints
    CONSTRAINT pk_transactions          PRIMARY KEY (transaction_id),
    CONSTRAINT uq_reference_number      UNIQUE      (reference_number),
    CONSTRAINT chk_amount_positive      CHECK       (amount > 0),
    CONSTRAINT fk_txn_account_number    FOREIGN KEY (account_number)
        REFERENCES accounts (account_number)
        ON DELETE RESTRICT   -- Prevent deleting an account that has transactions
        ON UPDATE CASCADE,   -- Propagate an account_number change automatically

    -- Indexes
    INDEX idx_txn_account_created (account_number, created_at DESC),
    INDEX idx_txn_type (transaction_type)

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  AUTO_INCREMENT = 1
  COMMENT = 'Append-only financial event ledger';

-- The uq_reference_number constraint already creates an index for receipt and
-- reconciliation lookups, so no separate reference-number index is needed.


-- ---------------------------------------------------------------------------
-- 3. Re-enable FK checks
-- ---------------------------------------------------------------------------
SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------------------
-- 4. Verification: show created objects
-- ---------------------------------------------------------------------------
SHOW TABLES;
