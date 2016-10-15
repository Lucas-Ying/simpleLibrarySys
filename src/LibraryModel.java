/*
 * LibraryModel.java
 * Author:
 * Created on:
 */



import javax.swing.*;
import java.sql.*;

public class LibraryModel {

    private static final String URL = "jdbc:postgresql://db.ecs.vuw.ac.nz/yingming_jdbc";
    private static final String DRIVER = "org.postgresql.Driver";

    private String userid;
    private String password;
    private Connection connection;
    private Statement statemt;
    private ResultSet results;

    // For use in creating dialogs and making them modal
    private JFrame dialogParent;
    private JOptionPane optionPane;

    public LibraryModel(JFrame parent, String userid, String password) {
	    dialogParent = parent;
        this.userid = userid;
        this.password = password;
        optionPane = new JOptionPane();
        connection = initConnection();
    }

//    @SuppressWarnings("static-access")
    private Connection initConnection() {
        Connection connection = null;
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            System.out.println("Driver not found");
            e.printStackTrace();
        }
        System.out.println("Driver registered");
        try {
            connection = DriverManager.getConnection(URL, userid, password);
        } catch (SQLException e) {
            optionPane.showMessageDialog(dialogParent, "Authentication failed.");
        }

        if (connection != null) {
            System.out.println("Connection has been established.");
        }

        return connection;
    }

    public String bookLookup(int isbn) {
        String result = null;
        String select = "SELECT book.isbn, title, edition_no, numofcop, numleft, "
                + "surname, name FROM book LEFT JOIN book_author "
                + "ON book.isbn = book_author.isbn LEFT JOIN author "
                + "ON book_author.authorid = author.authorid "
                + "WHERE book.isbn = " + isbn + "ORDER BY authorSeqNo";

        try {
            connection.setAutoCommit(false);
            statemt = connection.createStatement();
            results = statemt.executeQuery(select);

            if (results != null) {
                result = "Book Lookup result for " + isbn + ":\n\n";
            }

            boolean firstEntry = true;
            while (results.next()) {
                if (firstEntry) {
                    String title = results.getString("title").trim();
                    int edition = results.getInt("edition_no");
                    int noCopy = results.getInt("numofcop");
                    int noLeft = results.getInt("numleft");
                    String lName = results.getString("surname");
                    String fName = results.getString("name");

                    lName = lName == null ? "" : lName.trim();
                    fName = fName == null ? "(no authors)" : fName.trim();

                    String entry = "Title: " + title + '\n' + "ISBN: " + isbn
                            + '\n' + "Edition: " + edition + '\n'
                            + "Number of Copies: " + noCopy + '\n'
                            + "Copies left: " + noLeft + '\n' + "Author: "
                            + fName + " " + lName;

                    result += entry;

                    firstEntry = false;
                } else {
                    String lName = results.getString("surname").trim();
                    String fName = results.getString("name").trim();
                    result += ", " + fName + " " + lName;
                }
            }
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getSQLState());
        }

        if (result.equals("Book Lookup result for " + isbn + ":\n\n")) {
            result += "No book has been found with ISBN " + isbn;
        }

        return result;
//        return "Lookup Book Stub";
    }

    public String showCatalogue() {
        String result = null;
        String select = "SELECT book.isbn, title, edition_no, numofcop, numleft, surname, name "
                + "FROM book LEFT JOIN book_author "
                + "ON book.isbn = book_author.isbn "
                + "LEFT JOIN author "
                + "ON book_author.authorid = author.authorid "
                + "ORDER BY isbn";

        try {
            connection.setAutoCommit(false);
            statemt = connection.createStatement();
            results = statemt.executeQuery(select);

            if (results != null) {
                result = "Show Catalogue:";
            }

            int preISBN = -1;
            while (results.next()) {
                int isbn = results.getInt("ISBN");
                if (isbn != preISBN) {
                    result += "\n\n";
                    preISBN = isbn;

                    String title = results.getString("title").trim();
                    int edition = results.getInt("edition_no");
                    int noCopy = results.getInt("numofcop");
                    int noLeft = results.getInt("numleft");
                    String lName = results.getString("surname");
                    String fName = results.getString("name");

                    lName = lName == null ? "" : lName.trim();
                    fName = fName == null ? "(no authors)" : fName.trim();

                    String entry = title == null ? "" : "Title: " + title
                            + '\n' + "ISBN: " + isbn + '\n' + "Edition: "
                            + edition + '\n' + "Number of Copies: " + noCopy
                            + '\n' + "Copies left: " + noLeft + '\n'
                            + "Author: " + fName + " " + lName;

                    result += entry;
                } else {
                    String lName = results.getString("surname").trim();
                    String fName = results.getString("name").trim();
                    result += ", " + fName + " " + lName;
                }
            }
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getSQLState());
        }

        if (result.equals("Show Catalogue\n\n")) {
            result += "Empty Catalogue";
        }

        return result;
