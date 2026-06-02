CREATE TABLE payments (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id           UUID         NOT NULL UNIQUE,
    amount             NUMERIC(12, 2) NOT NULL,
    currency           VARCHAR(3)   NOT NULL,
    status             VARCHAR(20)  NOT NULL,
    provider           VARCHAR(50)  NOT NULL,
    provider_reference VARCHAR(100),
    reason_code        VARCHAR(50),
    reason             VARCHAR(255),
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX ix_payments_status ON payments (status);

CREATE TABLE payment_recon_log (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id      UUID         NOT NULL,
    order_id        UUID         NOT NULL,
    previous_status VARCHAR(20)  NOT NULL,
    new_status      VARCHAR(20)  NOT NULL,
    detail          VARCHAR(255),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX ix_recon_payment ON payment_recon_log (payment_id);
