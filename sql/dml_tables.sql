-- =============================================================================
--  VOLTZ - MISSÃO TIO PATINHAS | FASE 5
--  Script DML: INSERT, UPDATE, DELETE e SELECT
-- -----------------------------------------------------------------------------
--  Pré-requisito : executar antes o script DDL (sql/create_tables.sql).
--  Banco alvo    : Oracle (servidor da FIAP).
--  Como executar : no SQL Developer, abra este arquivo e use "Executar Script"
--                  (F5). Para rodar um único comando, deixe o cursor sobre ele
--                  e use Ctrl+Enter.
--
--  Convenção dos IDs: UUID de 36 caracteres, o mesmo formato gerado pelo
--  UUID.randomUUID() do Java. Aqui o 1º bloco indica a tabela e o último bloco
--  é o número do registro, para que as chaves estrangeiras (FKs) fiquem
--  legíveis:
--      10000000-0000-0000-0000-0000000000NN  ->  tb_user
--      20000000-0000-0000-0000-0000000000NN  ->  tb_currency
--      30000000-0000-0000-0000-0000000000NN  ->  tb_wallet
--      40000000-0000-0000-0000-0000000000NN  ->  tb_asset
--      50000000-0000-0000-0000-0000000000NN  ->  tb_currency_quote
--      60000000-0000-0000-0000-0000000000NN  ->  tb_transaction
--
--  Rodar o script duas vezes seguidas dá ORA-00001 (unique constraint
--  violated), porque os IDs já existem. Para recomeçar do zero, apague os
--  dados antes (sempre das tabelas filhas para as tabelas pai):
--      DELETE FROM tb_asset;       DELETE FROM tb_transaction;
--      DELETE FROM tb_wallet;      DELETE FROM tb_currency_quote;
--      DELETE FROM tb_currency;    DELETE FROM tb_user;
--      COMMIT;
-- =============================================================================


-- =============================================================================
--  1. INSERT
-- -----------------------------------------------------------------------------
--  A ordem importa por causa das FKs: primeiro as tabelas que não dependem de
--  ninguém (tb_user e tb_currency), depois as que apontam para elas. Inserir
--  uma carteira para um usuário que ainda não existe gera o erro
--  ORA-02291: integrity constraint violated - parent key not found.
--
--  Sempre listamos as colunas no INSERT: assim o comando continua correto
--  mesmo que a ordem das colunas na tabela mude.
-- =============================================================================

-- 1.1 Usuários ----------------------------------------------------------------
-- hashedPassword guarda o hash SHA-256 da senha, nunca a senha pura.
-- Felipe acabou de se cadastrar e ainda não tem carteira (usado no LEFT JOIN).
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

-- 1.2 Moedas ------------------------------------------------------------------
-- isFiat: 1 = moeda fiduciária (emitida por governo) | 0 = criptomoeda.
-- O Oracle (até a versão 21c) não tem BOOLEAN em colunas, por isso o DDL usa
-- NUMBER(1) com CHECK (isFiat IN (0, 1)).
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

-- Legenda das moedas para as FKs abaixo:
--   ...001 = BRL | ...002 = USD | ...003 = EUR | ...004 = BTC | ...005 = ETH | ...006 = SOL

-- 1.3 Cotações ----------------------------------------------------------------
-- Preço de 1 unidade de currency_id, expresso em baseCurrency_id.
-- As três primeiras são de agosto e serão apagadas na limpeza do item 3.1.
-- 1 USD = 5,40 BRL (agosto)
INSERT INTO tb_currency_quote (id, currency_id, baseCurrency_id, price, quote_timestamp)
VALUES ('50000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000002',
        '20000000-0000-0000-0000-000000000001', 5.40, TIMESTAMP '2026-08-01 10:00:00');

-- 1 BTC = 600.000 BRL (agosto)
INSERT INTO tb_currency_quote (id, currency_id, baseCurrency_id, price, quote_timestamp)
VALUES ('50000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000004',
        '20000000-0000-0000-0000-000000000001', 600000.00, TIMESTAMP '2026-08-01 10:00:00');

-- 1 ETH = 24.000 BRL (agosto)
INSERT INTO tb_currency_quote (id, currency_id, baseCurrency_id, price, quote_timestamp)
VALUES ('50000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000005',
        '20000000-0000-0000-0000-000000000001', 24000.00, TIMESTAMP '2026-08-01 10:00:00');

