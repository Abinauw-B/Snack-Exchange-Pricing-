-- ====================================================================
-- Enterprise Pub Exchange (Noida Pub Exchange Model) Database DDL Script
-- Database: PostgreSQL 16+
-- Schema: Public
-- Includes: PKs, FKs, Indexes, Constraints, Soft Delete, & Audit Columns
-- ====================================================================

-- 1. Users, Roles & Security Permissions
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL
);

CREATE TABLE IF NOT EXISTS permissions (
    id BIGSERIAL PRIMARY KEY,
    permission_code VARCHAR(100) NOT NULL UNIQUE,
    module VARCHAR(50) NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    phone_number VARCHAR(20),
    role_id BIGINT NOT NULL REFERENCES roles(id),
    status VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL,
    last_login_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL
);

-- 2. Beverage Menu & Categories
CREATE TABLE IF NOT EXISTS beverage_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    category_weight DOUBLE PRECISION DEFAULT 1.0 NOT NULL,
    display_order INT DEFAULT 0 NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL
);

CREATE TABLE IF NOT EXISTS beverages (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL REFERENCES beverage_categories(id),
    name VARCHAR(150) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    image_url VARCHAR(500),
    default_serving_ml INT DEFAULT 250 NOT NULL,
    base_price DECIMAL(12, 2) NOT NULL,
    current_price DECIMAL(12, 2) NOT NULL,
    min_floor_price DECIMAL(12, 2) NOT NULL,
    max_ceiling_price DECIMAL(12, 2) NOT NULL,
    popularity_score DOUBLE PRECISION DEFAULT 50.0 NOT NULL,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    CONSTRAINT chk_price_bounds CHECK (min_floor_price <= current_price AND current_price <= max_ceiling_price)
);

-- 3. Inventory & Stock Tracking (Bottles / Kegs / Batches)
CREATE TABLE IF NOT EXISTS inventory_batches (
    id BIGSERIAL PRIMARY KEY,
    beverage_id BIGINT NOT NULL REFERENCES beverages(id),
    batch_code VARCHAR(100) NOT NULL UNIQUE,
    container_type VARCHAR(30) DEFAULT 'KEG_20L' NOT NULL, -- KEG_20L, BOTTLE, GLASS
    initial_volume_ml INT NOT NULL,
    remaining_volume_ml INT NOT NULL,
    expiry_date DATE,
    vendor_name VARCHAR(150),
    cost_per_batch DECIMAL(12, 2),
    status VARCHAR(30) DEFAULT 'ACTIVE' NOT NULL, -- ACTIVE, DEPLETED, EXPIRED
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL
);

