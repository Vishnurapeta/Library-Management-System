package library.app;

import library.dao.BookDAO;
import library.dao.MemberDAO;
import library.dao.TransactionDAO;
import library.model.Book;
import library.model.Member;
import library.model.Transaction;

import java.util.List;
import java.util.Scanner;

/**
 * Console-based Library Management System.
 * Role-based access: ADMIN manages inventory & members, MEMBER can search/borrow/return.
 */
public class LibraryApp {

    private static final Scanner scanner = new Scanner(System.in);
    private static final BookDAO bookDAO = new BookDAO();
    private static final MemberDAO memberDAO = new MemberDAO();
    private static final TransactionDAO transactionDAO = new TransactionDAO();

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("   LIBRARY MANAGEMENT SYSTEM");
        System.out.println("=========================================");

        Member loggedInUser = login();
        if (loggedInUser == null) {
            System.out.println("Login failed. Exiting.");
            return;
        }

        System.out.println("\nWelcome, " + loggedInUser.getName() + " (" + loggedInUser.getRole() + ")");

        if (loggedInUser.isAdmin()) {
            adminMenu(loggedInUser);
        } else {
            memberMenu(loggedInUser);
        }

        System.out.println("\nGoodbye!");
    }

    private static Member login() {
        System.out.print("\nEmail: ");
        String email = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        return memberDAO.authenticate(email, password);
    }

    // ---------------- ADMIN MENU ----------------
    private static void adminMenu(Member admin) {
        boolean running = true;
        while (running) {
            System.out.println("\n----- ADMIN MENU -----");
            System.out.println("1. View All Books");
            System.out.println("2. Add Book");
            System.out.println("3. Update Book");
            System.out.println("4. Delete Book");
            System.out.println("5. View All Members");
            System.out.println("6. View All Transactions");
            System.out.println("7. View Overdue Books");
            System.out.println("8. Reports (Books by Category / Most Borrowed)");
            System.out.println("9. Return a Book (on behalf of member)");
            System.out.println("0. Logout");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> viewAllBooks();
                case "2" -> addBook();
                case "3" -> updateBook();
                case "4" -> deleteBook();
                case "5" -> viewAllMembers();
                case "6" -> viewAllTransactions();
                case "7" -> viewOverdueBooks();
                case "8" -> {
                    bookDAO.printBookCountByCategory();
                    transactionDAO.printMostBorrowedBooks();
                }
                case "9" -> returnBook();
                case "0" -> running = false;
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    // ---------------- MEMBER MENU ----------------
    private static void memberMenu(Member member) {
        boolean running = true;
        while (running) {
            System.out.println("\n----- MEMBER MENU -----");
            System.out.println("1. View Available Books");
            System.out.println("2. Search Books");
            System.out.println("3. Borrow a Book");
            System.out.println("4. Return a Book");
            System.out.println("5. My Borrowing History");
            System.out.println("0. Logout");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> bookDAO.getAvailableBooks().forEach(System.out::println);
                case "2" -> searchBooks();
                case "3" -> borrowBook(member);
                case "4" -> returnBook();
                case "5" -> transactionDAO.getTransactionsByMember(member.getMemberId()).forEach(System.out::println);
                case "0" -> running = false;
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    // ---------------- Book operations ----------------
    private static void viewAllBooks() {
        List<Book> books = bookDAO.getAllBooks();
        if (books.isEmpty()) {
            System.out.println("No books found.");
        }
        books.forEach(System.out::println);
    }

    private static void addBook() {
        System.out.print("Title: ");
        String title = scanner.nextLine();
        System.out.print("Author: ");
        String author = scanner.nextLine();
        System.out.print("ISBN: ");
        String isbn = scanner.nextLine();
        System.out.print("Category: ");
        String category = scanner.nextLine();
        System.out.print("Total Copies: ");
        int copies = Integer.parseInt(scanner.nextLine().trim());

        Book book = new Book(title, author, isbn, category, copies);
        System.out.println(bookDAO.addBook(book) ? "Book added successfully." : "Failed to add book.");
    }

    private static void updateBook() {
        System.out.print("Book ID to update: ");
        int id = Integer.parseInt(scanner.nextLine().trim());
        Book book = bookDAO.getBookById(id);
        if (book == null) {
            System.out.println("Book not found.");
            return;
        }
        System.out.print("New Title (" + book.getTitle() + "): ");
        String title = scanner.nextLine();
        if (!title.isBlank()) book.setTitle(title);

        System.out.print("New Total Copies (" + book.getTotalCopies() + "): ");
        String copiesStr = scanner.nextLine();
        if (!copiesStr.isBlank()) book.setTotalCopies(Integer.parseInt(copiesStr.trim()));

        System.out.println(bookDAO.updateBook(book) ? "Book updated." : "Update failed.");
    }

    private static void deleteBook() {
        System.out.print("Book ID to delete: ");
        int id = Integer.parseInt(scanner.nextLine().trim());
        System.out.println(bookDAO.deleteBook(id) ? "Book deleted." : "Delete failed.");
    }

    private static void searchBooks() {
        System.out.print("Search keyword (title/author/category): ");
        String keyword = scanner.nextLine();
        List<Book> results = bookDAO.searchBooks(keyword);
        if (results.isEmpty()) {
            System.out.println("No matches found.");
        }
        results.forEach(System.out::println);
    }

    // ---------------- Member operations ----------------
    private static void viewAllMembers() {
        memberDAO.getAllMembers().forEach(System.out::println);
    }

    // ---------------- Transaction operations ----------------
    private static void borrowBook(Member member) {
        System.out.print("Book ID to borrow: ");
        int bookId = Integer.parseInt(scanner.nextLine().trim());
        Book book = bookDAO.getBookById(bookId);
        if (book == null || book.getAvailableCopies() <= 0) {
            System.out.println("Book not available.");
            return;
        }
        boolean issued = transactionDAO.issueBook(bookId, member.getMemberId());
        if (issued) {
            bookDAO.updateAvailableCopies(bookId, -1);
            System.out.println("Book issued successfully. Due in 14 days.");
        } else {
            System.out.println("Failed to issue book.");
        }
    }

    private static void returnBook() {
        System.out.print("Transaction ID: ");
        int transactionId = Integer.parseInt(scanner.nextLine().trim());
        Transaction t = transactionDAO.getById(transactionId);
        if (t == null || "RETURNED".equals(t.getStatus())) {
            System.out.println("Invalid transaction or already returned.");
            return;
        }
        boolean returned = transactionDAO.returnBook(transactionId);
        if (returned) {
            bookDAO.updateAvailableCopies(t.getBookId(), 1);
            System.out.println("Book returned successfully.");
        } else {
            System.out.println("Failed to return book.");
        }
    }

    private static void viewAllTransactions() {
        transactionDAO.getAllTransactions().forEach(System.out::println);
    }

    private static void viewOverdueBooks() {
        List<Transaction> overdue = transactionDAO.getOverdueBooks();
        if (overdue.isEmpty()) {
            System.out.println("No overdue books.");
        }
        overdue.forEach(System.out::println);
    }
}