-- 1 USD = 5,45 BRL
INSERT INTO tb_currency_quote (id, currency_id, baseCurrency_id, price, quote_timestamp)
VALUES ('50000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000002',
        '20000000-0000-0000-0000-000000000001', 5.45, TIMESTAMP '2026-09-08 10:00:00');

-- 1 EUR = 6,35 BRL
INSERT INTO tb_currency_quote (id, currency_id, baseCurrency_id, price, quote_timestamp)
VALUES ('50000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000003',
        '20000000-0000-0000-0000-000000000001', 6.35, TIMESTAMP '2026-09-08 10:00:00');

-- 1 BTC = 610.000 BRL
INSERT INTO tb_currency_quote (id, currency_id, baseCurrency_id, price, quote_timestamp)
VALUES ('50000000-0000-0000-0000-000000000006', '20000000-0000-0000-0000-000000000004',
        '20000000-0000-0000-0000-000000000001', 610000.00, TIMESTAMP '2026-09-08 10:00:00');

-- 1 ETH = 24.500 BRL (valor lançado errado, corrigido no item 2.4)
INSERT INTO tb_currency_quote (id, currency_id, baseCurrency_id, price, quote_timestamp)
VALUES ('50000000-0000-0000-0000-000000000007', '20000000-0000-0000-0000-000000000005',
        '20000000-0000-0000-0000-000000000001', 24500.00, TIMESTAMP '2026-09-08 10:00:00');

-- 1 SOL = 1.100 BRL
INSERT INTO tb_currency_quote (id, currency_id, baseCurrency_id, price, quote_timestamp)
VALUES ('50000000-0000-0000-0000-000000000008', '20000000-0000-0000-0000-000000000006',
        '20000000-0000-0000-0000-000000000001', 1100.00, TIMESTAMP '2026-09-08 10:00:00');

-- 1 BTC = 112.000 USD
INSERT INTO tb_currency_quote (id, currency_id, baseCurrency_id, price, quote_timestamp)
VALUES ('50000000-0000-0000-0000-000000000009', '20000000-0000-0000-0000-000000000004',
        '20000000-0000-0000-0000-000000000002', 112000.00, TIMESTAMP '2026-09-08 10:00:00');

-- 1 ETH = 4.500 USD
INSERT INTO tb_currency_quote (id, currency_id, baseCurrency_id, price, quote_timestamp)
VALUES ('50000000-0000-0000-0000-000000000010', '20000000-0000-0000-0000-000000000005',
        '20000000-0000-0000-0000-000000000002', 4500.00, TIMESTAMP '2026-09-08 10:00:00');

-- 1 EUR = 1,165 USD
INSERT INTO tb_currency_quote (id, currency_id, baseCurrency_id, price, quote_timestamp)
VALUES ('50000000-0000-0000-0000-000000000011', '20000000-0000-0000-0000-000000000003',
        '20000000-0000-0000-0000-000000000002', 1.165, TIMESTAMP '2026-09-08 10:00:00');

-- 1 SOL = 200 USD
INSERT INTO tb_currency_quote (id, currency_id, baseCurrency_id, price, quote_timestamp)
VALUES ('50000000-0000-0000-0000-000000000012', '20000000-0000-0000-0000-000000000006',
        '20000000-0000-0000-0000-000000000002', 200.00, TIMESTAMP '2026-09-08 10:00:00');

-- 1 BTC = 615.000 BRL (a mais recente do par BTC/BRL)
INSERT INTO tb_currency_quote (id, currency_id, baseCurrency_id, price, quote_timestamp)
VALUES ('50000000-0000-0000-0000-000000000013', '20000000-0000-0000-0000-000000000004',
        '20000000-0000-0000-0000-000000000001', 615000.00, TIMESTAMP '2026-09-09 10:00:00');

-- 1.4 Carteiras ---------------------------------------------------------------
-- owner_id -> tb_user | baseCurrency_id -> moeda de referência da carteira.
-- Carteira 1: Ana, base BRL
INSERT INTO tb_wallet (id, owner_id, baseCurrency_id)
VALUES ('30000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001',
        '20000000-0000-0000-0000-000000000001');

