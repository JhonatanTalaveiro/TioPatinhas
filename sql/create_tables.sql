-- =============================================================================
--  VOLTZ - MISSÃO TIO PATINHAS
--  Script DDL: criação das tabelas e alterações posteriores
-- -----------------------------------------------------------------------------
--  Banco alvo    : Oracle (servidor da FIAP).
--  Como executar : no SQL Developer, abra este arquivo, confirme que a aba
--                  deste script é a que está selecionada e use "Executar
--                  Script" (F5). Ctrl+Enter roda só o comando sob o cursor.
--  Depois daqui  : rode sql/dml_tables.sql para carregar os dados de exemplo.
--
--  Ordem de criação: as tabelas pai (tb_user e tb_currency) vêm primeiro,
--  porque todas as outras têm chaves estrangeiras apontando para elas. Criar
--  uma tabela filha antes da pai dá ORA-00942 (table or view does not exist).
--
--  Se as tabelas JÁ existem no seu usuário, a seção 1 vai dar ORA-00955 (name
--  is already used). Nesse caso rode só a seção 2, ou descomente a seção 0
--  para recriar tudo do zero.
-- =============================================================================


-- =============================================================================
--  0. LIMPEZA (opcional - deixe comentado no dia a dia)
-- -----------------------------------------------------------------------------
--  ATENÇÃO: DROP TABLE apaga a tabela e todos os dados dela, e isso não pode
--  ser desfeito. A ordem é a inversa das chaves estrangeiras: primeiro as
--  tabelas filhas. CASCADE CONSTRAINTS derruba junto as FKs que apontam para
--  a tabela que está sendo removida.
-- =============================================================================

-- DROP TABLE tb_transaction CASCADE CONSTRAINTS;
-- DROP TABLE tb_asset CASCADE CONSTRAINTS;
-- DROP TABLE tb_currency_quote CASCADE CONSTRAINTS;
-- DROP TABLE tb_wallet CASCADE CONSTRAINTS;
-- DROP TABLE tb_currency CASCADE CONSTRAINTS;
-- DROP TABLE tb_user CASCADE CONSTRAINTS;


-- =============================================================================
--  1. CRIAÇÃO DAS TABELAS
-- =============================================================================

-- 1.1 Usuários ----------------------------------------------------------------
-- id é VARCHAR2(36) porque guarda o UUID gerado pelo Java (UUID.randomUUID()).
-- hashedPassword guarda o hash SHA-256 da senha, nunca a senha pura.
CREATE TABLE tb_user
(
    id             VARCHAR2(36) NOT NULL,
    email          VARCHAR2(255) NOT NULL,
    hashedPassword VARCHAR2(255) NOT NULL,
    name           VARCHAR2(255) NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY (id),
    CONSTRAINT uq_user_email UNIQUE (email)
);

-- 1.2 Moedas ------------------------------------------------------------------
-- isFiat: 1 = moeda fiduciária (emitida por governo) | 0 = criptomoeda.
-- O Oracle não tem BOOLEAN em coluna, por isso NUMBER(1) com CHECK.
CREATE TABLE tb_currency
(
    id     VARCHAR2(36) NOT NULL,
    symbol VARCHAR2(10) NOT NULL,
    name   VARCHAR2(255) NOT NULL,
    isFiat NUMBER(1) NOT NULL,
    CONSTRAINT pk_currency PRIMARY KEY (id),
    CONSTRAINT uq_currency_symbol UNIQUE (symbol),
    CONSTRAINT ck_currency_isFiat CHECK (isFiat IN (0, 1))
);

-- 1.3 Carteiras ---------------------------------------------------------------
-- Cada carteira pertence a um usuário e tem uma moeda base, que é a moeda em
-- que o patrimônio dela é medido.
CREATE TABLE tb_wallet
(
    id              VARCHAR2(36) NOT NULL,
    owner_id        VARCHAR2(36) NOT NULL,
    baseCurrency_id VARCHAR2(36) NOT NULL,
    CONSTRAINT pk_wallet PRIMARY KEY (id),
    CONSTRAINT fk_wallet_owner FOREIGN KEY (owner_id) REFERENCES tb_user (id),
    CONSTRAINT fk_wallet_base_currency FOREIGN KEY (baseCurrency_id) REFERENCES
        tb_currency (id)
);

-- 1.4 Ativos ------------------------------------------------------------------
-- Saldo atual de uma moeda dentro de uma carteira.
-- NUMBER(18,8): até 10 dígitos antes da vírgula e 8 casas decimais, o bastante
-- para frações pequenas de criptomoeda (0.00000001 BTC).
CREATE TABLE tb_asset
(
    id          VARCHAR2(36) NOT NULL,
    wallet_id   VARCHAR2(36) NOT NULL,
    currency_id VARCHAR2(36) NOT NULL,
    quantity    NUMBER(18, 8) NOT NULL,
    CONSTRAINT pk_asset PRIMARY KEY (id),
    CONSTRAINT fk_asset_wallet FOREIGN KEY (wallet_id) REFERENCES tb_wallet (id
        ),
    CONSTRAINT fk_asset_currency FOREIGN KEY (currency_id) REFERENCES
        tb_currency (id)
);

