--      DELETE FROM tb_asset;       DELETE FROM tb_transaction;
--      DELETE FROM tb_wallet;      DELETE FROM tb_currency_quote;
--      DELETE FROM tb_currency;    DELETE FROM tb_user;
--      COMMIT;

INSERT INTO tb_user (id, email, hashedPassword, name)
VALUES ('10000000-0000-0000-0000-000000000001', 'ana.souza@example.com',
        '5c5faf6134002d61d850e9eee111661643f2b40ca5327ae75672219e7f93d002', 'Ana Beatriz Souza');

INSERT INTO tb_user (id, email, hashedPassword, name)
VALUES ('10000000-0000-0000-0000-000000000002', 'bruno.lima@example.com',
        '807f43a8f0c3650ceccebe79e4695ced9b5200ae20d254336c569d92cf5cab0e', 'Bruno Henrique Lima');

INSERT INTO tb_user (id, email, hashedPassword, name)
VALUES ('10000000-0000-0000-0000-000000000003', 'carla.oliveira@example.com',
        'ac4968de6ed59608eddca40e6bf7ada0f57fdc19b306c55b9298eaab64d3528d', 'Carla Mendes Oliveira');

INSERT INTO tb_user (id, email, hashedPassword, name)
VALUES ('10000000-0000-0000-0000-000000000004', 'diego.santos@example.com',
        '81a95f6a72b9d8ccad59bdd65196771b3d0183e1a135be53dbac80abba2d841a', 'Diego Ferreira Santos');

INSERT INTO tb_user (id, email, hashedPassword, name)
VALUES ('10000000-0000-0000-0000-000000000005', 'eduarda.martins@example.com',
        '2cbc98662028c6087ea27a9276e453af5e06e553fffa9b51704a02ca60ea25c2', 'Eduarda Rocha Martins');

INSERT INTO tb_user (id, email, hashedPassword, name)
VALUES ('10000000-0000-0000-0000-000000000006', 'felipe.costa@example.com',
        '2d8de3f2c92866531b5fb513c4adcc1869938b5dc120bae9c6ec9f1f14b56283', 'Felipe Araújo Costa');

INSERT INTO tb_currency (id, symbol, name, isFiat)
VALUES ('20000000-0000-0000-0000-000000000001', 'BRL', 'Real Brasileiro', 1);

INSERT INTO tb_currency (id, symbol, name, isFiat)
VALUES ('20000000-0000-0000-0000-000000000002', 'USD', 'Dólar Americano', 1);

INSERT INTO tb_currency (id, symbol, name, isFiat)
VALUES ('20000000-0000-0000-0000-000000000003', 'EUR', 'Euro', 1);

INSERT INTO tb_currency (id, symbol, name, isFiat)
VALUES ('20000000-0000-0000-0000-000000000004', 'BTC', 'Bitcoin', 0);

INSERT INTO tb_currency (id, symbol, name, isFiat)
VALUES ('20000000-0000-0000-0000-000000000005', 'ETH', 'Ethereum', 0);

INSERT INTO tb_currency (id, symbol, name, isFiat)
VALUES ('20000000-0000-0000-0000-000000000006', 'SOL', 'Solana', 0);

INSERT INTO tb_currency_quote (id, currency_id, baseCurrency_id, price, quote_timestamp)
VALUES ('50000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000002',
        '20000000-0000-0000-0000-000000000001', 5.40, TIMESTAMP '2026-08-01 10:00:00');

INSERT INTO tb_currency_quote (id, currency_id, baseCurrency_id, price, quote_timestamp)
VALUES ('50000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000004',
        '20000000-0000-0000-0000-000000000001', 600000.00, TIMESTAMP '2026-08-01 10:00:00');

INSERT INTO tb_currency_quote (id, currency_id, baseCurrency_id, price, quote_timestamp)
VALUES ('50000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000005',
        '20000000-0000-0000-0000-000000000001', 24000.00, TIMESTAMP '2026-08-01 10:00:00');