-- Carteira 2: Ana, base USD
INSERT INTO tb_wallet (id, owner_id, baseCurrency_id)
VALUES ('30000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001',
        '20000000-0000-0000-0000-000000000002');

-- Carteira 3: Bruno, base BRL
INSERT INTO tb_wallet (id, owner_id, baseCurrency_id)
VALUES ('30000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000002',
        '20000000-0000-0000-0000-000000000001');

-- Carteira 4: Carla, base BRL
INSERT INTO tb_wallet (id, owner_id, baseCurrency_id)
VALUES ('30000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000003',
        '20000000-0000-0000-0000-000000000001');

-- Carteira 5: Diego, base USD
INSERT INTO tb_wallet (id, owner_id, baseCurrency_id)
VALUES ('30000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000004',
        '20000000-0000-0000-0000-000000000002');

-- Carteira 6: Eduarda, base BRL
INSERT INTO tb_wallet (id, owner_id, baseCurrency_id)
VALUES ('30000000-0000-0000-0000-000000000006', '10000000-0000-0000-0000-000000000005',
        '20000000-0000-0000-0000-000000000001');

-- 1.5 Transações --------------------------------------------------------------
-- type: DEPOSIT (entrada), WITHDRAW (saída), BUY (compra) ou SELL (venda).
-- priceAtTime = preço de 1 unidade da moeda, na moeda base da carteira, no
-- momento da operação. Em depósitos/saques da própria moeda base, vale 1.
-- Valor total de uma transação = quantity * priceAtTime.

-- Ana deposita R$ 10.000,00
INSERT INTO tb_transaction (id, wallet_id, currency_id, quantity, priceAtTime, type, executedAt)
VALUES ('60000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001',
        '20000000-0000-0000-0000-000000000001', 10000.00, 1, 'DEPOSIT', TIMESTAMP '2026-08-01 09:00:00');

-- Ana compra 0,01 BTC a R$ 600.000 (custo: R$ 6.000)
INSERT INTO tb_transaction (id, wallet_id, currency_id, quantity, priceAtTime, type, executedAt)
VALUES ('60000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000001',
        '20000000-0000-0000-0000-000000000004', 0.01, 600000.00, 'BUY', TIMESTAMP '2026-08-02 10:30:00');

-- Ana compra 0,10 ETH a R$ 24.000 (custo: R$ 2.400)
INSERT INTO tb_transaction (id, wallet_id, currency_id, quantity, priceAtTime, type, executedAt)
VALUES ('60000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000001',
        '20000000-0000-0000-0000-000000000005', 0.10, 24000.00, 'BUY', TIMESTAMP '2026-08-05 14:00:00');

-- Ana vende 0,004 BTC a R$ 620.000 (recebe: R$ 2.480)
INSERT INTO tb_transaction (id, wallet_id, currency_id, quantity, priceAtTime, type, executedAt)
VALUES ('60000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000001',
        '20000000-0000-0000-0000-000000000004', 0.004, 620000.00, 'SELL', TIMESTAMP '2026-08-20 16:45:00');

-- Ana deposita US$ 2.000,00 na carteira em dólar
INSERT INTO tb_transaction (id, wallet_id, currency_id, quantity, priceAtTime, type, executedAt)
VALUES ('60000000-0000-0000-0000-000000000005', '30000000-0000-0000-0000-000000000002',
        '20000000-0000-0000-0000-000000000002', 2000.00, 1, 'DEPOSIT', TIMESTAMP '2026-08-03 11:00:00');

-- Ana compra 0,20 ETH a US$ 4.400 (custo: US$ 880)
INSERT INTO tb_transaction (id, wallet_id, currency_id, quantity, priceAtTime, type, executedAt)
VALUES ('60000000-0000-0000-0000-000000000006', '30000000-0000-0000-0000-000000000002',
        '20000000-0000-0000-0000-000000000005', 0.20, 4400.00, 'BUY', TIMESTAMP '2026-08-03 11:05:00');

-- Bruno deposita R$ 5.000,00
INSERT INTO tb_transaction (id, wallet_id, currency_id, quantity, priceAtTime, type, executedAt)
VALUES ('60000000-0000-0000-0000-000000000007', '30000000-0000-0000-0000-000000000003',
        '20000000-0000-0000-0000-000000000001', 5000.00, 1, 'DEPOSIT', TIMESTAMP '2026-08-04 08:15:00');

