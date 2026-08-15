-- =============================================================================
-- POR Banking System demonstration seed data
-- MariaDB 10.4+ / MySQL 8.0+
--
-- Creates exactly 50 customers, credentials, accounts, and opening-deposit
-- transactions. Customer 1 is the administrator; customers 2-50 are regular
-- customers. This script is safe to rerun and removes only its own seed rows.
--
-- Administrator:
--   Email:    admin@tesdabank.local
--   Password: banking123
--
-- Seed customers:
--   Emails:   seed002@tesdabank.local through seed050@tesdabank.local
--   Password: SeedPass#123
-- =============================================================================

USE banking_db;

SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM transactions
WHERE account_number LIKE 'ACC-SEED-%';

DELETE FROM accounts
WHERE account_number LIKE 'ACC-SEED-%';

DELETE cc
FROM customer_credentials cc
JOIN customers c ON c.customer_id = cc.customer_id
WHERE c.email = 'admin@tesdabank.local'
   OR c.email LIKE 'seed%@tesdabank.local';

DELETE FROM customers
WHERE email = 'admin@tesdabank.local'
   OR email LIKE 'seed%@tesdabank.local';

SET FOREIGN_KEY_CHECKS = 1;

DROP PROCEDURE IF EXISTS seed_banking_demo;

DELIMITER $$

CREATE PROCEDURE seed_banking_demo()
BEGIN
    DECLARE counter INT DEFAULT 1;
    DECLARE new_customer_id BIGINT;
    DECLARE seed_email VARCHAR(254);
    DECLARE seed_first_name VARCHAR(100);
    DECLARE seed_last_name VARCHAR(100);
    DECLARE seed_account_number VARCHAR(20);
    DECLARE seed_account_name VARCHAR(100);
    DECLARE seed_balance DECIMAL(15, 2);
    DECLARE seed_password_hash VARCHAR(255);
    DECLARE seed_role VARCHAR(20);

    WHILE counter <= 50 DO
        IF counter = 1 THEN
            SET seed_email = 'admin@tesdabank.local';
            SET seed_first_name = 'Super';
            SET seed_last_name = 'Admin';
            SET seed_password_hash = '$2a$10$4P5foUwZBpzyankmvkQkx.MX.SRFRy1UEgH0PctUoB//zTa5n3X.K';
            SET seed_role = 'ADMIN';
        ELSE
            SET seed_email = CONCAT('seed', LPAD(counter, 3, '0'), '@tesdabank.local');
            SET seed_first_name = CONCAT('Seed', LPAD(counter, 3, '0'));
            SET seed_last_name = 'Customer';
            SET seed_password_hash = '$2a$10$kIvcFxAjW5ESnSEDXOOLnOmqGV8LtaY5Z2ck5NXFF.eP2tWE1Om1m';
            SET seed_role = 'CUSTOMER';
        END IF;

        SET seed_account_number = CONCAT('ACC-SEED-', LPAD(counter, 4, '0'));
        SET seed_account_name = CONCAT(seed_first_name, ' ', seed_last_name);
        SET seed_balance = 10000.00 + (counter * 250.00);

        INSERT INTO customers (
            first_name, last_name, email, phone_number, date_of_birth,
            status, created_at, updated_at
        ) VALUES (
            seed_first_name,
            seed_last_name,
            seed_email,
            CONCAT('0917', LPAD(counter, 7, '0')),
            DATE_ADD('1980-01-01', INTERVAL counter DAY),
            'ACTIVE',
            UTC_TIMESTAMP(),
            UTC_TIMESTAMP()
        );

        SET new_customer_id = LAST_INSERT_ID();

        INSERT INTO customer_credentials (
            customer_id, password_hash, role, failed_login_attempts,
            locked_until, terms_accepted_at, last_login_at, created_at, updated_at
        ) VALUES (
            new_customer_id,
            seed_password_hash,
            seed_role,
            0,
            NULL,
            UTC_TIMESTAMP(),
            NULL,
            UTC_TIMESTAMP(),
            UTC_TIMESTAMP()
        );

        INSERT INTO accounts (
            customer_id, account_number, account_name, balance,
            created_at, updated_at
        ) VALUES (
            new_customer_id,
            seed_account_number,
            seed_account_name,
            seed_balance,
            UTC_TIMESTAMP(),
            UTC_TIMESTAMP()
        );

        INSERT INTO transactions (
            account_number, transaction_type, amount, balance_after,
            reference_number, remarks, created_at
        ) VALUES (
            seed_account_number,
            'DEPOSIT',
            seed_balance,
            seed_balance,
            CONCAT('TXN-SEED-', LPAD(counter, 4, '0')),
            'Seed opening deposit',
            UTC_TIMESTAMP()
        );

        SET counter = counter + 1;
    END WHILE;
END$$

DELIMITER ;

CALL seed_banking_demo();
DROP PROCEDURE seed_banking_demo;

-- Verification summary. Expected: 50 accounts, 50 credentials, 50 transactions.
SELECT COUNT(*) AS seeded_accounts
FROM accounts
WHERE account_number LIKE 'ACC-SEED-%';

SELECT cc.role, COUNT(*) AS credential_count
FROM customer_credentials cc
JOIN customers c ON c.customer_id = cc.customer_id
WHERE c.email = 'admin@tesdabank.local'
   OR c.email LIKE 'seed%@tesdabank.local'
GROUP BY cc.role
ORDER BY cc.role;

SELECT COUNT(*) AS seeded_transactions
FROM transactions
WHERE account_number LIKE 'ACC-SEED-%';
