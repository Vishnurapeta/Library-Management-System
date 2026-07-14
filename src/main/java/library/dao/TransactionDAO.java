package library.dao;

import library.model.Transaction;
import library.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    // CREATE - issue a book (14-day loan period)
    public boolean issueBook(int bookId, int memberId) {
        String sql = "INSERT INTO transactions (book_id, member_id, issue_date, due_date, status) VALUES (?, ?, ?, ?, 'ISSUED')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            LocalDate today = LocalDate.now();
            ps.setInt(1, bookId);
            ps.setInt(2, memberId);
            ps.setDate(3, Date.valueOf(today));
            ps.setDate(4, Date.valueOf(today.plusDays(14)));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error issuing book: " + e.getMessage());
            return false;
        }
    }

    // UPDATE - return a book
    public boolean returnBook(int transactionId) {
        String sql = "UPDATE transactions SET return_date = ?, status = 'RETURNED' WHERE transaction_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(LocalDate.now()));
            ps.setInt(2, transactionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error returning book: " + e.getMessage());
            return false;
        }
    }

    // READ - all transactions with JOIN across 3 tables
    public List<Transaction> getAllTransactions() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t.*, b.title AS book_title, m.name AS member_name " +
                "FROM transactions t " +
                "JOIN books b ON t.book_id = b.book_id " +
                "JOIN members m ON t.member_id = m.member_id " +
                "ORDER BY t.transaction_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching transactions: " + e.getMessage());
        }
        return list;
    }

    // READ - transactions for a specific member (role-based: members see only their own)
    public List<Transaction> getTransactionsByMember(int memberId) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t.*, b.title AS book_title, m.name AS member_name " +
                "FROM transactions t " +
                "JOIN books b ON t.book_id = b.book_id " +
                "JOIN members m ON t.member_id = m.member_id " +
                "WHERE t.member_id = ? ORDER BY t.transaction_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching member transactions: " + e.getMessage());
        }
        return list;
    }

    // READ - currently overdue books (SELECT with filter)
    public List<Transaction> getOverdueBooks() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t.*, b.title AS book_title, m.name AS member_name " +
                "FROM transactions t " +
                "JOIN books b ON t.book_id = b.book_id " +
                "JOIN members m ON t.member_id = m.member_id " +
                "WHERE t.status = 'ISSUED' AND t.due_date < CURDATE()";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching overdue books: " + e.getMessage());
        }
        return list;
    }

    // Aggregate - most borrowed books (report generation)
    public void printMostBorrowedBooks() {
        String sql = "SELECT b.title, COUNT(*) AS times_borrowed " +
                "FROM transactions t JOIN books b ON t.book_id = b.book_id " +
                "GROUP BY b.title ORDER BY times_borrowed DESC LIMIT 5";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            System.out.println("\n--- Most Borrowed Books ---");
            while (rs.next()) {
                System.out.printf("%-30s : %d times%n", rs.getString("title"), rs.getInt("times_borrowed"));
            }
        } catch (SQLException e) {
            System.err.println("Error generating report: " + e.getMessage());
        }
    }

    public Transaction getById(int transactionId) {
        String sql = "SELECT t.*, b.title AS book_title, m.name AS member_name " +
                "FROM transactions t " +
                "JOIN books b ON t.book_id = b.book_id " +
                "JOIN members m ON t.member_id = m.member_id " +
                "WHERE t.transaction_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, transactionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching transaction: " + e.getMessage());
        }
        return null;
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setTransactionId(rs.getInt("transaction_id"));
        t.setBookId(rs.getInt("book_id"));
        t.setMemberId(rs.getInt("member_id"));
        t.setIssueDate(rs.getDate("issue_date"));
        t.setDueDate(rs.getDate("due_date"));
        t.setReturnDate(rs.getDate("return_date"));
        t.setStatus(rs.getString("status"));
        t.setBookTitle(rs.getString("book_title"));
        t.setMemberName(rs.getString("member_name"));
        return t;
    }
}