-- Bruno compra 0,005 BTC a R$ 600.000 (custo: R$ 3.000)
INSERT INTO tb_transaction (id, wallet_id, currency_id, quantity, priceAtTime, type, executedAt)
VALUES ('60000000-0000-0000-0000-000000000008', '30000000-0000-0000-0000-000000000003',
        '20000000-0000-0000-0000-000000000004', 0.005, 600000.00, 'BUY', TIMESTAMP '2026-08-04 08:20:00');

-- Bruno saca R$ 1.000,00
INSERT INTO tb_transaction (id, wallet_id, currency_id, quantity, priceAtTime, type, executedAt)
VALUES ('60000000-0000-0000-0000-000000000009', '30000000-0000-0000-0000-000000000003',
        '20000000-0000-0000-0000-000000000001', 1000.00, 1, 'WITHDRAW', TIMESTAMP '2026-08-25 19:00:00');

-- Carla deposita R$ 3.000,00
INSERT INTO tb_transaction (id, wallet_id, currency_id, quantity, priceAtTime, type, executedAt)
VALUES ('60000000-0000-0000-0000-000000000010', '30000000-0000-0000-0000-000000000004',
        '20000000-0000-0000-0000-000000000001', 3000.00, 1, 'DEPOSIT', TIMESTAMP '2026-08-10 13:00:00');

-- Carla compra 0,05 ETH a R$ 24.000 (custo: R$ 1.200)
INSERT INTO tb_transaction (id, wallet_id, currency_id, quantity, priceAtTime, type, executedAt)
VALUES ('60000000-0000-0000-0000-000000000011', '30000000-0000-0000-0000-000000000004',
        '20000000-0000-0000-0000-000000000005', 0.05, 24000.00, 'BUY', TIMESTAMP '2026-08-10 13:10:00');

-- Diego deposita US$ 5.000,00
INSERT INTO tb_transaction (id, wallet_id, currency_id, quantity, priceAtTime, type, executedAt)
VALUES ('60000000-0000-0000-0000-000000000012', '30000000-0000-0000-0000-000000000005',
        '20000000-0000-0000-0000-000000000002', 5000.00, 1, 'DEPOSIT', TIMESTAMP '2026-08-12 15:30:00');

-- Diego compra 0,02 BTC a US$ 110.000 (custo: US$ 2.200)
INSERT INTO tb_transaction (id, wallet_id, currency_id, quantity, priceAtTime, type, executedAt)
VALUES ('60000000-0000-0000-0000-000000000013', '30000000-0000-0000-0000-000000000005',
        '20000000-0000-0000-0000-000000000004', 0.02, 110000.00, 'BUY', TIMESTAMP '2026-08-12 15:35:00');

-- Diego deposita EUR 1.000,00 (naquele momento, 1 EUR = US$ 1,16)
INSERT INTO tb_transaction (id, wallet_id, currency_id, quantity, priceAtTime, type, executedAt)
VALUES ('60000000-0000-0000-0000-000000000014', '30000000-0000-0000-0000-000000000005',
        '20000000-0000-0000-0000-000000000003', 1000.00, 1.16, 'DEPOSIT', TIMESTAMP '2026-08-15 09:00:00');

-- Diego transfere 0,005 BTC para uma carteira externa
INSERT INTO tb_transaction (id, wallet_id, currency_id, quantity, priceAtTime, type, executedAt)
VALUES ('60000000-0000-0000-0000-000000000015', '30000000-0000-0000-0000-000000000005',
        '20000000-0000-0000-0000-000000000004', 0.005, 112000.00, 'WITHDRAW', TIMESTAMP '2026-08-28 10:00:00');

-- Eduarda deposita R$ 800,00
INSERT INTO tb_transaction (id, wallet_id, currency_id, quantity, priceAtTime, type, executedAt)
VALUES ('60000000-0000-0000-0000-000000000016', '30000000-0000-0000-0000-000000000006',
        '20000000-0000-0000-0000-000000000001', 800.00, 1, 'DEPOSIT', TIMESTAMP '2026-08-18 12:00:00');

