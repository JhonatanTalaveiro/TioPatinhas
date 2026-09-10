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