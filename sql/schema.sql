-- Library Management System Database Schema
-- 3 normalized tables: books, members, transactions

DROP DATABASE IF EXISTS library_db;
CREATE DATABASE library_db;
USE library_db;

-- Table 1: Books
CREATE TABLE books (
    book_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    author VARCHAR(100) NOT NULL,
    isbn VARCHAR(20) UNIQUE,
    category VARCHAR(50),
    total_copies INT NOT NULL DEFAULT 1,
    available_copies INT NOT NULL DEFAULT 1
);

-- Table 2: Members
CREATE TABLE members (
    member_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(15),
    role ENUM('ADMIN', 'MEMBER') NOT NULL DEFAULT 'MEMBER',
    password VARCHAR(100) NOT NULL,
    joined_on DATE DEFAULT (CURRENT_DATE)
);

-- Table 3: Transactions (issue/return records)
CREATE TABLE transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    book_id INT NOT NULL,
    member_id INT NOT NULL,
    issue_date DATE NOT NULL DEFAULT (CURRENT_DATE),
    due_date DATE NOT NULL,
    return_date DATE DEFAULT NULL,
    status ENUM('ISSUED', 'RETURNED') NOT NULL DEFAULT 'ISSUED',
    FOREIGN KEY (book_id) REFERENCES books(book_id),
    FOREIGN KEY (member_id) REFERENCES members(member_id)
);

-- Seed data
INSERT INTO members (name, email, phone, role, password) VALUES
('Admin User', 'admin@library.com', '9999999999', 'ADMIN', 'admin123'),
('Vishnu Vardhan', 'vishnu@library.com', '8885423963', 'MEMBER', 'member123'),
('Priya Sharma', 'priya@library.com', '9876543210', 'MEMBER', 'member123');

INSERT INTO books (title, author, isbn, category, total_copies, available_copies) VALUES
('Effective Java', 'Joshua Bloch', '9780134685991', 'Programming', 4, 4),
('Clean Code', 'Robert C. Martin', '9780132350884', 'Programming', 3, 3),
('Introduction to Algorithms', 'Cormen, Leiserson, Rivest', '9780262033848', 'Computer Science', 2, 2),
('Database System Concepts', 'Silberschatz, Korth', '9780078022159', 'Database', 3, 3),
('Head First Design Patterns', 'Freeman & Robson', '9780596007126', 'Programming', 2, 2);