//        return "Show Catalogue Stub";
    }

    public String showLoanedBooks() {
        String result = null;

        String select = "SELECT book.isbn, title, edition_no, numofcop, numleft, "
                + "surname, name, customer.customerid, f_name, l_name, city "
                + "FROM book RIGHT JOIN cust_book "
                + "ON book.isbn = cust_book.isbn "
                + "LEFT JOIN customer "
                + "ON customer.customerid = cust_book.customerid "
                + "LEFT JOIN book_author "
                + "ON book_author.isbn = book.isbn "
                + "LEFT JOIN author "
                + "ON book_author.authorid = author.authorid "
                + "ORDER BY book.isbn";

        try {
            connection.setAutoCommit(false);
            statemt = connection.createStatement();
            results = statemt.executeQuery(select);

            if (results != null) {
                result = "Show Loaned Books:\n";
            }

            int preISBN = -1;
            while (results.next()) {
                int isbn = results.getInt("ISBN");
                if (isbn != preISBN) {
                    // result += "\n\n";
                    preISBN = isbn;

                    String title = results.getString("title").trim();
                    int edition = results.getInt("edition_no");
                    int noCopy = results.getInt("numofcop");
                    int noLeft = results.getInt("numleft");
                    String author_lName = results.getString("surname");
                    String author_fName = results.getString("name");
                    int customerID = results.getInt("customerid");
                    String cust_lName = results.getString("l_name").trim();
                    String cust_fName = results.getString("f_name").trim();
                    String city = results.getString("city");

                    author_lName = author_lName == null ? "" : author_lName
                            .trim();
                    author_fName = author_fName == null ? "(no authors)"
                            : author_fName.trim();

                    String entry = "\n" + isbn + ": " + title + "\n"
                            + "\tEdition: " + edition + " - Number of copies: "
                            + noCopy + " - Copies left: " + noLeft + "\n"
                            + "\tAuthor: " + author_fName + " " + author_lName
                            + "\n" + "\tBorrowers:" + "\n\t\t" + customerID
                            + ": " + cust_lName + ", " + cust_fName + " - "
                            + city;

                    result += entry;
                } else {
                    int customerID = results.getInt("customerid");
                    String cust_lName = results.getString("l_name").trim();
                    String cust_fName = results.getString("f_name").trim();
                    String city = results.getString("city");
                    result += "\n\t\t" + customerID + ": " + cust_lName + ", "
                            + cust_fName + " - " + city;
                }
            }
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getSQLState());
        }

        if (result.equals("Show Loaned Books:\n")) {
            result += "(No Loaned Books)";
        }

        result += "\n";

        return result;
//        return "Show Loaned Books Stub";
    }

    public String showAuthor(int authorID) {
        String result = null;
        String select = "SELECT author.authorid, surname, name, book.isbn, title "
                + "FROM book RIGHT JOIN book_author "
                + "ON book.isbn = book_author.isbn RIGHT JOIN author "
                + "ON book_author.authorid = author.authorid "
                + "WHERE author.authorid = " + authorID + "ORDER BY authorid";

        try {
            connection.setAutoCommit(false);
            statemt = connection.createStatement();
            results = statemt.executeQuery(select);

            if (results != null) {
                result = "Show author:\n\n";
            }

            boolean firstEntry = true;
            while (results.next()) {
                if (firstEntry) {
                    String fName = results.getString("name").trim();
                    String lName = results.getString("surname").trim();
                    int isbn = results.getInt("isbn");
                    String title = results.getString("title");

                    if (title != null)
                        title = title.trim();

                    boolean hasBooks = title == null ? false : true;

                    String entry = null;
                    if (hasBooks) {
                        entry = authorID + " - " + fName + " " + lName + "\n"
                                + "Books written:" + "\n\t" + isbn + " - "
                                + title;
                    } else {
                        entry = authorID + " - " + fName + " " + lName + "\n"
                                + "\t(no books written)";
                    }

                    result += entry;

                    firstEntry = false;
                } else {
                    int isbn = results.getInt("isbn");
                    String title = results.getString("title").trim();
                    result += "\n\t" + isbn + " - " + title;
                }
            }
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getSQLState());
        }

        if (result == "Show author:\n\n") {
            result += "No such author ID: " + authorID;
        }

        return result;