INSERT INTO tb_currency_quote (id, currency_id, baseCurrency_id, price, quote_timestamp)
VALUES ('50000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000002',
        '20000000-0000-0000-0000-000000000001', 5.45, TIMESTAMP '2026-09-08 10:00:00');

INSERT INTO tb_currency_quote (id, currency_id, baseCurrency_id, price, quote_timestamp)
VALUES ('50000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000003',
        '20000000-0000-0000-0000-000000000001', 6.35, TIMESTAMP '2026-09-08 10:00:00');

INSERT INTO tb_currency_quote (id, currency_id, baseCurrency_id, price, quote_timestamp)
VALUES ('50000000-0000-0000-0000-000000000006', '20000000-0000-0000-0000-000000000004',
        '20000000-0000-0000-0000-000000000001', 610000.00, TIMESTAMP '2026-09-08 10:00:00');

INSERT INTO tb_currency_quote (id, currency_id, baseCurrency_id, price, quote_timestamp)
VALUES ('50000000-0000-0000-0000-000000000007', '20000000-0000-0000-0000-000000000005',
        '20000000-0000-0000-0000-000000000001', 24500.00, TIMESTAMP '2026-09-08 10:00:00');

INSERT INTO tb_currency_quote (id, currency_id, baseCurrency_id, price, quote_timestamp)
VALUES ('50000000-0000-0000-0000-000000000008', '20000000-0000-0000-0000-000000000006',
        '20000000-0000-0000-0000-000000000001', 1100.00, TIMESTAMP '2026-09-08 10:00:00');

INSERT INTO tb_currency_quote (id, currency_id, baseCurrency_id, price, quote_timestamp)
VALUES ('50000000-0000-0000-0000-000000000009', '20000000-0000-0000-0000-000000000004',
        '20000000-0000-0000-0000-000000000002', 112000.00, TIMESTAMP '2026-09-08 10:00:00');

INSERT INTO tb_currency_quote (id, currency_id, baseCurrency_id, price, quote_timestamp)
VALUES ('50000000-0000-0000-0000-000000000010', '20000000-0000-0000-0000-000000000005',
        '20000000-0000-0000-0000-000000000002', 4500.00, TIMESTAMP '2026-09-08 10:00:00');

INSERT INTO tb_currency_quote (id, currency_id, baseCurrency_id, price, quote_timestamp)
VALUES ('50000000-0000-0000-0000-000000000011', '20000000-0000-0000-0000-000000000003',
        '20000000-0000-0000-0000-000000000002', 1.165, TIMESTAMP '2026-09-08 10:00:00');

INSERT INTO tb_currency_quote (id, currency_id, baseCurrency_id, price, quote_timestamp)
VALUES ('50000000-0000-0000-0000-000000000012', '20000000-0000-0000-0000-000000000006',
        '20000000-0000-0000-0000-000000000002', 200.00, TIMESTAMP '2026-09-08 10:00:00');

INSERT INTO tb_currency_quote (id, currency_id, baseCurrency_id, price, quote_timestamp)
VALUES ('50000000-0000-0000-0000-000000000013', '20000000-0000-0000-0000-000000000004',
        '20000000-0000-0000-0000-000000000001', 615000.00, TIMESTAMP '2026-09-09 10:00:00');

INSERT INTO tb_wallet (id, owner_id, baseCurrency_id)
VALUES ('30000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001',
        '20000000-0000-0000-0000-000000000001');

INSERT INTO tb_wallet (id, owner_id, baseCurrency_id)
VALUES ('30000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001',
        '20000000-0000-0000-0000-000000000002');

INSERT INTO tb_wallet (id, owner_id, baseCurrency_id)
VALUES ('30000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000002',
        '20000000-0000-0000-0000-000000000001');

INSERT INTO tb_wallet (id, owner_id, baseCurrency_id)
VALUES ('30000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000003',
        '20000000-0000-0000-0000-000000000001');

INSERT INTO tb_wallet (id, owner_id, baseCurrency_id)
VALUES ('30000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000004',
        '20000000-0000-0000-0000-000000000002');