-- Eduarda compra 0,01 ETH a R$ 24.000 (custo: R$ 240)
INSERT INTO tb_transaction (id, wallet_id, currency_id, quantity, priceAtTime, type, executedAt)
VALUES ('60000000-0000-0000-0000-000000000017', '30000000-0000-0000-0000-000000000006',
        '20000000-0000-0000-0000-000000000005', 0.01, 24000.00, 'BUY', TIMESTAMP '2026-08-18 12:10:00');

-- 1.6 Ativos (saldos) ---------------------------------------------------------
-- tb_asset guarda o SALDO ATUAL de cada moeda em cada carteira. Os valores
-- são exatamente o resultado das transações acima: compras tiram dinheiro da
-- moeda base e vendas devolvem.

-- Carteira 1 | BRL: 10.000 - 6.000 - 2.400 + 2.480
INSERT INTO tb_asset (id, wallet_id, currency_id, quantity)
VALUES ('40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001',
        '20000000-0000-0000-0000-000000000001', 4080.00);

-- Carteira 1 | BTC: 0,01 - 0,004
INSERT INTO tb_asset (id, wallet_id, currency_id, quantity)
VALUES ('40000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000001',
        '20000000-0000-0000-0000-000000000004', 0.006);

-- Carteira 1 | ETH
INSERT INTO tb_asset (id, wallet_id, currency_id, quantity)
VALUES ('40000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000001',
        '20000000-0000-0000-0000-000000000005', 0.10);

-- Carteira 2 | USD: 2.000 - 880
INSERT INTO tb_asset (id, wallet_id, currency_id, quantity)
VALUES ('40000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000002',
        '20000000-0000-0000-0000-000000000002', 1120.00);

-- Carteira 2 | ETH
INSERT INTO tb_asset (id, wallet_id, currency_id, quantity)
VALUES ('40000000-0000-0000-0000-000000000005', '30000000-0000-0000-0000-000000000002',
        '20000000-0000-0000-0000-000000000005', 0.20);

-- Carteira 3 | BRL: 5.000 - 3.000 - 1.000
INSERT INTO tb_asset (id, wallet_id, currency_id, quantity)
VALUES ('40000000-0000-0000-0000-000000000006', '30000000-0000-0000-0000-000000000003',
        '20000000-0000-0000-0000-000000000001', 1000.00);

-- Carteira 3 | BTC
INSERT INTO tb_asset (id, wallet_id, currency_id, quantity)
VALUES ('40000000-0000-0000-0000-000000000007', '30000000-0000-0000-0000-000000000003',
        '20000000-0000-0000-0000-000000000004', 0.005);

-- Carteira 4 | BRL: 3.000 - 1.200
INSERT INTO tb_asset (id, wallet_id, currency_id, quantity)
VALUES ('40000000-0000-0000-0000-000000000008', '30000000-0000-0000-0000-000000000004',
        '20000000-0000-0000-0000-000000000001', 1800.00);

-- Carteira 4 | ETH
INSERT INTO tb_asset (id, wallet_id, currency_id, quantity)
VALUES ('40000000-0000-0000-0000-000000000009', '30000000-0000-0000-0000-000000000004',
        '20000000-0000-0000-0000-000000000005', 0.05);

-- Carteira 5 | USD: 5.000 - 2.200
INSERT INTO tb_asset (id, wallet_id, currency_id, quantity)
VALUES ('40000000-0000-0000-0000-000000000010', '30000000-0000-0000-0000-000000000005',
        '20000000-0000-0000-0000-000000000002', 2800.00);

-- Carteira 5 | BTC: 0,02 - 0,005
INSERT INTO tb_asset (id, wallet_id, currency_id, quantity)
VALUES ('40000000-0000-0000-0000-000000000011', '30000000-0000-0000-0000-000000000005',
        '20000000-0000-0000-0000-000000000004', 0.015);

-- Carteira 5 | EUR
INSERT INTO tb_asset (id, wallet_id, currency_id, quantity)
VALUES ('40000000-0000-0000-0000-000000000012', '30000000-0000-0000-0000-000000000005',
        '20000000-0000-0000-0000-000000000003', 1000.00);

-- Carteira 6 | BRL: 800 - 240
INSERT INTO tb_asset (id, wallet_id, currency_id, quantity)
VALUES ('40000000-0000-0000-0000-000000000013', '30000000-0000-0000-0000-000000000006',
        '20000000-0000-0000-0000-000000000001', 560.00);

