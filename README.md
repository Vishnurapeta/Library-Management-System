# Library Management System

A console-based Library Management System built in **Java**, using **JDBC** to connect to a **MySQL** database. It manages book records, member registrations, and book issue/return transactions for a simulated library.

## Features

- Full **CRUD** operations on books (Create, Read, Update, Delete)
- **JDBC + MySQL** persistence across 3 normalized tables: `books`, `members`, `transactions`
- All queries use `PreparedStatement` — no string-concatenated SQL, eliminating SQL injection risk
- **Role-based access control**
  - **Admin**: add/update/delete books, view all members, view all transactions, view overdue books, generate reports
  - **Member**: search/browse available books, borrow a book, return a book, view personal borrowing history
- 10+ SQL queries covering:
  - `SELECT` with `WHERE` filters (search, overdue lookup, available copies)
  - `JOIN` across `transactions` ↔ `books` ↔ `members`
  - Aggregate functions: `COUNT`, `GROUP BY` (books per category, most-borrowed books)

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17+ |
| Database | MySQL 8.x |
| Connectivity | JDBC (mysql-connector-j) |
| Interface | Console (Scanner-based menu) |

## Project Structure
LibraryManagementSystem/
├── sql/
│   └── schema.sql              # DB schema + seed data (3 tables)
├── src/main/java/library/
│   ├── model/                  # Book, Member, Transaction (POJOs)
│   ├── dao/                    # BookDAO, MemberDAO, TransactionDAO (JDBC + CRUD)
│   ├── util/DBConnection.java  # Centralized JDBC connection handler
│   └── app/LibraryApp.java     # Console entry point, login + role-based menus
└── README.md

## Database Schema

**books** — book_id (PK), title, author, isbn, category, total_copies, available_copies
**members** — member_id (PK), name, email, phone, role (ADMIN/MEMBER), password, joined_on
**transactions** — transaction_id (PK), book_id (FK), member_id (FK), issue_date, due_date, return_date, status

## Setup Instructions

### 1. Create the database
```bash
mysql -u root -p < sql/schema.sql
```
This creates `library_db` with the three tables and seeds sample books + members.

### 2. Download the MySQL JDBC driver
Download `mysql-connector-j-8.x.x.jar` from [MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/) and place it in a `lib/` folder in the project root.

### 3. Configure the connection
Edit `src/main/java/library/util/DBConnection.java` and update `DB_USER` / `DB_PASSWORD` to match your local MySQL credentials.

### 4. Compile
```bash
javac -cp "lib/mysql-connector-j-8.x.x.jar" -d out $(find src -name "*.java")
```

### 5. Run
```bash
java -cp "out:lib/mysql-connector-j-8.x.x.jar" library.app.LibraryApp
```
(On Windows, use `;` instead of `:` in the classpath.)

## Demo Login Credentials (seeded)

| Role | Email | Password |
|---|---|---|
| Admin | admin@library.com | admin123 |
| Member | vishnu@library.com | member123 |

## Sample Workflow

1. Log in as **Admin** → add a new book → view all books → check the category report.
2. Log in as **Member** → search for a book → borrow it → view borrowing history.
3. Log back in as **Admin** → view all transactions → return the book on the member's behalf.

## Notes

This project was built to demonstrate core Java, JDBC, and relational database skills: connection management, prepared statements, normalized schema design, joins, aggregates, and simple role-based access control in a console application.