CREATE TABLE IF NOT EXISTS stock_movements (
    id BIGSERIAL PRIMARY KEY,
    batch_id BIGINT NOT NULL REFERENCES inventory_batches(id),
    movement_type VARCHAR(30) NOT NULL, -- CONSUMPTION, PURCHASE, WASTE, RETURN
    volume_change_ml INT NOT NULL,
    reference_order_id VARCHAR(100),
    notes TEXT,
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 4. Dynamic Pricing Engine & Live Market Tracking
CREATE TABLE IF NOT EXISTS live_prices (
    beverage_id BIGINT PRIMARY KEY REFERENCES beverages(id),
    current_price DECIMAL(12, 2) NOT NULL,
    previous_price DECIMAL(12, 2) NOT NULL,
    price_change_amount DECIMAL(12, 2) NOT NULL,
    price_change_pct DOUBLE PRECISION NOT NULL,
    trend_direction VARCHAR(10) NOT NULL, -- UP, DOWN, FLAT
    demand_score DOUBLE PRECISION NOT NULL,
    velocity_score DOUBLE PRECISION NOT NULL,
    stock_pressure_score DOUBLE PRECISION NOT NULL,
    time_decay_factor DOUBLE PRECISION NOT NULL,
    last_updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS price_history (
    id BIGSERIAL PRIMARY KEY,
    beverage_id BIGINT NOT NULL REFERENCES beverages(id),
    old_price DECIMAL(12, 2) NOT NULL,
    new_price DECIMAL(12, 2) NOT NULL,
    price_delta DECIMAL(12, 2) NOT NULL,
    demand_score DOUBLE PRECISION NOT NULL,
    stock_pressure_pct DOUBLE PRECISION NOT NULL,
    trigger_source VARCHAR(50) NOT NULL, -- SCHEDULER_60S, POS_SURGE, MARKET_CRASH, ADMIN_OVERRIDE
    explanation_log TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS price_rules (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT REFERENCES beverage_categories(id),
    rule_name VARCHAR(100) NOT NULL,
    weight_velocity DOUBLE PRECISION DEFAULT 0.40 NOT NULL,
    weight_stock_pressure DOUBLE PRECISION DEFAULT 0.40 NOT NULL,
    weight_time_decay DOUBLE PRECISION DEFAULT 0.20 NOT NULL,
    margin_protection_min_pct DOUBLE PRECISION DEFAULT 20.0 NOT NULL,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS demand_metrics (
    id BIGSERIAL PRIMARY KEY,
    beverage_id BIGINT NOT NULL REFERENCES beverages(id),
    window_start TIMESTAMP WITH TIME ZONE NOT NULL,
    window_end TIMESTAMP WITH TIME ZONE NOT NULL,
    orders_count INT DEFAULT 0 NOT NULL,
    volume_sold_ml INT DEFAULT 0 NOT NULL,
    velocity_rate DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 5. Order Locks & Locked Price Versions
CREATE TABLE IF NOT EXISTS order_locks (
    id BIGSERIAL PRIMARY KEY,
    lock_token VARCHAR(100) NOT NULL UNIQUE,
    beverage_id BIGINT NOT NULL REFERENCES beverages(id),
    locked_price DECIMAL(12, 2) NOT NULL,
    price_version INT NOT NULL,
    quantity INT NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    is_redeemed BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(100) NOT NULL UNIQUE,
    table_number VARCHAR(30),
    customer_id BIGINT REFERENCES users(id),
    waiter_id BIGINT REFERENCES users(id),
    order_type VARCHAR(30) DEFAULT 'QR_MOBILE' NOT NULL, -- QR_MOBILE, WAITER_POS, CASHIER_POS
    status VARCHAR(30) DEFAULT 'PENDING' NOT NULL, -- PENDING, LOCKED, PAID, PREPARING, SERVED, CANCELLED
    total_amount DECIMAL(12, 2) NOT NULL,
    discount_amount DECIMAL(12, 2) DEFAULT 0.00 NOT NULL,
    net_payable_amount DECIMAL(12, 2) NOT NULL,
    payment_status VARCHAR(30) DEFAULT 'UNPAID' NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL
);

CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    beverage_id BIGINT NOT NULL REFERENCES beverages(id),
    locked_price_id BIGINT REFERENCES order_locks(id),
    unit_price DECIMAL(12, 2) NOT NULL,
    quantity INT NOT NULL,
    subtotal DECIMAL(12, 2) NOT NULL,
    volume_deducted_ml INT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS payment_transactions (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    transaction_reference VARCHAR(150) NOT NULL UNIQUE,
    payment_method VARCHAR(30) NOT NULL, -- CASH, UPI, CARD, WALLET
    amount DECIMAL(12, 2) NOT NULL,
    gateway_name VARCHAR(50),
    status VARCHAR(30) NOT NULL, -- SUCCESS, FAILED, REFUNDED
    raw_response TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 6. WebSocket Clients & Customer Sessions
CREATE TABLE IF NOT EXISTS websocket_clients (
    session_id VARCHAR(100) PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    client_type VARCHAR(30) NOT NULL, -- MOBILE, DISPLAY_NODE, ADMIN_DASHBOARD
    ip_address VARCHAR(45),
    connected_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    last_ping_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS customer_sessions (
    id BIGSERIAL PRIMARY KEY,
    session_token VARCHAR(100) NOT NULL UNIQUE,
    table_number VARCHAR(30),
    user_id BIGINT REFERENCES users(id),
    wallet_balance DECIMAL(12, 2) DEFAULT 0.00 NOT NULL,
    active_until TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 7. Market Crash & Events Engine
CREATE TABLE IF NOT EXISTS market_crashes (
    id BIGSERIAL PRIMARY KEY,
    event_code VARCHAR(100) NOT NULL UNIQUE,
    trigger_type VARCHAR(30) NOT NULL, -- MANUAL, SCHEDULED, RANDOM_ALGORITHM
    duration_minutes INT NOT NULL,
    floor_price_override DECIMAL(12, 2) NOT NULL,
    status VARCHAR(30) NOT NULL, -- ACTIVE, COMPLETED, ABORTED
    triggered_by BIGINT REFERENCES users(id),
    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ended_at TIMESTAMP WITH TIME ZONE,
    total_orders_during_crash INT DEFAULT 0 NOT NULL,
    total_volume_sold_during_crash_ml INT DEFAULT 0 NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS market_events (
    id BIGSERIAL PRIMARY KEY,
    event_name VARCHAR(150) NOT NULL,
    event_type VARCHAR(50) NOT NULL, -- HAPPY_HOUR, WEEKEND_SURGE, WEATHER_EVENT, SPECIAL_PARTY
    price_multiplier DOUBLE PRECISION DEFAULT 1.0 NOT NULL,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 8. Audit Logs & System Monitoring
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    module VARCHAR(50) NOT NULL,
    details TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS scheduler_logs (
    id BIGSERIAL PRIMARY KEY,
    job_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    execution_time_ms BIGINT NOT NULL,
    details TEXT,
    executed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS digital_display_nodes (
    id BIGSERIAL PRIMARY KEY,
    node_name VARCHAR(100) NOT NULL UNIQUE,
    location VARCHAR(100) NOT NULL,
    display_mode VARCHAR(50) DEFAULT 'TICKER_GRID' NOT NULL, -- TICKER_GRID, TOP_GAINERS, CRASH_ALERT
    ip_address VARCHAR(45),
    status VARCHAR(20) DEFAULT 'ONLINE' NOT NULL,
    last_heartbeat_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS system_configuration (
    config_key VARCHAR(100) PRIMARY KEY,
    config_value VARCHAR(500) NOT NULL,
    description TEXT,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS analytics_snapshots (
    id BIGSERIAL PRIMARY KEY,
    snapshot_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    total_revenue DECIMAL(12, 2) NOT NULL,
    total_orders INT NOT NULL,
    total_volume_litres DOUBLE PRECISION NOT NULL,
    active_crash_count INT NOT NULL,
    top_gainer_beverage VARCHAR(150),
    top_loser_beverage VARCHAR(150),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- ====================================================================
-- Indexes for High Performance Queries
-- ====================================================================
CREATE INDEX IF NOT EXISTS idx_beverages_category ON beverages(category_id);
CREATE INDEX IF NOT EXISTS idx_beverages_code ON beverages(code);
CREATE INDEX IF NOT EXISTS idx_inventory_beverage ON inventory_batches(beverage_id);
CREATE INDEX IF NOT EXISTS idx_price_history_beverage ON price_history(beverage_id);
CREATE INDEX IF NOT EXISTS idx_price_history_created ON price_history(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_orders_number ON orders(order_number);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_order_locks_token ON order_locks(lock_token);
CREATE INDEX IF NOT EXISTS idx_market_crashes_status ON market_crashes(status);

-- Initial Seed Roles
INSERT INTO roles (name, description) VALUES 
('ADMIN', 'Enterprise Platform Administrator'),
('MANAGER', 'Pub Operations & Pricing Manager'),
('WAITER', 'POS Terminal Waiter Staff'),
('CASHIER', 'POS Terminal Cashier Staff'),
('CUSTOMER', 'Mobile Web QR Ordering Customer')
ON CONFLICT (name) DO NOTHING;

-- Initial Seed System Config
INSERT INTO system_configuration (config_key, config_value, description) VALUES
('pricing_interval_seconds', '60', 'Interval in seconds for automated pricing engine run'),
('market_crash_default_duration_mins', '3', 'Default duration for market crash routine'),
('price_lock_duration_seconds', '120', 'Duration in seconds for which an order price version is locked'),
('global_min_floor_price', '18.00', 'Hard floor price limit across all beverages'),
('global_max_ceiling_price', '25.00', 'Hard ceiling price limit across all beverages')
ON CONFLICT (config_key) DO NOTHING;