INSERT INTO tb_wallet (id, owner_id, baseCurrency_id)
VALUES ('30000000-0000-0000-0000-000000000006', '10000000-0000-0000-0000-000000000005',
        '20000000-0000-0000-0000-000000000001');

INSERT INTO tb_transaction (id, wallet_id, currency_id, quantity, priceAtTime, type, executedAt)
VALUES ('60000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001',
        '20000000-0000-0000-0000-000000000001', 10000.00, 1, 'DEPOSIT', TIMESTAMP '2026-08-01 09:00:00');

INSERT INTO tb_transaction (id, wallet_id, currency_id, quantity, priceAtTime, type, executedAt)
VALUES ('60000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000001',
        '20000000-0000-0000-0000-000000000004', 0.01, 600000.00, 'BUY', TIMESTAMP '2026-08-02 10:30:00');

INSERT INTO tb_transaction (id, wallet_id, currency_id, quantity, priceAtTime, type, executedAt)
VALUES ('60000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000001',
        '20000000-0000-0000-0000-000000000005', 0.10, 24000.00, 'BUY', TIMESTAMP '2026-08-05 14:00:00');

INSERT INTO tb_transaction (id, wallet_id, currency_id, quantity, priceAtTime, type, executedAt)
VALUES ('60000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000001',
        '20000000-0000-0000-0000-000000000004', 0.004, 620000.00, 'SELL', TIMESTAMP '2026-08-20 16:45:00');

INSERT INTO tb_transaction (id, wallet_id, currency_id, quantity, priceAtTime, type, executedAt)
VALUES ('60000000-0000-0000-0000-000000000005', '30000000-0000-0000-0000-000000000002',
        '20000000-0000-0000-0000-000000000002', 2000.00, 1, 'DEPOSIT', TIMESTAMP '2026-08-03 11:00:00');

INSERT INTO tb_transaction (id, wallet_id, currency_id, quantity, priceAtTime, type, executedAt)
VALUES ('60000000-0000-0000-0000-000000000006', '30000000-0000-0000-0000-000000000002',
        '20000000-0000-0000-0000-000000000005', 0.20, 4400.00, 'BUY', TIMESTAMP '2026-08-03 11:05:00');

INSERT INTO tb_transaction (id, wallet_id, currency_id, quantity, priceAtTime, type, executedAt)
VALUES ('60000000-0000-0000-0000-000000000007', '30000000-0000-0000-0000-000000000003',
        '20000000-0000-0000-0000-000000000001', 5000.00, 1, 'DEPOSIT', TIMESTAMP '2026-08-04 08:15:00');

INSERT INTO tb_transaction (id, wallet_id, currency_id, quantity, priceAtTime, type, executedAt)
VALUES ('60000000-0000-0000-0000-000000000008', '30000000-0000-0000-0000-000000000003',
        '20000000-0000-0000-0000-000000000004', 0.005, 600000.00, 'BUY', TIMESTAMP '2026-08-04 08:20:00');

INSERT INTO tb_transaction (id, wallet_id, currency_id, quantity, priceAtTime, type, executedAt)
VALUES ('60000000-0000-0000-0000-000000000009', '30000000-0000-0000-0000-000000000003',
        '20000000-0000-0000-0000-000000000001', 1000.00, 1, 'WITHDRAW', TIMESTAMP '2026-08-25 19:00:00');

INSERT INTO tb_transaction (id, wallet_id, currency_id, quantity, priceAtTime, type, executedAt)
VALUES ('60000000-0000-0000-0000-000000000010', '30000000-0000-0000-0000-000000000004',
        '20000000-0000-0000-0000-000000000001', 3000.00, 1, 'DEPOSIT', TIMESTAMP '2026-08-10 13:00:00');

INSERT INTO tb_transaction (id, wallet_id, currency_id, quantity, priceAtTime, type, executedAt)
VALUES ('60000000-0000-0000-0000-000000000011', '30000000-0000-0000-0000-000000000004',
        '20000000-0000-0000-0000-000000000005', 0.05, 24000.00, 'BUY', TIMESTAMP '2026-08-10 13:10:00');

