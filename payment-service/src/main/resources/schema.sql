CREATE TABLE IF NOT EXISTS balance (
    user_id    BIGINT PRIMARY KEY,
    balance    DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_balance_updated_at
    ON balance(updated_at);