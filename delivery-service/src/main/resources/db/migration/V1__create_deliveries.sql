CREATE TABLE deliveries (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id              UUID         NOT NULL UNIQUE,
    courier_id            VARCHAR(100) NOT NULL,
    status                VARCHAR(20)  NOT NULL,
    dispatched_at         TIMESTAMPTZ,
    estimated_delivery_at TIMESTAMPTZ,
    delivered_at          TIMESTAMPTZ,
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX ix_deliveries_status ON deliveries (status);