//        return "Show Author Stub";
    }

    public String showAllAuthors() {
        String result = null;
        String select = "SELECT * FROM author ORDER BY authorid";

        try {
            connection.setAutoCommit(false);
            statemt = connection.createStatement();
            results = statemt.executeQuery(select);

            if (results != null) {
                result = "Show all authors:\n\n";
            }

            while (results.next()) {
                int id = results.getInt("authorid");
                String lName = results.getString("surname").trim();
                String fName = results.getString("name").trim();

                String entry = id + ": " + lName + ", " + fName + "\n";

                result += entry;
            }

            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getSQLState());
        }

        if (result.equals("Show all authors:\n\n")) {
            result += "No authors found.";
        }

        return result;
//        return "Show All Authors Stub";
    }

    public String showCustomer(int customerID) {
        String result = null;
        String select = "SELECT l_name, f_name, city, book.isbn, title "
                + "FROM customer RIGHT JOIN cust_book "
                + "ON customer.customerid = cust_book.customerid "
                + "LEFT JOIN book " + "ON book.isbn = cust_book.isbn "
                + "WHERE customer.customerid = " + customerID
                + "ORDER BY book.isbn";

        try {
            connection.setAutoCommit(false);
            statemt = connection.createStatement();
            results = statemt.executeQuery(select);

            if (results != null) {
                result = "Show customer:\n\n";
            }

            boolean firstEntry = true;
            while (results.next()) {
                if (firstEntry) {
                    String fName = results.getString("f_name").trim();
                    String lName = results.getString("l_name").trim();
                    String city = results.getString("city");
                    int isbn = results.getInt("isbn");
                    String title = results.getString("title");

                    if (title != null)
                        title = title.trim();

                    boolean hasBooks = title == null ? false : true;

                    String entry = null;
                    if (hasBooks) {
                        entry = customerID + ": " + lName + ", " + fName
                                + " - " + city + "\n" + "Books Borrowed:"
                                + "\n\t" + isbn + " - " + title;
                    } else {
                        entry = customerID + ": " + lName + ", " + fName
                                + " - " + city + "\n" + "\t(No books borrowed)";
                    }

                    result += entry;

                    firstEntry = false;
                } else {
                    int isbn = results.getInt("isbn");
                    String title = results.getString("title").trim();
                    result += "\n\t" + isbn + " - " + title;
                }
            }
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getSQLState());
        }

        if (result == "Show customer:\n\n") {
            result += "No such customer ID: " + customerID;
        }

        return result;
//        return "Show Customer Stub";
    }

    public String showAllCustomers() {
        String result = null;
        String select = "SELECT * FROM customer ORDER BY customerid;";

        try {
            connection.setAutoCommit(false);
            statemt = connection.createStatement();
            results = statemt.executeQuery(select);

            if (results != null) {
                result = "Show all customers:\n\n";
            }

            while (results.next()) {
                int id = results.getInt("customerid");
                String lName = results.getString("l_name").trim();
                String fName = results.getString("f_name").trim();
                String city = results.getString("city");

                city = city == null ? "(no city)" : city.trim();

                String entry = id + ": " + lName + ", " + fName + " - " + city
                        + "\n";

                result += entry;
            }

            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getSQLState());
        }

        if (result.equals("Show all authors:\n\n")) {
            result += "No authors found.";
        }

        return result;
