-- =============================================================
-- E-Commerce API - PostgreSQL Database Schema
-- Run this in pgAdmin or psql to create the database/tables
-- =============================================================

-- 1. Create the database (run this separately if needed)
-- CREATE DATABASE ecommerce_db;

-- 2. Connect to ecommerce_db, then run everything below:

-- ─────────────────────────────────────────
-- USERS
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id          BIGSERIAL PRIMARY KEY,
    first_name  VARCHAR(50)  NOT NULL,
    last_name   VARCHAR(50)  NOT NULL,
    email       VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(20)  NOT NULL DEFAULT 'USER',
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- ─────────────────────────────────────────
-- CATEGORIES
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS categories (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

-- ─────────────────────────────────────────
-- PRODUCTS
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS products (
    id             BIGSERIAL PRIMARY KEY,
    name           VARCHAR(200)   NOT NULL,
    description    TEXT,
    price          NUMERIC(10, 2) NOT NULL,
    stock_quantity INTEGER        NOT NULL DEFAULT 0,
    image_url      VARCHAR(500),
    category_id    BIGINT REFERENCES categories(id) ON DELETE SET NULL,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ─────────────────────────────────────────
-- CARTS
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS carts (
    id      BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE REFERENCES users(id) ON DELETE CASCADE
);

-- ─────────────────────────────────────────
-- CART ITEMS
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS cart_items (
    id         BIGSERIAL PRIMARY KEY,
    cart_id    BIGINT  NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    product_id BIGINT  NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    quantity   INTEGER NOT NULL DEFAULT 1
);

-- ─────────────────────────────────────────
-- ORDERS
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS orders (
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT         REFERENCES users(id) ON DELETE SET NULL,
    status           VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    total_amount     NUMERIC(10, 2) NOT NULL,
    shipping_address TEXT           NOT NULL,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ─────────────────────────────────────────
-- ORDER ITEMS
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS order_items (
    id         BIGSERIAL PRIMARY KEY,
    order_id   BIGINT         NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id BIGINT         REFERENCES products(id) ON DELETE SET NULL,
    quantity   INTEGER        NOT NULL,
    price      NUMERIC(10, 2) NOT NULL
);

-- ─────────────────────────────────────────
-- INDEXES for performance
-- ─────────────────────────────────────────
CREATE INDEX IF NOT EXISTS idx_products_category    ON products(category_id);
CREATE INDEX IF NOT EXISTS idx_orders_user          ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_cart      ON cart_items(cart_id);
CREATE INDEX IF NOT EXISTS idx_order_items_order    ON order_items(order_id);