INSERT INTO tb_transaction (id, wallet_id, currency_id, quantity, priceAtTime, type, executedAt)
VALUES ('60000000-0000-0000-0000-000000000012', '30000000-0000-0000-0000-000000000005',
        '20000000-0000-0000-0000-000000000002', 5000.00, 1, 'DEPOSIT', TIMESTAMP '2026-08-12 15:30:00');

INSERT INTO tb_transaction (id, wallet_id, currency_id, quantity, priceAtTime, type, executedAt)
VALUES ('60000000-0000-0000-0000-000000000013', '30000000-0000-0000-0000-000000000005',
        '20000000-0000-0000-0000-000000000004', 0.02, 110000.00, 'BUY', TIMESTAMP '2026-08-12 15:35:00');

INSERT INTO tb_transaction (id, wallet_id, currency_id, quantity, priceAtTime, type, executedAt)
VALUES ('60000000-0000-0000-0000-000000000014', '30000000-0000-0000-0000-000000000005',
        '20000000-0000-0000-0000-000000000003', 1000.00, 1.16, 'DEPOSIT', TIMESTAMP '2026-08-15 09:00:00');

INSERT INTO tb_transaction (id, wallet_id, currency_id, quantity, priceAtTime, type, executedAt)
VALUES ('60000000-0000-0000-0000-000000000015', '30000000-0000-0000-0000-000000000005',
        '20000000-0000-0000-0000-000000000004', 0.005, 112000.00, 'WITHDRAW', TIMESTAMP '2026-08-28 10:00:00');

INSERT INTO tb_transaction (id, wallet_id, currency_id, quantity, priceAtTime, type, executedAt)
VALUES ('60000000-0000-0000-0000-000000000016', '30000000-0000-0000-0000-000000000006',
        '20000000-0000-0000-0000-000000000001', 800.00, 1, 'DEPOSIT', TIMESTAMP '2026-08-18 12:00:00');

INSERT INTO tb_transaction (id, wallet_id, currency_id, quantity, priceAtTime, type, executedAt)
VALUES ('60000000-0000-0000-0000-000000000017', '30000000-0000-0000-0000-000000000006',
        '20000000-0000-0000-0000-000000000005', 0.01, 24000.00, 'BUY', TIMESTAMP '2026-08-18 12:10:00');

INSERT INTO tb_asset (id, wallet_id, currency_id, quantity)
VALUES ('40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001',
        '20000000-0000-0000-0000-000000000001', 4080.00);

INSERT INTO tb_asset (id, wallet_id, currency_id, quantity)
VALUES ('40000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000001',
        '20000000-0000-0000-0000-000000000004', 0.006);

INSERT INTO tb_asset (id, wallet_id, currency_id, quantity)
VALUES ('40000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000001',
        '20000000-0000-0000-0000-000000000005', 0.10);

INSERT INTO tb_asset (id, wallet_id, currency_id, quantity)
VALUES ('40000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000002',
        '20000000-0000-0000-0000-000000000002', 1120.00);

INSERT INTO tb_asset (id, wallet_id, currency_id, quantity)
VALUES ('40000000-0000-0000-0000-000000000005', '30000000-0000-0000-0000-000000000002',
        '20000000-0000-0000-0000-000000000005', 0.20);

INSERT INTO tb_asset (id, wallet_id, currency_id, quantity)
VALUES ('40000000-0000-0000-0000-000000000006', '30000000-0000-0000-0000-000000000003',
        '20000000-0000-0000-0000-000000000001', 1000.00);

INSERT INTO tb_asset (id, wallet_id, currency_id, quantity)
VALUES ('40000000-0000-0000-0000-000000000007', '30000000-0000-0000-0000-000000000003',
        '20000000-0000-0000-0000-000000000004', 0.005);

INSERT INTO tb_asset (id, wallet_id, currency_id, quantity)
VALUES ('40000000-0000-0000-0000-000000000008', '30000000-0000-0000-0000-000000000004',
        '20000000-0000-0000-0000-000000000001', 1800.00);

