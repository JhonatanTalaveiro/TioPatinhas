-- DROP TABLE tb_transaction CASCADE CONSTRAINTS;
-- DROP TABLE tb_asset CASCADE CONSTRAINTS;
-- DROP TABLE tb_currency_quote CASCADE CONSTRAINTS;
-- DROP TABLE tb_wallet CASCADE CONSTRAINTS;
-- DROP TABLE tb_currency CASCADE CONSTRAINTS;
-- DROP TABLE tb_user CASCADE CONSTRAINTS;

CREATE TABLE tb_user
(
    id             VARCHAR2(36) NOT NULL,
    email          VARCHAR2(255) NOT NULL,
    hashedPassword VARCHAR2(255) NOT NULL,
    name           VARCHAR2(255) NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY (id),
    CONSTRAINT uq_user_email UNIQUE (email)
);

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

ALTER TABLE tb_wallet
    ADD CONSTRAINT uq_wallet_owner_currency UNIQUE (owner_id, baseCurrency_id);

ALTER TABLE tb_asset
    ADD CONSTRAINT uq_asset_wallet_currency UNIQUE (wallet_id, currency_id);

ALTER TABLE tb_asset
    ADD CONSTRAINT ck_asset_quantity CHECK (quantity >= 0);

ALTER TABLE tb_transaction
    ADD CONSTRAINT ck_transaction_quantity CHECK (quantity > 0);

ALTER TABLE tb_transaction
    ADD CONSTRAINT ck_transaction_price CHECK (priceAtTime > 0);

ALTER TABLE tb_currency_quote
    ADD CONSTRAINT ck_quote_price CHECK (price > 0);

ALTER TABLE tb_currency_quote
    ADD CONSTRAINT ck_quote_moedas_diferentes CHECK (currency_id <> baseCurrency_id);

CREATE INDEX ix_transaction_wallet ON tb_transaction (wallet_id);

CREATE INDEX ix_quote_par ON tb_currency_quote (currency_id, baseCurrency_id,
                                                quote_timestamp);

-- SELECT table_name FROM user_tables ORDER BY table_name;

-- SELECT table_name, constraint_name, constraint_type
-- FROM user_constraints
-- WHERE table_name LIKE 'TB\_%' ESCAPE '\'
-- ORDER BY table_name, constraint_type, constraint_name;
