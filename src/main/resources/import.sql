-- ─────────────────────────────────────────────────────────────────────────────
-- Banking API — Seed Data
-- ─────────────────────────────────────────────────────────────────────────────

-- ── Users ─────────────────────────────────────────────────────────────────────
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


-- ── Accounts (DEBIT, CREDIT, and SAVINGS with Interest Rates) ──────────────────
INSERT INTO account (id, accountNumber, balance, creditLimit, accountType, user_id, interestRate, lastInterestCalculatedAt, createdAt, updatedAt) VALUES
    -- Alice (user 2): 1 Debit + 1 Credit + 1 Savings
    (nextval('account_SEQ'), 'ALICE001DEBIT001', 5000.00,     5000.00,     'DEBIT',  2, NULL, NULL, NOW(), NOW()),
    (nextval('account_SEQ'), 'ALICE002CREDIT01',  800.00,  1000.00,     'CREDIT', 2, NULL, NULL, NOW(), NOW()),
    (nextval('account_SEQ'), 'ALICE003SAVING01', 25000.00,    NULL,     'SAVINGS', 2, 2.5, NOW(), NOW(), NOW()),

    -- Bob (user 3): 1 Debit + 1 Savings
    (nextval('account_SEQ'), 'BOB00001DEBIT001', 3500.00,     5000.00,     'DEBIT',  3, NULL, NULL, NOW(), NOW()),
    (nextval('account_SEQ'), 'BOB00002SAVING01', 18500.00,    NULL,     'SAVINGS', 3, 2.5, NOW(), NOW(), NOW()),

    -- Charlie (user 4): 1 Credit + 1 Savings
    (nextval('account_SEQ'), 'CHARLIE01CREDIT1',  200.00,   500.00,     'CREDIT', 4, NULL, NULL, NOW(), NOW()),
    (nextval('account_SEQ'), 'CHARLIE01SAVING01', 12000.00,    NULL,     'SAVINGS', 4, 2.5, NOW(), NOW(), NOW()),

    -- Diana (user 5): 1 Debit + 1 Credit + 1 Savings
    (nextval('account_SEQ'), 'DIANA001DEBIT001', 8200.00,     10000.00,     'DEBIT',  5, NULL, NULL, NOW(), NOW()),
    (nextval('account_SEQ'), 'DIANA002CREDIT01',    0.00,  2000.00,     'CREDIT', 5, NULL, NULL, NOW(), NOW()),
    (nextval('account_SEQ'), 'DIANA003SAVING01', 50000.00,    NULL,     'SAVINGS', 5, 2.5, NOW(), NOW(), NOW()),

    -- Hanna (user 6): 1 Debit + 1 Credit + 1 Savings
    (nextval('account_SEQ'), 'HANNA001DEBIT001', 15200.00,     20000.00,     'DEBIT',  6, NULL, NULL, NOW(), NOW()),
    (nextval('account_SEQ'), 'HANNA002CREDIT01',    0.00,  2000.00,     'CREDIT', 6, NULL, NULL, NOW(), NOW()),
    (nextval('account_SEQ'), 'HANNA003SAVING01', 75000.00,    NULL,     'SAVINGS', 6, 2.5, NOW(), NOW(), NOW()),

    -- Sarah (user 7): 1 Debit + 1 Credit + 1 Savings
    (nextval('account_SEQ'), 'SARAH001DEBIT001', 8200.00,     10000.00,     'DEBIT',  7, NULL, NULL, NOW(), NOW()),
    (nextval('account_SEQ'), 'SARAH002CREDIT01',    0.00,  2000.00,     'CREDIT', 7, NULL, NULL, NOW(), NOW()),
    (nextval('account_SEQ'), 'SARAH003SAVING01', 35000.00,    NULL,     'SAVINGS', 7, 2.5, NOW(), NOW(), NOW()),

    -- John (user 8): 1 Debit + 1 Credit + 1 Savings
    (nextval('account_SEQ'), 'JOHN0001DEBIT001', 12500.00,     15000.00,    'DEBIT',  8, NULL, NULL, NOW(), NOW()),
    (nextval('account_SEQ'), 'JOHN0002CREDIT01',  1500.00,  3000.00,     'CREDIT', 8, NULL, NULL, NOW(), NOW()),
    (nextval('account_SEQ'), 'JOHN0003SAVING01', 42000.00,    NULL,     'SAVINGS', 8, 2.5, NOW(), NOW(), NOW()),

    -- Jane (user 9): 1 Debit + 1 Credit + 1 Savings
    (nextval('account_SEQ'), 'JANE0001DEBIT001', 6750.00,     10000.00,    'DEBIT',  9, NULL, NULL, NOW(), NOW()),
    (nextval('account_SEQ'), 'JANE0002CREDIT01',   500.00,  1500.00,     'CREDIT', 9, NULL, NULL, NOW(), NOW()),
    (nextval('account_SEQ'), 'JANE0003SAVING01', 28000.00,    NULL,     'SAVINGS', 9, 2.5, NOW(), NOW(), NOW()),

    -- Michael (user 10): 1 Debit + 1 Credit + 1 Savings
    (nextval('account_SEQ'), 'MICHAEL01DEBIT001', 22000.00,     25000.00,    'DEBIT', 10, NULL, NULL, NOW(), NOW()),
    (nextval('account_SEQ'), 'MICHAEL01CREDIT01',  3200.00,  5000.00,     'CREDIT', 10, NULL, NULL, NOW(), NOW()),
    (nextval('account_SEQ'), 'MICHAEL01SAVING01', 150000.00,    NULL,     'SAVINGS', 10, 2.5, NOW(), NOW(), NOW()),

    -- Emma (user 11): 1 Debit + 1 Credit + 1 Savings
    (nextval('account_SEQ'), 'EMMA0001DEBIT001', 4300.00,     8000.00,    'DEBIT', 11, NULL, NULL, NOW(), NOW()),
    (nextval('account_SEQ'), 'EMMA0002CREDIT01',   900.00,  2000.00,     'CREDIT', 11, NULL, NULL, NOW(), NOW()),
    (nextval('account_SEQ'), 'EMMA0003SAVING01', 31500.00,    NULL,     'SAVINGS', 11, 2.5, NOW(), NOW(), NOW()),

    -- David (user 12): 1 Debit + 1 Credit + 1 Savings
    (nextval('account_SEQ'), 'DAVID001DEBIT001', 18900.00,     20000.00,    'DEBIT', 12, NULL, NULL, NOW(), NOW()),
    (nextval('account_SEQ'), 'DAVID001CREDIT01',  2100.00,  4000.00,     'CREDIT', 12, NULL, NULL, NOW(), NOW()),
    (nextval('account_SEQ'), 'DAVID001SAVING01', 95000.00,    NULL,     'SAVINGS', 12, 2.5, NOW(), NOW(), NOW());


