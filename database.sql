CREATE DATABASE HomestayDB;
GO

USE HomestayDB;
GO

-- Users Table (Admin/Staff)
CREATE TABLE Users (
    id INT PRIMARY KEY IDENTITY(1,1),
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL, -- Should be hashed
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'STAFF'))
);

-- Rooms Table
CREATE TABLE Rooms (
    id INT PRIMARY KEY IDENTITY(1,1),
    name VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('AVAILABLE', 'OCCUPIED', 'RESERVED')),
    price DECIMAL(10, 2) NOT NULL,
    image_path VARCHAR(255) -- Path to image on server
);

-- Tenants Table
CREATE TABLE Tenants (
    id INT PRIMARY KEY IDENTITY(1,1),
    name NVARCHAR(100) NOT NULL,
    id_card VARCHAR(20) UNIQUE NOT NULL,
    phone VARCHAR(15),
    room_id INT,
    contract_path VARCHAR(255), -- Path to contract file
    check_in_date DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (room_id) REFERENCES Rooms(id) ON DELETE SET NULL
);

-- Services Table (Configuration for unit prices)
CREATE TABLE Services (
    id INT PRIMARY KEY IDENTITY(1,1),
    name NVARCHAR(50) NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL
);

-- Invoices Table
CREATE TABLE Invoices (
    id INT PRIMARY KEY IDENTITY(1,1),
    room_id INT NOT NULL,
    month INT NOT NULL,
    year INT NOT NULL,
    electricity_usage INT NOT NULL, -- kWh
    water_usage INT NOT NULL, -- m3
    internet_fee DECIMAL(10, 2) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) CHECK (status IN ('PAID', 'UNPAID')),
    created_at DATETIME DEFAULT GETDATE(),
    start_date DATETIME,
    end_date DATETIME,
    FOREIGN KEY (room_id) REFERENCES Rooms(id)
);

-- Tenant History Table (Lịch sử khách thuê)
CREATE TABLE TenantHistory (
    id INT PRIMARY KEY IDENTITY(1,1),
    name NVARCHAR(100) NOT NULL,
    id_card VARCHAR(20) NOT NULL,
    phone VARCHAR(15),
    room_id INT,
    contract_path VARCHAR(255),
    check_in_date DATETIME,
    checkout_date DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (room_id) REFERENCES Rooms(id) ON DELETE SET NULL
);

-- Insert Default Admin
INSERT INTO Users (username, password, role) VALUES ('admin', 'admin123', 'ADMIN');

-- Insert Default Services Prices
INSERT INTO Services (name, unit_price) VALUES ('Electricity', 3500);
INSERT INTO Services (name, unit_price) VALUES ('Water', 15000);
INSERT INTO Services (name, unit_price) VALUES ('Internet', 100000);
