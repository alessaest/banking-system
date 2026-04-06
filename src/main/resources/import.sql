-- ─────────────────────────────────────────────────────────────────────────────
-- Banking API — Seed Data
-- Schema changes reflected here:
--   • "user" table: fullName replaced by firstName + lastName
--   • transaction table: userId column added (the initiating user's ID)
--   • FIXED: User ID assignments for accounts (Diana=5, Hanna=6, Sarah=7)
-- ─────────────────────────────────────────────────────────────────────────────

-- ── Users ─────────────────────────────────────────────────────────────────────
-- Passwords (all users except admin share the same hash for convenience):

INSERT INTO "user" (id, username, password, email, firstName, lastName, role, createdAt, updatedAt) VALUES
    (nextval('user_SEQ'), 'admin',         '$2a$12$N8xiWpyng.Lq.lKbimH6QeQk10SJI9Xay77H2veVQR.x6FxNmlmbe', 'admin@bank.com',   'System',   'Admin',  'admin', NOW(), NOW()),
    (nextval('user_SEQ'), 'alice_johnson', '$2a$12$N6Cg6995jHLW/F2MzsQ5LekL3hOGZ84YmCJXxV.QsaY6EPiHFJToK', 'alice@bank.com',   'Alice',    'Johnson','user',  NOW(), NOW()),
    (nextval('user_SEQ'), 'bob_smith',     '$2a$12$N6Cg6995jHLW/F2MzsQ5LekL3hOGZ84YmCJXxV.QsaY6EPiHFJToK', 'bob@bank.com',     'Bob',      'Smith',  'user',  NOW(), NOW()),
    (nextval('user_SEQ'), 'charlie_brown', '$2a$12$N6Cg6995jHLW/F2MzsQ5LekL3hOGZ84YmCJXxV.QsaY6EPiHFJToK', 'charlie@bank.com', 'Charlie',  'Brown',  'user',  NOW(), NOW()),
    (nextval('user_SEQ'), 'diana_prince',  '$2a$12$N6Cg6995jHLW/F2MzsQ5LekL3hOGZ84YmCJXxV.QsaY6EPiHFJToK', 'diana@bank.com',   'Diana',    'Prince', 'user',  NOW(), NOW()),
    (nextval('user_SEQ'), 'hanna_kincaid', '$2a$12$N6Cg6995jHLW/F2MzsQ5LekL3hOGZ84YmCJXxV.QsaY6EPiHFJToK', 'hanna@bank.com', 'Hanna',  'Kincaid',  'user',  NOW(), NOW()),
    (nextval('user_SEQ'), 'sarah_kim',  '$2a$12$N6Cg6995jHLW/F2MzsQ5LekL3hOGZ84YmCJXxV.QsaY6EPiHFJToK', 'sarah@bank.com',   'Sarah',    'Kim', 'user',  NOW(), NOW()),
    (nextval('user_SEQ'), 'john_doe',     '$2a$12$N6Cg6995jHLW/F2MzsQ5LekL3hOGZ84YmCJXxV.QsaY6EPiHFJToK', 'john@bank.com',    'John',     'Doe',    'user',  NOW(), NOW()),
    (nextval('user_SEQ'), 'jane_smith',   '$2a$12$N6Cg6995jHLW/F2MzsQ5LekL3hOGZ84YmCJXxV.QsaY6EPiHFJToK', 'jane@bank.com',    'Jane',     'Smith',  'user',  NOW(), NOW()),
    (nextval('user_SEQ'), 'michael_jones','$2a$12$N6Cg6995jHLW/F2MzsQ5LekL3hOGZ84YmCJXxV.QsaY6EPiHFJToK', 'michael@bank.com', 'Michael',  'Jones',  'user',  NOW(), NOW()),
    (nextval('user_SEQ'), 'emma_wilson',  '$2a$12$N6Cg6995jHLW/F2MzsQ5LekL3hOGZ84YmCJXxV.QsaY6EPiHFJToK', 'emma@bank.com',    'Emma',     'Wilson', 'user',  NOW(), NOW()),
    (nextval('user_SEQ'), 'david_miller', '$2a$12$N6Cg6995jHLW/F2MzsQ5LekL3hOGZ84YmCJXxV.QsaY6EPiHFJToK', 'david@bank.com',   'David',    'Miller', 'user',  NOW(), NOW());


-- ── Accounts ──────────────────
INSERT INTO account (id, accountNumber, balance, creditLimit, accountType, user_id, createdAt, updatedAt) VALUES
    (nextval('account_SEQ'), 'ALICE001DEBIT001', 5000.00,     5000.00,     'DEBIT',  2, NOW(), NOW()),
    (nextval('account_SEQ'), 'ALICE002CREDIT01',  800.00,  1000.00,     'CREDIT', 2, NOW(), NOW()),
    (nextval('account_SEQ'), 'BOB00001DEBIT001', 3500.00,     5000.00,     'DEBIT',  3, NOW(), NOW()),
    (nextval('account_SEQ'), 'CHARLIE01CREDIT1',  200.00,   500.00,     'CREDIT', 4, NOW(), NOW()),
    (nextval('account_SEQ'), 'DIANA001DEBIT001', 8200.00,     10000.00,     'DEBIT',  5, NOW(), NOW()),
    (nextval('account_SEQ'), 'DIANA002CREDIT01',    0.00,  2000.00,     'CREDIT', 5, NOW(), NOW()),
    (nextval('account_SEQ'), 'HANNA001DEBIT001', 15200.00,     20000.00,     'DEBIT',  6, NOW(), NOW()),
    (nextval('account_SEQ'), 'HANNA002CREDIT01',    0.00,  2000.00,     'CREDIT', 6, NOW(), NOW()),
    (nextval('account_SEQ'), 'SARAH001DEBIT001', 8200.00,     10000.00,     'DEBIT',  7, NOW(), NOW()),
    (nextval('account_SEQ'), 'SARAH002CREDIT01',    0.00,  2000.00,     'CREDIT', 7, NOW(), NOW()),
    (nextval('account_SEQ'), 'JOHN0001DEBIT001', 12500.00,     15000.00,    'DEBIT',  8, NOW(), NOW()),
    (nextval('account_SEQ'), 'JOHN0002CREDIT01',  1500.00,  3000.00,     'CREDIT', 8, NOW(), NOW()),
    (nextval('account_SEQ'), 'JANE0001DEBIT001', 6750.00,     10000.00,    'DEBIT',  9, NOW(), NOW()),
    (nextval('account_SEQ'), 'JANE0002CREDIT01',   500.00,  1500.00,     'CREDIT', 9, NOW(), NOW()),
    (nextval('account_SEQ'), 'MICHAEL01DEBIT001', 22000.00,     25000.00,    'DEBIT', 10, NOW(), NOW()),
    (nextval('account_SEQ'), 'MICHAEL01CREDIT01',  3200.00,  5000.00,     'CREDIT', 10, NOW(), NOW()),
    (nextval('account_SEQ'), 'EMMA0001DEBIT001', 4300.00,     8000.00,    'DEBIT', 11, NOW(), NOW()),
    (nextval('account_SEQ'), 'EMMA0002CREDIT01',   900.00,  2000.00,     'CREDIT', 11, NOW(), NOW()),
    (nextval('account_SEQ'), 'DAVID001DEBIT001', 18900.00,     20000.00,    'DEBIT', 12, NOW(), NOW()),
    (nextval('account_SEQ'), 'DAVID001CREDIT01',  2100.00,  4000.00,     'CREDIT', 12, NOW(), NOW());


-- ── Transactions ────────────────────────────────────────────────────
-- userId column records the initiating user:
--   TRANSFER   → the sender's userId
--   DEPOSIT    → the depositor's userId (account owner)
--   WITHDRAWAL → the withdrawer's userId (account owner)

INSERT INTO transaction (id, from_account_id, to_account_id, userId, amount, type, status, dateTime, description) VALUES
    (nextval('transaction_SEQ'), 1,    3,    2, 500.00,  'TRANSFER',   'Completed', NOW() - INTERVAL '5 days',  'Transfer from Alice to Bob'),
    (nextval('transaction_SEQ'), NULL, 1,    2, 1000.00, 'DEPOSIT',    'Completed', NOW() - INTERVAL '4 days',  'Deposit into Alice DEBIT'),
    (nextval('transaction_SEQ'), 4,    NULL, 4,  250.00, 'WITHDRAWAL', 'Completed', NOW() - INTERVAL '3 days',  'Withdrawal from Charlie CREDIT'),
    (nextval('transaction_SEQ'), NULL, 3,    3,  700.00, 'DEPOSIT',    'Completed', NOW() - INTERVAL '3 days',  'Deposit into Bob DEBIT'),
    (nextval('transaction_SEQ'), 5,    1,    5, 1200.00, 'TRANSFER',   'Completed', NOW() - INTERVAL '2 days',  'Transfer from Diana to Alice'),
    (nextval('transaction_SEQ'), NULL, 2,    2,  800.00, 'DEPOSIT',    'Completed', NOW() - INTERVAL '1 day',   'Alice CREDIT deposit'),
    (nextval('transaction_SEQ'), NULL, 6,    5,  500.00, 'DEPOSIT',    'Completed', NOW() - INTERVAL '12 hours','Diana CREDIT deposit'),
    (nextval('transaction_SEQ'), 3,    NULL, 3,  300.00, 'WITHDRAWAL', 'Completed', NOW() - INTERVAL '6 hours', 'Withdrawal from Bob DEBIT'),
    (nextval('transaction_SEQ'), NULL, 4,    4,  150.00, 'DEPOSIT',    'Completed', NOW() - INTERVAL '2 hours', 'Charlie CREDIT deposit'),
    (nextval('transaction_SEQ'), 1,    NULL, 2,  200.00, 'WITHDRAWAL', 'Completed', NOW() - INTERVAL '1 hour',  'Withdrawal from Alice DEBIT'),

    -- NEW TRANSACTIONS FOR TESTING
    -- John's transactions
    (nextval('transaction_SEQ'), NULL, 11,   8, 5000.00, 'DEPOSIT',    'Completed', NOW() - INTERVAL '10 days', 'John DEBIT deposit'),
    (nextval('transaction_SEQ'), 11,   NULL, 8, 1500.00, 'WITHDRAWAL', 'Completed', NOW() - INTERVAL '9 days',  'John DEBIT withdrawal'),
    (nextval('transaction_SEQ'), 11,   3,    8, 2000.00, 'TRANSFER',   'Completed', NOW() - INTERVAL '7 days',  'Transfer from John to Bob'),

    -- Jane's transactions
    (nextval('transaction_SEQ'), NULL, 13,   9, 3500.00, 'DEPOSIT',    'Completed', NOW() - INTERVAL '8 days',  'Jane DEBIT deposit'),
    (nextval('transaction_SEQ'), 13,   NULL, 9, 800.00,  'WITHDRAWAL', 'Completed', NOW() - INTERVAL '7 days',  'Jane DEBIT withdrawal'),

    -- Michael's large transactions
    (nextval('transaction_SEQ'), 15,   NULL, 10, 5000.00, 'WITHDRAWAL', 'Completed', NOW() - INTERVAL '6 days',  'Michael DEBIT withdrawal'),
    (nextval('transaction_SEQ'), NULL, 15,   10, 10000.00,'DEPOSIT',    'Completed', NOW() - INTERVAL '5 days',  'Michael DEBIT deposit'),
    (nextval('transaction_SEQ'), 15,   1,    10, 3500.00, 'TRANSFER',   'Completed', NOW() - INTERVAL '4 days',  'Transfer from Michael to Alice'),

    -- Emma's transactions
    (nextval('transaction_SEQ'), NULL, 17,   11, 2000.00, 'DEPOSIT',    'Completed', NOW() - INTERVAL '6 days',  'Emma DEBIT deposit'),
    (nextval('transaction_SEQ'), 17,   3,    11, 1500.00, 'TRANSFER',   'Completed', NOW() - INTERVAL '5 days',  'Transfer from Emma to Bob'),

    -- David's transactions
    (nextval('transaction_SEQ'), NULL, 19,   12, 7500.00, 'DEPOSIT',    'Completed', NOW() - INTERVAL '4 days',  'David DEBIT deposit'),
    (nextval('transaction_SEQ'), 19,   13,   12, 4200.00, 'TRANSFER',   'Completed', NOW() - INTERVAL '3 days',  'Transfer from David to Jane'),
    (nextval('transaction_SEQ'), 19,   NULL, 12, 2000.00, 'WITHDRAWAL', 'Completed', NOW() - INTERVAL '2 days',  'David DEBIT withdrawal'),

    -- Cross-user transfers
    (nextval('transaction_SEQ'), 11,   5,    8, 1000.00, 'TRANSFER',   'Completed', NOW() - INTERVAL '8 hours',  'Transfer from John to Diana'),
    (nextval('transaction_SEQ'), 13,   9,    9, 2500.00, 'TRANSFER',   'Completed', NOW() - INTERVAL '6 hours',  'Transfer from Jane to Alice'),
    (nextval('transaction_SEQ'), 15,   7,    10, 1800.00, 'TRANSFER',   'Completed', NOW() - INTERVAL '4 hours',  'Transfer from Michael to Sarah'),

    -- Credit account transactions
    (nextval('transaction_SEQ'), NULL, 12,   8, 1000.00, 'DEPOSIT',    'Completed', NOW() - INTERVAL '3 hours',  'John CREDIT deposit'),
    (nextval('transaction_SEQ'), 12,   NULL, 8, 300.00,  'WITHDRAWAL', 'Completed', NOW() - INTERVAL '2 hours',  'John CREDIT withdrawal'),
    (nextval('transaction_SEQ'), NULL, 14,   9, 500.00,  'DEPOSIT',    'Completed', NOW() - INTERVAL '2 hours',  'Jane CREDIT deposit'),
    (nextval('transaction_SEQ'), NULL, 16,   10, 2000.00, 'DEPOSIT',    'Completed', NOW() - INTERVAL '1 hour',   'Michael CREDIT deposit'),
    (nextval('transaction_SEQ'), 16,   NULL, 10, 500.00,  'WITHDRAWAL', 'Completed', NOW() - INTERVAL '30 minutes','Michael CREDIT withdrawal'),
    (nextval('transaction_SEQ'), NULL, 18,   11, 750.00,  'DEPOSIT',    'Completed', NOW() - INTERVAL '1 hour',   'Emma CREDIT deposit'),
    (nextval('transaction_SEQ'), NULL, 20,   12, 1200.00, 'DEPOSIT',    'Completed', NOW() - INTERVAL '45 minutes','David CREDIT deposit');