-- Carteira 6 | ETH
INSERT INTO tb_asset (id, wallet_id, currency_id, quantity)
VALUES ('40000000-0000-0000-0000-000000000014', '30000000-0000-0000-0000-000000000006',
        '20000000-0000-0000-0000-000000000005', 0.01);

-- COMMIT grava as alterações de forma definitiva. Até aqui elas só existem na
-- sua sessão; se algo der errado antes do COMMIT, ROLLBACK desfaz tudo.
COMMIT;


-- =============================================================================
--  2. UPDATE
-- -----------------------------------------------------------------------------
--  Regra de ouro: todo UPDATE precisa de WHERE. Sem ele, o comando altera
--  TODAS as linhas da tabela. Na dúvida, rode antes um SELECT com o mesmo
--  WHERE para ver quais linhas serão afetadas.
-- =============================================================================

-- 2.1 Bruno trocou de e-mail.
UPDATE tb_user
SET email = 'bruno.henrique@example.com'
WHERE id = '10000000-0000-0000-0000-000000000002';

-- 2.2 Carla trocou de senha. O filtro pode usar qualquer coluna única, como o e-mail.
UPDATE tb_user
SET hashedPassword = 'c29f5f6764fe9bea43ea9ddcc625a66539793a010aa9ebcd6a4e9119c596cca2'
WHERE email = 'carla.oliveira@example.com';

-- 2.3 Padronizar o nome do dólar com o que a aplicação Java usa (Main.java).
UPDATE tb_currency
SET name = 'Dólar dos Estados Unidos'
WHERE symbol = 'USD';

-- 2.4 A cotação ETH/BRL de 08/09 foi lançada errada: corrigir o preço.
UPDATE tb_currency_quote
SET price = 24800.00
WHERE id = '50000000-0000-0000-0000-000000000007';

-- 2.5 Carla fez um novo depósito de R$ 500,00.
--     Numa operação real, um depósito são DOIS comandos: registrar a transação
--     (INSERT) e atualizar o saldo (UPDATE). O UPDATE parte do valor atual da
--     própria coluna: quantity = quantity + 500.
INSERT INTO tb_transaction (id, wallet_id, currency_id, quantity, priceAtTime, type, executedAt)
VALUES ('60000000-0000-0000-0000-000000000018', '30000000-0000-0000-0000-000000000004',
        '20000000-0000-0000-0000-000000000001', 500.00, 1, 'DEPOSIT', TIMESTAMP '2026-09-09 09:00:00');

UPDATE tb_asset
SET quantity = quantity + 500.00
WHERE wallet_id = '30000000-0000-0000-0000-000000000004'
  AND currency_id = '20000000-0000-0000-0000-000000000001';

COMMIT;


-- =============================================================================
--  3. DELETE
-- -----------------------------------------------------------------------------
--  Também exige WHERE. E a ordem importa de novo por causa das FKs: primeiro
--  apagamos os registros filhos, depois o pai. Tentar apagar um usuário que
--  ainda tem carteira gera o erro
--  ORA-02292: integrity constraint violated - child record found.
-- =============================================================================

-- 3.1 Limpeza de histórico: remover as cotações anteriores a setembro de 2026.
DELETE FROM tb_currency_quote
WHERE quote_timestamp < TIMESTAMP '2026-09-01 00:00:00';

-- 3.2 A plataforma deixou de negociar Solana (SOL).
--     Primeiro as cotações que apontam para a moeda, depois a própria moeda.
--     (Nenhuma carteira tem SOL; se tivesse, o DELETE da moeda falharia.)
DELETE FROM tb_currency_quote
WHERE currency_id = (SELECT id FROM tb_currency WHERE symbol = 'SOL')
   OR baseCurrency_id = (SELECT id FROM tb_currency WHERE symbol = 'SOL');

DELETE FROM tb_currency
WHERE symbol = 'SOL';

-- 3.3 Eduarda encerrou a conta. Apagamos na ordem inversa das FKs:
--     transações -> ativos -> carteiras -> usuário.
--     Observação: sistemas financeiros reais costumam manter esses dados por
--     exigência legal e fazer só uma "exclusão lógica" (ex.: uma coluna
--     ativo = 0). Aqui fazemos a exclusão física para demonstrar o DELETE.
DELETE FROM tb_transaction
WHERE wallet_id IN (SELECT id FROM tb_wallet WHERE owner_id = '10000000-0000-0000-0000-000000000005');

