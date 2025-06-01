// FactorialCalculatorApp.java

import java.util.InputMismatchException;
import java.util.Scanner;
import java.sql.*;

public class FactorialCalculatorApp {

    // JDBC SQLite URL
    private static final String DB_URL = "jdbc:sqlite:factorial_records.db";

    public static void main(String[] args) {
        createDatabase();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\nFactorial Calculator Application");
            System.out.println("1. Calculate factorial (iterative)");
            System.out.println("2. Calculate factorial (recursive)");
            System.out.println("3. View history");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");

            try {
                int choice = scanner.nextInt();
                switch (choice) {
                    case 1:
                        handleFactorial(scanner, false);
                        break;
                    case 2:
                        handleFactorial(scanner, true);
                        break;
                    case 3:
                        viewHistory();
                        break;
                    case 4:
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Please enter a valid number.");
                scanner.next();
            }
        }
    }

    private static void handleFactorial(Scanner scanner, boolean recursive) {
        try {
            System.out.print("Enter a non-negative integer: ");
            int number = scanner.nextInt();

            if (number < 0) {
                System.out.println("Factorial is not defined for negative numbers.");
                return;
            }

            long result = recursive ? factorialRecursive(number) : factorialIterative(number);
            System.out.printf("Factorial of %d is %d (%s)\n", number, result, recursive ? "recursive" : "iterative");

            saveToDatabase(number, result, recursive ? "recursive" : "iterative");
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter an integer.");
            scanner.next();
        }
    }

    // Iterative factorial method
    public static long factorialIterative(int n) {
        long result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    // Recursive factorial method
    public static long factorialRecursive(int n) {
        if (n <= 1) return 1;
        return n * factorialRecursive(n - 1);
    }

    // Create database and table if not exist
    private static void createDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL); Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS factorials ("
                       + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                       + "input INTEGER,"
                       + "result INTEGER,"
                       + "method TEXT,"
                       + "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP"
                       + ");";
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // Save a record to the database
    private static void saveToDatabase(int input, long result, String method) {
        String sql = "INSERT INTO factorials(input, result, method) VALUES (?, ?, ?);";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, input);
            pstmt.setLong(2, result);
            pstmt.setString(3, method);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error saving to database: " + e.getMessage());
        }
    }

    // View saved factorial calculations
    private static void viewHistory() {
        String sql = "SELECT input, result, method, timestamp FROM factorials ORDER BY timestamp DESC;";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n--- Calculation History ---");
            while (rs.next()) {
                System.out.printf("%d! = %d [%s] at %s\n",
                    rs.getInt("input"),
                    rs.getLong("result"),
                    rs.getString("method"),
                    rs.getString("timestamp"));
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving history: " + e.getMessage());
        }
    }
}