INSERT INTO tb_asset (id, wallet_id, currency_id, quantity)
VALUES ('40000000-0000-0000-0000-000000000009', '30000000-0000-0000-0000-000000000004',
        '20000000-0000-0000-0000-000000000005', 0.05);

INSERT INTO tb_asset (id, wallet_id, currency_id, quantity)
VALUES ('40000000-0000-0000-0000-000000000010', '30000000-0000-0000-0000-000000000005',
        '20000000-0000-0000-0000-000000000002', 2800.00);

INSERT INTO tb_asset (id, wallet_id, currency_id, quantity)
VALUES ('40000000-0000-0000-0000-000000000011', '30000000-0000-0000-0000-000000000005',
        '20000000-0000-0000-0000-000000000004', 0.015);

INSERT INTO tb_asset (id, wallet_id, currency_id, quantity)
VALUES ('40000000-0000-0000-0000-000000000012', '30000000-0000-0000-0000-000000000005',
        '20000000-0000-0000-0000-000000000003', 1000.00);

INSERT INTO tb_asset (id, wallet_id, currency_id, quantity)
VALUES ('40000000-0000-0000-0000-000000000013', '30000000-0000-0000-0000-000000000006',
        '20000000-0000-0000-0000-000000000001', 560.00);

INSERT INTO tb_asset (id, wallet_id, currency_id, quantity)
VALUES ('40000000-0000-0000-0000-000000000014', '30000000-0000-0000-0000-000000000006',
        '20000000-0000-0000-0000-000000000005', 0.01);


COMMIT;


UPDATE tb_user
SET email = 'bruno.henrique@example.com'
WHERE id = '10000000-0000-0000-0000-000000000002';

UPDATE tb_user
SET hashedPassword = 'c29f5f6764fe9bea43ea9ddcc625a66539793a010aa9ebcd6a4e9119c596cca2'
WHERE email = 'carla.oliveira@example.com';

UPDATE tb_currency
SET name = 'Dólar dos Estados Unidos'
WHERE symbol = 'USD';

UPDATE tb_currency_quote
SET price = 24800.00
WHERE id = '50000000-0000-0000-0000-000000000007';

INSERT INTO tb_transaction (id, wallet_id, currency_id, quantity, priceAtTime, type, executedAt)
VALUES ('60000000-0000-0000-0000-000000000018', '30000000-0000-0000-0000-000000000004',
        '20000000-0000-0000-0000-000000000001', 500.00, 1, 'DEPOSIT', TIMESTAMP '2026-09-09 09:00:00');

UPDATE tb_asset
SET quantity = quantity + 500.00
WHERE wallet_id = '30000000-0000-0000-0000-000000000004'
  AND currency_id = '20000000-0000-0000-0000-000000000001';

COMMIT;

DELETE FROM tb_currency_quote
WHERE quote_timestamp < TIMESTAMP '2026-09-01 00:00:00';

DELETE FROM tb_currency_quote
WHERE currency_id = (SELECT id FROM tb_currency WHERE symbol = 'SOL')
   OR baseCurrency_id = (SELECT id FROM tb_currency WHERE symbol = 'SOL');

DELETE FROM tb_currency
WHERE symbol = 'SOL';

DELETE FROM tb_transaction
WHERE wallet_id IN (SELECT id FROM tb_wallet WHERE owner_id = '10000000-0000-0000-0000-000000000005');

DELETE FROM tb_asset
WHERE wallet_id IN (SELECT id FROM tb_wallet WHERE owner_id = '10000000-0000-0000-0000-000000000005');

DELETE FROM tb_wallet
WHERE owner_id = '10000000-0000-0000-0000-000000000005';

DELETE FROM tb_user
WHERE id = '10000000-0000-0000-0000-000000000005';

COMMIT;

