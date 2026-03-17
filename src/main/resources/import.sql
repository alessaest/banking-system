INSERT INTO "user" (id, username, password, email, fullname, createdat, updatedat) VALUES
    (nextval('user_SEQ'), 'alice_johnson', '$2a$12$eiQ6oefFV2TrfnRHIN.9j.YxfMDFIPhu3HrNdo5BO57Q48xos2Ooq', 'alice@bank.com',   'Alice Johnson', NOW(), NOW());

INSERT INTO "user" (id, username, password, email, fullname, createdat, updatedat) VALUES
    (nextval('user_SEQ'), 'bob_smith',     '$2a$12$eiQ6oefFV2TrfnRHIN.9j.YxfMDFIPhu3HrNdo5BO57Q48xos2Ooq', 'bob@bank.com',     'Bob Smith',     NOW(), NOW());

INSERT INTO "user" (id, username, password, email, fullname, createdat, updatedat) VALUES
    (nextval('user_SEQ'), 'charlie_brown', '$2a$12$eiQ6oefFV2TrfnRHIN.9j.YxfMDFIPhu3HrNdo5BO57Q48xos2Ooq', 'charlie@bank.com', 'Charlie Brown', NOW(), NOW());

INSERT INTO account (id, accountnumber, balance, accounttype, user_id, createdat, updatedat) VALUES
    (nextval('account_SEQ'), '1001000001', 5000.00,  'DEBIT',  1,   NOW(), NOW());

INSERT INTO account (id, accountnumber, balance, accounttype, user_id, createdat, updatedat) VALUES
    (nextval('account_SEQ'), '2001000001', 10000.00, 'CREDIT', 1,   NOW(), NOW());

INSERT INTO account (id, accountnumber, balance, accounttype, user_id, createdat, updatedat) VALUES
    (nextval('account_SEQ'), '1002000001', 3500.00,  'DEBIT',  51,  NOW(), NOW());

INSERT INTO account (id, accountnumber, balance, accounttype, user_id, createdat, updatedat) VALUES
    (nextval('account_SEQ'), '2002000001', 7500.00,  'CREDIT', 51,  NOW(), NOW());

INSERT INTO account (id, accountnumber, balance, accounttype, user_id, createdat, updatedat) VALUES
    (nextval('account_SEQ'), '1003000001', 2000.00,  'DEBIT',  101, NOW(), NOW());

INSERT INTO account (id, accountnumber, balance, accounttype, user_id, createdat, updatedat) VALUES
    (nextval('account_SEQ'), '2003000001', 5000.00,  'CREDIT', 101, NOW(), NOW());

INSERT INTO transaction (id, from_account_id, to_account_id, amount, type, status, datetime, description) VALUES
    (nextval('transaction_SEQ'), 1, 51, 500.00, 'TRANSFER', 'Completed', NOW(), 'Transfer to Bob');

INSERT INTO transaction (id, from_account_id, to_account_id, amount, type, status, datetime, description) VALUES
    (nextval('transaction_SEQ'), NULL, 51, 1000.00, 'DEPOSIT', 'Completed', NOW(), 'Deposit');

INSERT INTO transaction (id, from_account_id, to_account_id, amount, type, status, datetime, description) VALUES
    (nextval('transaction_SEQ'), NULL, 101, 2000.00, 'DEPOSIT', 'Completed', NOW(), 'Deposit');

INSERT INTO transaction (id, from_account_id, to_account_id, amount, type, status, datetime, description) VALUES
    (nextval('transaction_SEQ'), 101, 51, 250.00, 'TRANSFER', 'Completed', NOW(), 'Transfer to Alice');