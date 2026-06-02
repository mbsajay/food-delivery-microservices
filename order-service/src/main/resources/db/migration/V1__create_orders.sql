CREATE TABLE orders (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id   UUID         NOT NULL,
    restaurant_id UUID         NOT NULL,
    status        VARCHAR(30)  NOT NULL,
    total_amount  NUMERIC(12, 2) NOT NULL,
    currency      VARCHAR(3)   NOT NULL,
    payment_id    UUID,
    courier_id    VARCHAR(100),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX ix_orders_customer ON orders (customer_id);
CREATE INDEX ix_orders_status ON orders (status);

CREATE TABLE order_items (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id     UUID         NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    menu_item_id UUID         NOT NULL,
    name         VARCHAR(150) NOT NULL,
    quantity     INTEGER      NOT NULL,
    unit_price   NUMERIC(10, 2) NOT NULL
);

CREATE INDEX ix_order_items_order ON order_items (order_id);