SELECT 'tb_user' AS tabela, COUNT(*) AS registros FROM tb_user
UNION ALL
SELECT 'tb_currency', COUNT(*) FROM tb_currency
UNION ALL
SELECT 'tb_currency_quote', COUNT(*) FROM tb_currency_quote
UNION ALL
SELECT 'tb_wallet', COUNT(*) FROM tb_wallet
UNION ALL
SELECT 'tb_transaction', COUNT(*) FROM tb_transaction
UNION ALL
SELECT 'tb_asset', COUNT(*) FROM tb_asset;

SELECT * FROM tb_user;
SELECT * FROM tb_currency;
SELECT * FROM tb_currency_quote;
SELECT * FROM tb_wallet;
SELECT * FROM tb_transaction;
SELECT * FROM tb_asset;

SELECT symbol, name
FROM tb_currency
WHERE isFiat = 0
ORDER BY symbol;

SELECT u.name AS usuario, u.email, w.id AS carteira, c.symbol AS moeda_base
FROM tb_user u
LEFT JOIN tb_wallet w ON w.owner_id = u.id
LEFT JOIN tb_currency c ON c.id = w.baseCurrency_id
ORDER BY u.name;

SELECT u.name AS usuario, bc.symbol AS carteira_base, c.symbol AS moeda, a.quantity AS quantidade
FROM tb_asset a
JOIN tb_wallet w ON w.id = a.wallet_id
JOIN tb_user u ON u.id = w.owner_id
JOIN tb_currency c ON c.id = a.currency_id
JOIN tb_currency bc ON bc.id = w.baseCurrency_id
ORDER BY u.name, bc.symbol, c.symbol;

SELECT TO_CHAR(t.executedAt, 'DD/MM/YYYY HH24:MI') AS data_hora,
       t.type AS tipo,
       c.symbol AS moeda,
       t.quantity AS quantidade,
       t.priceAtTime AS preco_unitario,
       t.quantity * t.priceAtTime AS valor_total
FROM tb_transaction t
JOIN tb_currency c ON c.id = t.currency_id
WHERE t.wallet_id = '30000000-0000-0000-0000-000000000001'
ORDER BY t.executedAt;

SELECT t.type AS tipo, COUNT(*) AS total
FROM tb_transaction t
GROUP BY t.type
ORDER BY total DESC, tipo;

SELECT u.name AS usuario, COUNT(w.id) AS qtd_carteiras
FROM tb_user u
JOIN tb_wallet w ON w.owner_id = u.id
GROUP BY u.id, u.name
HAVING COUNT(w.id) > 1;

SELECT c.symbol AS moeda, b.symbol AS cotada_em, q.price AS preco, q.quote_timestamp AS data_cotacao
FROM tb_currency_quote q
JOIN tb_currency c ON c.id = q.currency_id
JOIN tb_currency b ON b.id = q.baseCurrency_id
WHERE q.quote_timestamp = (SELECT MAX(q2.quote_timestamp)
                           FROM tb_currency_quote q2
                           WHERE q2.currency_id = q.currency_id
                             AND q2.baseCurrency_id = q.baseCurrency_id)
ORDER BY b.symbol, c.symbol;

SELECT u.name AS usuario,
       bc.symbol AS moeda_base,
       ROUND(SUM(CASE
                   WHEN a.currency_id = w.baseCurrency_id THEN a.quantity
                   ELSE a.quantity * ult.price
                 END), 2) AS patrimonio_total
FROM tb_wallet w
JOIN tb_user u ON u.id = w.owner_id
JOIN tb_currency bc ON bc.id = w.baseCurrency_id
JOIN tb_asset a ON a.wallet_id = w.id
LEFT JOIN (SELECT q.currency_id, q.baseCurrency_id, q.price
           FROM tb_currency_quote q
           WHERE q.quote_timestamp = (SELECT MAX(q2.quote_timestamp)
                                      FROM tb_currency_quote q2
                                      WHERE q2.currency_id = q.currency_id
                                        AND q2.baseCurrency_id = q.baseCurrency_id)) ult
       ON ult.currency_id = a.currency_id
      AND ult.baseCurrency_id = w.baseCurrency_id
GROUP BY w.id, u.name, bc.symbol
ORDER BY u.name, bc.symbol;
