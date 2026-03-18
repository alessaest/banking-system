INSERT INTO "user" (id, username, password, email, fullName, role, createdAt, updatedAt) VALUES
                 (nextval('user_SEQ'), 'admin',         '$2a$12$N8xiWpyng.Lq.lKbimH6QeQk10SJI9Xay77H2veVQR.x6FxNmlmbe', 'admin@bank.com',   'System Admin',  'admin', NOW(), NOW()),
                 (nextval('user_SEQ'), 'alice_johnson', '$2a$12$N6Cg6995jHLW/F2MzsQ5LekL3hOGZ84YmCJXxV.QsaY6EPiHFJToK', 'alice@bank.com',   'Alice Johnson', 'user',  NOW(), NOW()),
                 (nextval('user_SEQ'), 'bob_smith',     '$2a$12$N6Cg6995jHLW/F2MzsQ5LekL3hOGZ84YmCJXxV.QsaY6EPiHFJToK', 'bob@bank.com',     'Bob Smith',     'user',  NOW(), NOW()),
                 (nextval('user_SEQ'), 'charlie_brown', '$2a$12$N6Cg6995jHLW/F2MzsQ5LekL3hOGZ84YmCJXxV.QsaY6EPiHFJToK', 'charlie@bank.com', 'Charlie Brown', 'user',  NOW(), NOW());

-- admin   = id 1
-- alice   = id 2
-- bob     = id 3
-- charlie = id 4

INSERT INTO account (id, accountNumber, balance, accountType, user_id, createdAt, updatedAt) VALUES
                 (nextval('account_SEQ'), 'ALICE001DEBIT001', 5000.00,  'DEBIT',  2, NOW(), NOW()),
                 (nextval('account_SEQ'), 'ALICE002CREDIT01', 10000.00, 'CREDIT', 2, NOW(), NOW()),
                 (nextval('account_SEQ'), 'BOB00001DEBIT001', 3500.00,  'DEBIT',  3, NOW(), NOW()),
                 (nextval('account_SEQ'), 'CHARLIE01CREDIT1', 5000.00,  'CREDIT', 4, NOW(), NOW());

-- Alice DEBIT    = id 1
-- Alice CREDIT   = id 2
-- Bob DEBIT      = id 3
-- Charlie CREDIT = id 4

INSERT INTO transaction (id, from_account_id, to_account_id, amount, type, status, dateTime, description) VALUES
                  (nextval('transaction_SEQ'), 1,    3,    500.00,  'TRANSFER',   'Completed', NOW(), 'Transfer from Alice to Bob'),
                  (nextval('transaction_SEQ'), NULL, 1,    1000.00, 'DEPOSIT',    'Completed', NOW(), 'Deposit into Alice DEBIT'),
                  (nextval('transaction_SEQ'), 4,    NULL, 250.00,  'WITHDRAWAL', 'Completed', NOW(), 'Withdrawal from Charlie CREDIT');