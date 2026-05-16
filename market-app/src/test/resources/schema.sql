CREATE TABLE IF NOT EXISTS items
(
    id             BIGSERIAL PRIMARY KEY,
    title          VARCHAR(255)   NOT NULL,
    description    TEXT,
    img_path       VARCHAR(256),
    price          DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    stock_quantity INTEGER        NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_items_title_lower ON items (LOWER(title));
CREATE INDEX IF NOT EXISTS idx_items_price ON items (price);

CREATE TABLE IF NOT EXISTS orders
(
    id         BIGSERIAL PRIMARY KEY,
    status     VARCHAR(255) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS order_items
(
    id         BIGSERIAL PRIMARY KEY,
    order_id   BIGINT         NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    item_id    BIGINT         NOT NULL REFERENCES items (id),
    quantity   INTEGER        NOT NULL CHECK (quantity > 0),
    price      DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items (order_id);