-- 1.5 Cotações ----------------------------------------------------------------
-- Preço de 1 unidade de currency_id expresso em baseCurrency_id, em um
-- instante. As duas FKs apontam para a MESMA tabela tb_currency.
CREATE TABLE tb_currency_quote
(
    id              VARCHAR2(36) NOT NULL,
    currency_id     VARCHAR2(36) NOT NULL,
    baseCurrency_id VARCHAR2(36) NOT NULL,
    price           NUMBER(18, 8) NOT NULL,
    quote_timestamp TIMESTAMP NOT NULL,
    CONSTRAINT pk_currency_quote PRIMARY KEY (id),
    CONSTRAINT fk_quote_currency FOREIGN KEY (currency_id) REFERENCES
        tb_currency (id),
    CONSTRAINT fk_quote_base_currency FOREIGN KEY (baseCurrency_id) REFERENCES
        tb_currency (id)
);

-- 1.6 Transações --------------------------------------------------------------
-- Movimentações da carteira. A lista do CHECK é exatamente o enum
-- TransactionType do Java.
CREATE TABLE tb_transaction
(
    id          VARCHAR2(36) NOT NULL,
    wallet_id   VARCHAR2(36) NOT NULL,
    currency_id VARCHAR2(36) NOT NULL,
    quantity    NUMBER(18, 8) NOT NULL,
    priceAtTime NUMBER(18, 8) NOT NULL,
    TYPE        VARCHAR2(20) NOT NULL,
    executedAt  TIMESTAMP NOT NULL,
    CONSTRAINT pk_transaction PRIMARY KEY (id),
    CONSTRAINT fk_transaction_wallet FOREIGN KEY (wallet_id) REFERENCES
        tb_wallet (id),
    CONSTRAINT fk_transaction_currency FOREIGN KEY (currency_id) REFERENCES
        tb_currency (id),
    CONSTRAINT ck_transaction_type CHECK (TYPE IN ('BUY', 'SELL', 'DEPOSIT',
                                                   'WITHDRAW'))
);


-- =============================================================================
--  2. ALTERAÇÕES (ALTER TABLE e índices)
-- -----------------------------------------------------------------------------
--  Estas são as regras que o programa Java também valida na digitação (classes
--  Validador e Menu*). Deixá-las no banco é a garantia de verdade: o Oracle
--  recusa o dado mesmo que ele chegue por outro caminho, como o SQL Developer.
--
--  Se a tabela já tiver dados que quebram a regra, o ALTER falha com ORA-02293
--  (cannot validate - constraint violated). Nesse caso, corrija os dados e
--  rode o comando de novo.
-- =============================================================================

-- 2.1 Um usuário não precisa de duas carteiras na mesma moeda base.
ALTER TABLE tb_wallet
    ADD CONSTRAINT uq_wallet_owner_currency UNIQUE (owner_id, baseCurrency_id);

-- 2.2 O saldo de uma moeda na carteira é uma linha só. Sem isso, o mesmo saldo
--     poderia aparecer dividido em dois registros.
ALTER TABLE tb_asset
    ADD CONSTRAINT uq_asset_wallet_currency UNIQUE (wallet_id, currency_id);

-- 2.3 Saldo nunca é negativo; quantidade e preço de uma operação são positivos.
ALTER TABLE tb_asset
    ADD CONSTRAINT ck_asset_quantity CHECK (quantity >= 0);

ALTER TABLE tb_transaction
    ADD CONSTRAINT ck_transaction_quantity CHECK (quantity > 0);

ALTER TABLE tb_transaction
    ADD CONSTRAINT ck_transaction_price CHECK (priceAtTime > 0);

-- 2.4 Cotação: preço positivo e par de moedas diferentes (1 BRL = 1 BRL não
--     informa nada).
ALTER TABLE tb_currency_quote
    ADD CONSTRAINT ck_quote_price CHECK (price > 0);

ALTER TABLE tb_currency_quote
    ADD CONSTRAINT ck_quote_moedas_diferentes CHECK (currency_id <> baseCurrency_id);

-- 2.5 Índices para as consultas mais usadas pela aplicação.
--     O Oracle já cria um índice automático para cada PRIMARY KEY e para cada
--     UNIQUE, então as FKs owner_id (tb_wallet) e wallet_id (tb_asset) já ficam
--     cobertas pelas constraints 2.1 e 2.2. Faltam estas duas:
CREATE INDEX ix_transaction_wallet ON tb_transaction (wallet_id);

CREATE INDEX ix_quote_par ON tb_currency_quote (currency_id, baseCurrency_id,
                                                quote_timestamp);


-- =============================================================================
--  3. CONFERÊNCIA
-- -----------------------------------------------------------------------------
--  Rode depois do script para ver se tudo foi criado no seu usuário.
-- =============================================================================

-- SELECT table_name FROM user_tables ORDER BY table_name;

-- SELECT table_name, constraint_name, constraint_type
-- FROM user_constraints
-- WHERE table_name LIKE 'TB\_%' ESCAPE '\'
-- ORDER BY table_name, constraint_type, constraint_name;
