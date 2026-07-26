-- =====================================================================
-- Fashion Design & Clothing Management System
-- Tie & Dye Frocks and Sarees - Small Business Edition
-- MySQL Database Schema (matches the UML class diagram)
-- =====================================================================

DROP DATABASE IF EXISTS fashion_design_system;
CREATE DATABASE fashion_design_system;
USE fashion_design_system;

-- ---------------------------------------------------------------------
-- USERS (abstract superclass -> single-table inheritance with role)
-- ---------------------------------------------------------------------
CREATE TABLE users (
    user_id     VARCHAR(20) PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    phone       VARCHAR(20),
    address     VARCHAR(255),
    role        ENUM('DESIGNER', 'CUSTOMER', 'ADMIN') NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------
-- DESIGNER (only one for this small business, enforced in app logic)
-- ---------------------------------------------------------------------
CREATE TABLE designers (
    design_er_id  VARCHAR(20) PRIMARY KEY,
    user_id       VARCHAR(20) NOT NULL UNIQUE,
    speciality    VARCHAR(100) DEFAULT 'Tie & Dye Frocks and Sarees',
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- CUSTOMER
-- ---------------------------------------------------------------------
CREATE TABLE customers (
    customer_id VARCHAR(20) PRIMARY KEY,
    user_id     VARCHAR(20) NOT NULL UNIQUE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- ADMIN (only one for this small business, enforced in app logic)
-- ---------------------------------------------------------------------
CREATE TABLE admins (
    admin_id VARCHAR(20) PRIMARY KEY,
    user_id  VARCHAR(20) NOT NULL UNIQUE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- FABRIC
-- ---------------------------------------------------------------------
CREATE TABLE fabrics (
    fabric_id       VARCHAR(20) PRIMARY KEY,
    fabric_name     VARCHAR(100) NOT NULL,
    color           VARCHAR(50),
    price_per_unit  DECIMAL(10,2) NOT NULL
);

-- ---------------------------------------------------------------------
-- DESIGN  (the designer's sketches - frock / saree, tie & dye patterns)
-- ---------------------------------------------------------------------
CREATE TABLE designs (
    design_id     VARCHAR(20) PRIMARY KEY,
    designer_id   VARCHAR(20) NOT NULL,
    design_name   VARCHAR(150) NOT NULL,
    category      ENUM('FROCK', 'SAREE') NOT NULL,
    pattern       VARCHAR(100) DEFAULT 'Tie & Dye',
    fabric_id     VARCHAR(20),
    price         DECIMAL(10,2) NOT NULL,
    sketch_image  VARCHAR(255),
    description   TEXT,
    status        ENUM('AVAILABLE', 'ARCHIVED') DEFAULT 'AVAILABLE',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (designer_id) REFERENCES designers(design_er_id) ON DELETE CASCADE,
    FOREIGN KEY (fabric_id) REFERENCES fabrics(fabric_id) ON DELETE SET NULL
);

-- ---------------------------------------------------------------------
-- CART
-- ---------------------------------------------------------------------
CREATE TABLE carts (
    cart_id      VARCHAR(20) PRIMARY KEY,
    customer_id  VARCHAR(20) NOT NULL,
    created_date DATE NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- CART ITEM
-- ---------------------------------------------------------------------
CREATE TABLE cart_items (
    cart_item_id VARCHAR(20) PRIMARY KEY,
    cart_id      VARCHAR(20) NOT NULL,
    design_id    VARCHAR(20) NOT NULL,
    size         ENUM('XS','S','M','L','XL','XXL','NONE') NOT NULL,
    quantity     INT NOT NULL DEFAULT 1,
    custom_note  VARCHAR(255),
    FOREIGN KEY (cart_id) REFERENCES carts(cart_id) ON DELETE CASCADE,
    FOREIGN KEY (design_id) REFERENCES designs(design_id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- ORDER
-- ---------------------------------------------------------------------
CREATE TABLE orders (
    order_id      VARCHAR(20) PRIMARY KEY,
    customer_id   VARCHAR(20) NOT NULL,
    total_amount  DECIMAL(10,2) NOT NULL DEFAULT 0,
    status        ENUM('PENDING','CONFIRMED','IN_PROGRESS','COMPLETED','CANCELLED') DEFAULT 'PENDING',
    order_date    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- ORDER ITEM
-- ---------------------------------------------------------------------
CREATE TABLE order_items (
    order_item_id VARCHAR(20) PRIMARY KEY,
    order_id      VARCHAR(20) NOT NULL,
    design_id     VARCHAR(20) NOT NULL,
    size          ENUM('XS','S','M','L','XL','XXL','NONE') NOT NULL,
    quantity      INT NOT NULL DEFAULT 1,
    unit_price    DECIMAL(10,2) NOT NULL,
    custom_note   VARCHAR(255),
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (design_id) REFERENCES designs(design_id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- PAYMENT
-- ---------------------------------------------------------------------
CREATE TABLE payments (
    payment_id   VARCHAR(20) PRIMARY KEY,
    order_id     VARCHAR(20) NOT NULL,
    method       ENUM('CASH','ONLINE') NOT NULL,
    amount       DECIMAL(10,2) NOT NULL,
    status       ENUM('PENDING','COMPLETED','FAILED') DEFAULT 'PENDING',
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- DELIVERY
-- ---------------------------------------------------------------------
CREATE TABLE deliveries (
    delivery_id   VARCHAR(20) PRIMARY KEY,
    order_id      VARCHAR(20) NOT NULL,
    address       VARCHAR(255) NOT NULL,
    status        ENUM('PREPARING','DISPATCHED','DELIVERED','RETURNED') DEFAULT 'PREPARING',
    delivery_date DATE,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- INVENTORY (managed by Admin)
-- ---------------------------------------------------------------------
CREATE TABLE inventory (
    inventory_id VARCHAR(20) PRIMARY KEY,
    item_type    VARCHAR(50) NOT NULL,
    item_name    VARCHAR(100) NOT NULL,
    quantity     INT NOT NULL DEFAULT 0,
    unit_price   DECIMAL(10,2) NOT NULL
);

-- =====================================================================
-- SEED DATA
-- One Designer, one Admin (per business requirement), sample fabrics
-- =====================================================================

INSERT INTO users (user_id, name, email, password, phone, address, role) VALUES
('U-ADM-001', 'Thoshika Chamodi', 'thoshika@gmail.com', 'admin123', '0771234567', 'Colombo, Sri Lanka', 'ADMIN'),
('U-DES-001', 'Tharindi Kavindya', 'tharindi@gmail.com', 'design123', '0779876543', 'Colombo, Sri Lanka', 'DESIGNER');

INSERT INTO admins (admin_id, user_id) VALUES ('ADM-001', 'U-ADM-001');
INSERT INTO designers (design_er_id, user_id, speciality) VALUES ('DES-001', 'U-DES-001', 'Tie & Dye Frocks and Sarees');

INSERT INTO fabrics (fabric_id, fabric_name, color, price_per_unit) VALUES
('FAB-001', 'Cotton', 'Indigo Blue', 850.00),
('FAB-002', 'Cotton', 'Madder Red', 850.00),
('FAB-003', 'Rayon', 'Turmeric Yellow', 950.00),
('FAB-004', 'Silk Blend', 'Multicolor Spiral', 1800.00);

INSERT INTO designs (design_id, designer_id, design_name, category, pattern, fabric_id, price, sketch_image, description) VALUES
('DSG-001', 'DES-001', 'Indigo Spiral Saree', 'SAREE', 'Spiral Tie & Dye', 'FAB-004', 6500.00, 'assets/image1.svg', 'Hand tie-dyed silk-blend saree with an indigo spiral motif and contrast pallu.'),
('DSG-002', 'DES-001', 'Sunset Shibori Saree', 'FROCK', 'Shibori Tie & Dye', 'FAB-003', 3200.00, 'assets/image3.png', 'Flowy cotton-rayon frock in a turmeric-to-madder shibori gradient.'),
('DSG-003', 'DES-001', 'Ink Wash Cotton Saree', 'SAREE', 'Mandala Tie & Dye', 'FAB-001', 5800.00, 'assets/image4.png', 'Handloom cotton saree with mandala-style resist dyeing in deep indigo.'),
('DSG-004', 'DES-001', 'Marigold Bloom Saree', 'FROCK', 'Bullseye Tie & Dye', 'FAB-002', 2900.00, 'assets/image5.png', 'Fitted-waist frock with bold bullseye tie-dye rings in madder red.');

INSERT INTO inventory (inventory_id, item_type, item_name, quantity, unit_price) VALUES
('INV-001', 'Fabric', 'Cotton - Indigo Blue', 40, 850.00),
('INV-002', 'Fabric', 'Cotton - Madder Red', 35, 850.00),
('INV-003', 'Fabric', 'Rayon - Turmeric Yellow', 25, 950.00),
('INV-004', 'Fabric', 'Silk Blend - Multicolor Spiral', 15, 1800.00),
('INV-005', 'Dye', 'Natural Indigo Dye', 20, 450.00),
('INV-006', 'Dye', 'Madder Root Dye', 20, 400.00);