//        return "Show All Customers Stub";
    }

    public String borrowBook(int isbn, int customerID,
			     int day, int month, int year) {
        String customer = "SELECT * FROM customer WHERE customerid = "
                + customerID + " FOR UPDATE";
        String book = "SELECT * FROM book WHERE isbn = " + isbn + "FOR UPDATE";
        String custBook = "SELECT * FROM cust_book FOR UPDATE";

        String customerName = null;
        String title = null;
        String dueDate = null;

        try {
            connection.setAutoCommit(false);
            statemt = connection.createStatement();

            results = statemt.executeQuery(customer);
            if (!results.next()) {
                throw new IllegalArgumentException("\n\tCustomer: "
                        + customerID + " does not exist.");
            }
            customerName = results.getString("f_name").trim() + " "
                    + results.getString("l_name").trim();

            results = statemt.executeQuery(book);
            if (!results.next()) {
                throw new IllegalArgumentException("\n\tBook: " + isbn
                        + " does not exist.");
            }
            title = results.getString("title").trim();

            int copyLeft = results.getInt("numleft");
            if (copyLeft == 0) {
                throw new IllegalArgumentException(
                        "\n\tNot enough copies of book " + isbn + " left.");
            }

            Date date = Date.valueOf(year + "-" + month + "-" + day);
            dueDate = date.toString();

            statemt.executeQuery(custBook);
            String insert = "INSERT INTO cust_book " + "VALUES(" + isbn + ", '"
                    + date + "', " + customerID + ")";
            String update = "UPDATE book SET numleft = numleft - 1 WHERE isbn = "
                    + isbn;

            int return_value = statemt.executeUpdate(insert);
            if (return_value == 0) {
                connection.rollback();
                connection.setAutoCommit(true);
                return "Cannot update cust_book table";
            }

            optionPane.showMessageDialog(dialogParent,
                            "Locked the tuple(s), ready to update. Click OK to continue.");
            return_value = statemt.executeUpdate(update);

            if (return_value == 0) {
                connection.rollback();
                connection.setAutoCommit(true);
                return "Cannot update book table";
            }

            connection.commit();
            connection.setAutoCommit(true);
        } catch (IllegalArgumentException ie) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
                System.out
                        .println("JDBC transaction rolled back successfully.");
            } catch (SQLException e) {
                System.out.println("PostgreSQL in rollback " + e.getMessage());
            }
            return "Borrow book faild: " + ie.getMessage();
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
                System.out
                        .println("JDBC transaction rolled back successfully.");
            } catch (SQLException se) {
                System.out.println("PostgreSQL in rollback " + se.getMessage());
            }

            if (e.getMessage().contains("cust_book_pkey")) {
                return "Borrow book faild:\n\tCustomer " + customerID
                        + " already has book " + isbn + " on loan.";
            }
            return "Borrow book faild: " + e.getMessage();
        }

        return "Borrow Book:\n" + "\tBook: " + isbn + " (" + title + ")\n"
                + "\tLoaned to: " + customerID + " (" + customerName + ")\n"
                + "\tDue Date: " + dueDate;
//	    return "Borrow Book Stub";
    }

    public String returnBook(int isbn, int customerid) {
        String customer = "SELECT * FROM cust_book WHERE isbn =" + isbn
                + " AND customerid = " + customerid + " FOR UPDATE";
        String book = "SELECT * FROM book WHERE isbn = " + isbn + " FOR UPDATE";
        String delete = "DELETE FROM cust_book WHERE isbn=" + isbn
                + " AND customerid =" + customerid;
        String update = "UPDATE book SET numLeft = numLeft + 1 WHERE isbn ="
                + isbn;
        try {
            connection.setAutoCommit(false);
            statemt = connection.createStatement();
            results = statemt.executeQuery(customer);
            if (!results.next()) {
                throw new IllegalArgumentException(
                        "No such record exist in Cust_book table: ISBN: "
                                + isbn + " CustomerId: " + customerid);
            }
            results = statemt.executeQuery(book);
            if (!results.next()) {
                throw new IllegalArgumentException("This book isbn: " + isbn
                        + " cannot be found in database");
            }

            statemt.executeUpdate(delete);
            optionPane.showMessageDialog(dialogParent,
                            "Locked the tuples(s), ready to update. Click OK to continue");
            statemt.executeUpdate(update);

            connection.commit();
            connection.setAutoCommit(true);
        } catch (IllegalArgumentException ie) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
                System.out.println("JDBC transaction rolled back successfully");
            } catch (SQLException se) {
                System.out.println("Progresql in rollback " + se.getMessage());
            }
            return "Return book failed: " + ie.getMessage();
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
                System.out.println("JDBC transaction rolled back successfully");
            } catch (SQLException se) {
                System.out.println("Progresql in rollback " + se.getMessage());
            }
            return "Return book failed: " + e.getMessage();
        }
        return "Customer: " + customerid + " returned book: " + isbn;
