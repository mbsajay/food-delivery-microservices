CREATE TABLE restaurants (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id    UUID         NOT NULL,
    name        VARCHAR(150) NOT NULL,
    description VARCHAR(1000),
    cuisine     VARCHAR(80),
    city        VARCHAR(80),
    address     VARCHAR(250),
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX ix_restaurants_city ON restaurants (lower(city));
CREATE INDEX ix_restaurants_owner ON restaurants (owner_id);

CREATE TABLE menu_items (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id UUID         NOT NULL REFERENCES restaurants (id) ON DELETE CASCADE,
    name          VARCHAR(150) NOT NULL,
    description   VARCHAR(1000),
    category      VARCHAR(80),
    price         NUMERIC(10, 2) NOT NULL,
    available     BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE INDEX ix_menu_items_restaurant ON menu_items (restaurant_id);