DELETE FROM tb_asset
WHERE wallet_id IN (SELECT id FROM tb_wallet WHERE owner_id = '10000000-0000-0000-0000-000000000005');

DELETE FROM tb_wallet
WHERE owner_id = '10000000-0000-0000-0000-000000000005';

DELETE FROM tb_user
WHERE id = '10000000-0000-0000-0000-000000000005';

COMMIT;


-- =============================================================================
--  4. SELECT
-- =============================================================================

-- 4.1 Conferência geral: quantos registros ficaram em cada tabela.
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

-- 4.2 Todos os dados de cada tabela.
SELECT * FROM tb_user;
SELECT * FROM tb_currency;
SELECT * FROM tb_currency_quote;
SELECT * FROM tb_wallet;
SELECT * FROM tb_transaction;
SELECT * FROM tb_asset;

-- 4.3 Filtro com WHERE: apenas as criptomoedas.
SELECT symbol, name
FROM tb_currency
WHERE isFiat = 0
ORDER BY symbol;

-- 4.4 LEFT JOIN: todos os usuários e suas carteiras. O LEFT JOIN mantém quem
--     não tem carteira (Felipe aparece com as colunas da carteira vazias).
--     Com um JOIN comum (INNER JOIN), o Felipe sumiria do resultado.
SELECT u.name AS usuario, u.email, w.id AS carteira, c.symbol AS moeda_base
FROM tb_user u
LEFT JOIN tb_wallet w ON w.owner_id = u.id
LEFT JOIN tb_currency c ON c.id = w.baseCurrency_id
ORDER BY u.name;

-- 4.5 JOIN entre várias tabelas: saldo de cada usuário, por carteira e moeda.
--     tb_currency aparece duas vezes, com apelidos diferentes: c é a moeda do
--     ativo e bc é a moeda base da carteira.
SELECT u.name AS usuario, bc.symbol AS carteira_base, c.symbol AS moeda, a.quantity AS quantidade
FROM tb_asset a
JOIN tb_wallet w ON w.id = a.wallet_id
JOIN tb_user u ON u.id = w.owner_id
JOIN tb_currency c ON c.id = a.currency_id
JOIN tb_currency bc ON bc.id = w.baseCurrency_id
ORDER BY u.name, bc.symbol, c.symbol;

-- 4.6 Extrato da carteira 1 (Ana, base BRL) em ordem cronológica.
--     TO_CHAR formata a data; quantity * priceAtTime é o valor da operação.
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

-- 4.7 GROUP BY: quantidade de transações de cada tipo.
SELECT t.type AS tipo, COUNT(*) AS total
FROM tb_transaction t
GROUP BY t.type
ORDER BY total DESC, tipo;

-- 4.8 HAVING: usuários com mais de uma carteira.
--     WHERE filtra as linhas ANTES de agrupar; HAVING filtra os grupos DEPOIS.
SELECT u.name AS usuario, COUNT(w.id) AS qtd_carteiras
FROM tb_user u
JOIN tb_wallet w ON w.owner_id = u.id
GROUP BY u.id, u.name
HAVING COUNT(w.id) > 1;

-- 4.9 Subconsulta: cotação mais recente de cada par de moedas.
--     Para cada linha, a subconsulta busca a data mais recente do mesmo par.
SELECT c.symbol AS moeda, b.symbol AS cotada_em, q.price AS preco, q.quote_timestamp AS data_cotacao
FROM tb_currency_quote q
JOIN tb_currency c ON c.id = q.currency_id
JOIN tb_currency b ON b.id = q.baseCurrency_id
WHERE q.quote_timestamp = (SELECT MAX(q2.quote_timestamp)
                           FROM tb_currency_quote q2
                           WHERE q2.currency_id = q.currency_id
                             AND q2.baseCurrency_id = q.baseCurrency_id)
ORDER BY b.symbol, c.symbol;

-- 4.10 Patrimônio total de cada carteira, na sua moeda base.
--      É o equivalente em SQL do método Wallet.getTotalValue() do Java:
--      - ativo na própria moeda base: vale a própria quantidade;
--      - demais ativos: quantidade * cotação mais recente na moeda base.
--      A subconsulta "ult" é a consulta 4.9 reaproveitada como tabela.
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