//        return "Return Book Stub";
    }

    public void closeDBConnection() {
        try {
            if (connection != null) {
                connection.close();
                System.out.println("Connection closed.");
            }
            if (statemt != null) {
                statemt.close();
                System.out.println("Statement closed.");
            }
            if (results != null) {
                results.close();
                System.out.println("Results closed.");
            }
        } catch (SQLException e) {
            System.out.println(e.getSQLState());
            System.out.println(e.getErrorCode());
        }
    }
    
    public String deleteCus(int customerID) {
        String customer = "SELECT * FROM customer WHERE customerID = "
                + customerID + " FOR UPDATE";
        String cb = "SELECT * FROM cust_book WHERE customerID = " + customerID
                + " FOR UPDATE";
        String delete_cus = "DELETE FROM customer WHERE customerID = "
                + customerID;

        try {
            connection.setAutoCommit(false);
            statemt = connection.createStatement();
            results = statemt.executeQuery(customer);
            if (!results.next())
                throw new IllegalArgumentException("Customer with id: "
                        + customerID + " does not exist.");
            statemt.executeQuery(cb);
            statemt.executeUpdate(delete_cus);

            connection.commit();
            connection.setAutoCommit(true);
        } catch (IllegalArgumentException ie) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
                System.out.println("JDBC transaction rolled back successfully");
            } catch (SQLException se) {
                System.out.println("Progresql in rollback " + se.getMessage());
            }
            return "Delete Customer failed: " + ie.getMessage();
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
                System.out.println("JDBC transaction rolled back successfully");
            } catch (SQLException se) {
                System.out.println("Progresql in rollback " + se.getMessage());
            }
            return "Delete Customer failed: " + e.getMessage();
        }
        return "Customer " + customerID + " deleted.";
//        return "Delete Customer";
    }
    
    public String deleteAuthor(int authorID) {
        String author = "SELECT * FROM author WHERE authorID = " + authorID
                + " FOR UPDATE";
        String ba = "SELECT * FROM book_author WHERE authorID = " + authorID
                + " FOR UPDATE";
        String delete = "DELETE FROM author WHERE authorID =" + authorID;
        String update = "UPDATE book_author SET authorID = 0 WHERE authorID = "
                + authorID;

        try {
            connection.setAutoCommit(false);
            statemt = connection.createStatement();
            results = statemt.executeQuery(author);
            if (!results.next()) {
                throw new IllegalArgumentException(
                        "No such record exist in Author Table: " + authorID);
            }
            statemt.executeUpdate(delete);
            results = statemt.executeQuery(ba);
            while (results.next()) {
                statemt.executeUpdate(update);
            }
            connection.commit();
            connection.setAutoCommit(true);
        } catch (IllegalArgumentException ie) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
                System.out.println("JDBC transaction rolled back successfully");
            } catch (SQLException se) {
                System.out.println("Progresql in rollback " + se.getMessage());
            }
            return "Delete Author failed: " + ie.getMessage();
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
                System.out.println("JDBC transaction rolled back successfully");
            } catch (SQLException se) {
                System.out.println("Progresql in rollback " + se.getMessage());
            }
            return "Delete Author failed: " + e.getMessage();
        }
        return "Author " + authorID + " deleted.";
//        return "Delete Author";
    }
    
    public String deleteBook(int isbn) {
        String book = "SELECT * FROM book WHERE isbn =" + isbn + " FOR UPDATE";
        String book_a = "SELECT * FROM book_author WHERE isbn = " + isbn
                + " FOR UPDATE";
        String cust_b = "SELECT * FROM cust_book WHERE isbn= " + isbn
                + " FOR UPDATE";
        String delete = "DELETE FROM book WHERE isbn=" + isbn;
        String update_ba = "UPDATE book_author SET isbn = 0 WHERE isbn = "
                + isbn;
        try {
            connection.setAutoCommit(false);
            statemt = connection.createStatement();
            results = statemt.executeQuery(book);
            if (!results.next()) {
                throw new IllegalArgumentException(
                        "No such record exist in Book table: ISBN = " + isbn);
            }
            statemt.executeQuery(book_a);
            statemt.executeQuery(cust_b);
            statemt.executeUpdate(delete);
            optionPane.showMessageDialog(dialogParent,
                            "Locked the tuples(s), ready to update. Click OK to continue");
            statemt.executeUpdate(update_ba);

            connection.commit();
            connection.setAutoCommit(true);
        } catch (IllegalArgumentException ie) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
                System.out.println("JDBC transaction rolled back successfully");
            } catch (SQLException se) {
                System.out.println("Progresql in rollback " + se.getMessage());
            }
            return "Delete book failed: " + ie.getMessage();
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
                System.out.println("JDBC transaction rolled back successfully");
            } catch (SQLException se) {
                System.out.println("Progresql in rollback " + se.getMessage());
            }
            return "Delete book failed: " + e.getMessage();
        }
        return "Delete Book ISBN = " + isbn + " successfully.";
//        return "Delete Book";
    }
}