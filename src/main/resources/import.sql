-- ─────────────────────────────────────────────────────────────────────────────
-- Banking API — Seed Data
-- Schema changes reflected here:
--   • "user" table: fullName replaced by firstName + lastName
--   • transaction table: userId column added (the initiating user's ID)
-- ─────────────────────────────────────────────────────────────────────────────

-- ── Users ─────────────────────────────────────────────────────────────────────
-- Passwords (all users except admin share the same hash for convenience):
--   admin         → "admin123"
--   alice_johnson → "password1"
--   bob_smith     → "password1"
--   charlie_brown → "password1"
--   diana_prince  → "password1"

INSERT INTO "user" (id, username, password, email, firstName, lastName, role, createdAt, updatedAt) VALUES
        (nextval('user_SEQ'), 'admin',         '$2a$12$N8xiWpyng.Lq.lKbimH6QeQk10SJI9Xay77H2veVQR.x6FxNmlmbe', 'admin@bank.com',   'System',   'Admin',  'admin', NOW(), NOW()),
        (nextval('user_SEQ'), 'alice_johnson', '$2a$12$N6Cg6995jHLW/F2MzsQ5LekL3hOGZ84YmCJXxV.QsaY6EPiHFJToK', 'alice@bank.com',   'Alice',    'Johnson','user',  NOW(), NOW()),
        (nextval('user_SEQ'), 'bob_smith',     '$2a$12$N6Cg6995jHLW/F2MzsQ5LekL3hOGZ84YmCJXxV.QsaY6EPiHFJToK', 'bob@bank.com',     'Bob',      'Smith',  'user',  NOW(), NOW()),
        (nextval('user_SEQ'), 'charlie_brown', '$2a$12$N6Cg6995jHLW/F2MzsQ5LekL3hOGZ84YmCJXxV.QsaY6EPiHFJToK', 'charlie@bank.com', 'Charlie',  'Brown',  'user',  NOW(), NOW()),
        (nextval('user_SEQ'), 'diana_prince',  '$2a$12$N6Cg6995jHLW/F2MzsQ5LekL3hOGZ84YmCJXxV.QsaY6EPiHFJToK', 'diana@bank.com',   'Diana',    'Prince', 'user',  NOW(), NOW());

-- Expected IDs (sequence starts at 1):
--   id 1 → admin
--   id 2 → alice_johnson
--   id 3 → bob_smith
--   id 4 → charlie_brown
--   id 5 → diana_prince


-- ── Accounts ──────────────────────────────────────────────────────────────────
-- Alice  : DEBIT + CREDIT  (both account types)
-- Bob    : DEBIT only
-- Charlie: CREDIT only
-- Diana  : DEBIT + CREDIT  (both account types)
-- Admin  : no personal accounts

INSERT INTO account (id, accountNumber, balance, creditLimit, accountType, user_id, createdAt, updatedAt) VALUES
              (nextval('account_SEQ'), 'ALICE001DEBIT001', 5000.00,     NULL,     'DEBIT',  2, NOW(), NOW()),
              (nextval('account_SEQ'), 'ALICE002CREDIT01',  800.00,  1000.00,     'CREDIT', 2, NOW(), NOW()),
              (nextval('account_SEQ'), 'BOB00001DEBIT001', 3500.00,     NULL,     'DEBIT',  3, NOW(), NOW()),
              (nextval('account_SEQ'), 'CHARLIE01CREDIT1',  200.00,   500.00,     'CREDIT', 4, NOW(), NOW()),
              (nextval('account_SEQ'), 'DIANA001DEBIT001', 8200.00,     NULL,     'DEBIT',  5, NOW(), NOW()),
              (nextval('account_SEQ'), 'DIANA002CREDIT01',    0.00,  2000.00,     'CREDIT', 5, NOW(), NOW());

-- Expected IDs (sequence starts at 1):
--   id 1 → Alice  DEBIT   balance: 5000.00
--   id 2 → Alice  CREDIT  balance:  800.00  limit: 1000.00
--   id 3 → Bob    DEBIT   balance: 3500.00
--   id 4 → Charlie CREDIT balance:  200.00  limit:  500.00
--   id 5 → Diana  DEBIT   balance: 8200.00
--   id 6 → Diana  CREDIT  balance:    0.00  limit: 2000.00


-- ── Transactions ──────────────────────────────────────────────────────────────
-- userId column records the initiating user:
--   TRANSFER   → the sender's userId
--   DEPOSIT    → the depositor's userId (account owner)
--   WITHDRAWAL → the withdrawer's userId (account owner)

INSERT INTO transaction (id, from_account_id, to_account_id, userId, amount, type, status, dateTime, description) VALUES
                      -- Alice (userId=2) transfers 500 to Bob (account 3)
                      (nextval('transaction_SEQ'), 1,    3,    2, 500.00,  'TRANSFER',   'Completed', NOW() - INTERVAL '5 days',  'Transfer from Alice to Bob'),

                      -- Alice (userId=2) deposits 1000 into her own DEBIT account (account 1)
                      (nextval('transaction_SEQ'), NULL, 1,    2, 1000.00, 'DEPOSIT',    'Completed', NOW() - INTERVAL '4 days',  'Deposit into Alice DEBIT'),

                      -- Charlie (userId=4) withdraws 250 from his CREDIT account (account 4)
                      (nextval('transaction_SEQ'), 4,    NULL, 4,  250.00, 'WITHDRAWAL', 'Completed', NOW() - INTERVAL '3 days',  'Withdrawal from Charlie CREDIT'),

                      -- Bob (userId=3) deposits 700 into his DEBIT account (account 3)
                      (nextval('transaction_SEQ'), NULL, 3,    3,  700.00, 'DEPOSIT',    'Completed', NOW() - INTERVAL '3 days',  'Deposit into Bob DEBIT'),

                      -- Diana (userId=5) transfers 1200 to Alice DEBIT (account 1)
                      (nextval('transaction_SEQ'), 5,    1,    5, 1200.00, 'TRANSFER',   'Completed', NOW() - INTERVAL '2 days',  'Transfer from Diana to Alice'),

                      -- Alice (userId=2) deposits 800 into her CREDIT account (account 2)
                      (nextval('transaction_SEQ'), NULL, 2,    2,  800.00, 'DEPOSIT',    'Completed', NOW() - INTERVAL '1 day',   'Alice CREDIT deposit'),

                      -- Diana (userId=5) deposits into her CREDIT account (account 6)
                      (nextval('transaction_SEQ'), NULL, 6,    5,  500.00, 'DEPOSIT',    'Completed', NOW() - INTERVAL '12 hours','Diana CREDIT deposit'),

                      -- Bob (userId=3) withdraws 300 from his DEBIT account (account 3)
                      (nextval('transaction_SEQ'), 3,    NULL, 3,  300.00, 'WITHDRAWAL', 'Completed', NOW() - INTERVAL '6 hours', 'Withdrawal from Bob DEBIT'),

                      -- Charlie (userId=4) deposits 150 into his CREDIT account (account 4)
                      (nextval('transaction_SEQ'), NULL, 4,    4,  150.00, 'DEPOSIT',    'Completed', NOW() - INTERVAL '2 hours', 'Charlie CREDIT deposit'),

                      -- Alice (userId=2) withdraws 200 from her DEBIT account (account 1)
                      (nextval('transaction_SEQ'), 1,    NULL, 2,  200.00, 'WITHDRAWAL', 'Completed', NOW() - INTERVAL '1 hour',  'Withdrawal from Alice DEBIT');