-- ── Transactions ─────────────────────────────────────────────────────────────
INSERT INTO transaction (id, from_account_id, to_account_id, userId, amount, type, status, dateTime, description) VALUES
    -- Original DEBIT/CREDIT transactions
    (nextval('transaction_SEQ'), 1,    4,    2, 500.00,  'TRANSFER',   'Completed', NOW() - INTERVAL '5 days',  'Transfer from Alice to Bob'),
    (nextval('transaction_SEQ'), NULL, 1,    2, 1000.00, 'DEPOSIT',    'Completed', NOW() - INTERVAL '4 days',  'Deposit into Alice DEBIT'),
    (nextval('transaction_SEQ'), 6,    NULL, 4,  250.00, 'WITHDRAWAL', 'Completed', NOW() - INTERVAL '3 days',  'Withdrawal from Charlie CREDIT'),
    (nextval('transaction_SEQ'), NULL, 4,    3,  700.00, 'DEPOSIT',    'Completed', NOW() - INTERVAL '3 days',  'Deposit into Bob DEBIT'),
    (nextval('transaction_SEQ'), 8,    1,    5, 1200.00, 'TRANSFER',   'Completed', NOW() - INTERVAL '2 days',  'Transfer from Diana to Alice'),
    (nextval('transaction_SEQ'), NULL, 2,    2,  800.00, 'DEPOSIT',    'Completed', NOW() - INTERVAL '1 day',   'Alice CREDIT deposit'),
    (nextval('transaction_SEQ'), NULL, 9,    5,  500.00, 'DEPOSIT',    'Completed', NOW() - INTERVAL '12 hours','Diana CREDIT deposit'),
    (nextval('transaction_SEQ'), 4,    NULL, 3,  300.00, 'WITHDRAWAL', 'Completed', NOW() - INTERVAL '6 hours', 'Withdrawal from Bob DEBIT'),
    (nextval('transaction_SEQ'), NULL, 6,    4,  150.00, 'DEPOSIT',    'Completed', NOW() - INTERVAL '2 hours', 'Charlie CREDIT deposit'),
    (nextval('transaction_SEQ'), 1,    NULL, 2,  200.00, 'WITHDRAWAL', 'Completed', NOW() - INTERVAL '1 hour',  'Withdrawal from Alice DEBIT'),

    -- Savings account deposits
    (nextval('transaction_SEQ'), NULL, 3,    2, 5000.00, 'DEPOSIT',    'Completed', NOW() - INTERVAL '30 days',  'Deposit to Alice SAVINGS'),
    (nextval('transaction_SEQ'), NULL, 5,    3, 3500.00, 'DEPOSIT',    'Completed', NOW() - INTERVAL '25 days',  'Deposit to Bob SAVINGS'),
    (nextval('transaction_SEQ'), NULL, 7,    4, 2000.00, 'DEPOSIT',    'Completed', NOW() - INTERVAL '20 days',  'Deposit to Charlie SAVINGS'),
    (nextval('transaction_SEQ'), NULL, 10,   5, 10000.00, 'DEPOSIT',    'Completed', NOW() - INTERVAL '15 days',  'Deposit to Diana SAVINGS'),

    -- Sample interest transactions
    (nextval('transaction_SEQ'), NULL, 3,    2, 72.92,  'INTEREST',   'Completed', NOW() - INTERVAL '10 days',  'Monthly interest (2.5% annual)'),
    (nextval('transaction_SEQ'), NULL, 5,    3, 46.25,  'INTEREST',   'Completed', NOW() - INTERVAL '10 days',  'Monthly interest (2.5% annual)'),
    (nextval('transaction_SEQ'), NULL, 7,    4, 25.00,  'INTEREST',   'Completed', NOW() - INTERVAL '10 days',  'Monthly interest (2.5% annual)'),
    (nextval('transaction_SEQ'), NULL, 10,   5, 166.67, 'INTEREST',   'Completed', NOW() - INTERVAL '10 days',  'Monthly interest (2.5% annual)');
