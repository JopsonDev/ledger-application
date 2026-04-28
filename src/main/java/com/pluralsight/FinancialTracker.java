package com.pluralsight;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

/*
 * Capstone skeleton – personal finance tracker.
 * ------------------------------------------------
 * File format  (pipe-delimited)
 *     yyyy-MM-dd|HH:mm:ss|description|vendor|amount
 * A deposit has a positive amount; a payment is stored
 * as a negative amount.
 */
public class FinancialTracker {

    /* ------------------------------------------------------------------
       Shared data and formatters
       ------------------------------------------------------------------ */
    private static final ArrayList<Transaction> transactions = new ArrayList<>();
    private static final String FILE_NAME = "transactions.csv";

    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String TIME_PATTERN = "HH:mm:ss";
    private static final String DATETIME_PATTERN = DATE_PATTERN + " " + TIME_PATTERN;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern(DATE_PATTERN);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern(TIME_PATTERN);
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern(DATETIME_PATTERN);

    /* ------------------------------------------------------------------
       Main menu
       ------------------------------------------------------------------ */
    public static void main(String[] args) {
        loadTransactions(FILE_NAME, transactions);

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("Welcome to TransactionApp");
            System.out.println("Choose an option:");
            System.out.println("D) Add Deposit");
            System.out.println("P) Make Payment (Debit)");
            System.out.println("L) Ledger");
            System.out.println("X) Exit");

            String input = scanner.nextLine().trim();

            switch (input.toUpperCase()) {
                case "D" -> addDeposit(scanner);
                case "P" -> addPayment(scanner);
                case "L" -> ledgerMenu(scanner);
                case "X" -> running = false;
                default -> System.out.println("Invalid option");
            }
        }
        scanner.close();
    }

    /* ------------------------------------------------------------------
       File I/O
       ------------------------------------------------------------------ */

    /**
     * Load transactions from FILE_NAME.
     * • If the file doesn’t exist, create an empty one so that future writes succeed.
     * • Each line looks like: date|time|description|vendor|amount
     */
    public static void loadTransactions(String fileName, ArrayList<Transaction> transactions) {
        // TODO: create file if it does not exist, then read each line,
        //       parse the five fields, build a Transaction object,
        //       and add it to the transactions list.
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                LocalDate date = LocalDate.parse(parts[0]);
                LocalTime time = LocalTime.parse(parts[1]);
                String description = parts[2];
                String vendor = parts[3];
                double amount = Double.parseDouble(parts[4]);

                Transaction object = new Transaction(date, time, description, vendor, amount);
                transactions.add(object);
            }
        } catch (Exception a) {
            System.out.println("Error 1: couldn't load transactions.");
        }
    }

    /* ------------------------------------------------------------------
       Add new transactions
       ------------------------------------------------------------------ */

    /**
     * Prompt for ONE date+time string in the format
     * "yyyy-MM-dd HH:mm:ss", plus description, vendor, amount.
     * Validate that the amount entered is positive.
     * Store the amount as-is (positive) and append to the file.
     */
    private static void addDeposit(Scanner scanner) {
        // TODO
        LocalDate date;
        LocalTime time;
        while (true) {
            try {
                System.out.print("Date and Time (yyyy-MM-dd HH:mm:ss): ");
                String input = scanner.nextLine();
                LocalDateTime dateTime = LocalDateTime.parse(input, DATETIME_FMT);
                date = dateTime.toLocalDate();
                time = dateTime.toLocalTime();
                break;
            } catch (Exception b) {
                System.out.println("Invalid Date input Use yyyy-MM-dd HH:mm:ss)");
            }
        }
        System.out.print("Description: ");
        String description = scanner.nextLine();
        System.out.print("Vendor: ");
        String vendor = scanner.nextLine();
        double amount;
        while (true) {
            System.out.println("Deposit: ");
            amount = scanner.nextDouble();
            scanner.nextLine();
            if (amount > 0) { break;
            } else {
                System.out.println("Deposit needs to be a positive number.");
            }
        }
        Transaction newDeposit = new Transaction(date, time, description, vendor, amount);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("Test.csv", true));
            writer.write(newDeposit + "\n");
            writer.close();
        }catch (Exception c){
            System.out.println("failed to write");
        }
    }

    private static void addPayment(Scanner scanner) {
        LocalDate date;
        LocalTime time;
        while (true) {
            try {
                System.out.print("Date and Time (yyyy-MM-dd HH:mm:ss): ");
                String input = scanner.nextLine();
                LocalDateTime dateTime = LocalDateTime.parse(input, DATETIME_FMT);
                date = dateTime.toLocalDate();
                time = dateTime.toLocalTime();
                break;
            } catch (Exception b) {
                System.out.println("Invalid Date input Use yyyy-MM-dd HH:mm:ss)");
            }
        }
        System.out.print("Description: ");
        String description = scanner.nextLine();
        System.out.print("Vendor: ");
        String vendor = scanner.nextLine();
        double amount;
        while (true) {
            System.out.println("Payment: ");
            amount = scanner.nextDouble();
            scanner.nextLine();
            amount = amount * -1;
            if (amount >= 0) { System.out.println("Please enter as a Positive.");
            } else { break;
            }
        }
        Transaction newPayment = new Transaction(date, time, description, vendor, amount);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("Test.csv", true));
            writer.write(newPayment + "\n");
            writer.close();
        }catch (Exception c){
            System.out.println("failed to write");
        }
    }



    private static void ledgerMenu(Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("Ledger");
            System.out.println("Choose an option:");
            System.out.println("A) All");
            System.out.println("D) Deposits");
            System.out.println("P) Payments");
            System.out.println("R) Reports");
            System.out.println("H) Home");

            String input = scanner.nextLine().trim();

            switch (input.toUpperCase()) {
                case "A" -> displayLedger(columnWidths());
                case "D" -> displayDeposits(columnWidths());
                case "P" -> displayPayments(columnWidths());
                case "R" -> reportsMenu(scanner, columnWidths());
                case "H" -> running = false;
                default -> System.out.println("Invalid option");
            }
        }
    }

    private static ColumnWidth columnWidths(){
        int dateLength = "Date".length();
        int timeLength = "Time".length();
        int descriptionLength = "Description".length();
        int vendorLength = "Vendor".length();

        for (Transaction t : transactions){
            dateLength = Math.max(dateLength, t.getDate().toString().length());
            timeLength = Math.max(timeLength, t.getTime().toString().length());
            descriptionLength = Math.max(descriptionLength, t.getDescription().length());
            vendorLength = Math.max(vendorLength, t.getVendor().length());
        }
        int totalLength = dateLength + timeLength + descriptionLength + vendorLength + 11 + 8; // 11 = amount column width, 8 = spaces between columns
        return new ColumnWidth(dateLength,timeLength,descriptionLength,vendorLength,totalLength);
    }
    private static void columnSetUp(ColumnWidth width){
        System.out.printf("%-" + width.date + "s %-" + width.time + "s %-" + width.description + "s %-" + width.vendor + "s $%10s%n", "Date", "Time", "Description", "Vendor", "Amount");
        System.out.println("=".repeat(width.total));
    }
    private static void printRow(Transaction t, ColumnWidth width){
        System.out.printf("%-" + width.date + "s %-" + width.time + "s %-" + width.description + "s %-" + width.vendor + "s $%10.2f%n", t.getDate(), t.getTime(), t.getDescription(), t.getVendor(), t.getAmount());
    }

    private static void displayLedger(ColumnWidth width) {
        columnSetUp(width);
        for (Transaction t : transactions) {
            printRow(t, width);
        }
        System.out.println("\n");
    }

    private static void displayDeposits(ColumnWidth width) {
        columnSetUp(width);
        for (Transaction t: transactions){
            if (t.getAmount() > 0){
                printRow(t, width);
            }
        }
        System.out.println("\n");
    }

    private static void displayPayments(ColumnWidth width){
        columnSetUp(width);
        for(Transaction t: transactions){
            if (t.getAmount() < 0) {
                printRow(t, width);
            }
        }
        System.out.println("\n");
    }

    /* ------------------------------------------------------------------
       Reports menu
       ------------------------------------------------------------------ */
    private static void reportsMenu(Scanner scanner, ColumnWidth width) {
        boolean running = true;
        while (running) {
            System.out.println("Reports");
            System.out.println("Choose an option:");
            System.out.println("1) Month To Date");
            System.out.println("2) Previous Month");
            System.out.println("3) Year To Date");
            System.out.println("4) Previous Year");
            System.out.println("5) Search by Vendor");
            System.out.println("6) Custom Search");
            System.out.println("0) Back");

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> {/* TODO – month-to-date report */
                    LocalDate today = LocalDate.now();
                    LocalDate startOfMonth = today.withDayOfMonth(1);
                    filterTransactionsByDate(startOfMonth, today, columnWidths());
                }
                case "2" -> {
                    LocalDate today = LocalDate.now();
                    LocalDate previousMonth = today.withDayOfMonth(1).minusMonths(1);
                    LocalDate endOfPerviousMonth = today.withDayOfMonth(1).minusDays(1);
                    filterTransactionsByDate(previousMonth, endOfPerviousMonth, columnWidths());
                }
                case "3" -> {
                    LocalDate today = LocalDate.now();
                    LocalDate yearStart = today.withDayOfYear(1);
                    LocalDate yearEnd = today.withDayOfYear(today.lengthOfYear());
                    filterTransactionsByDate(yearStart, yearEnd, columnWidths());
                }
                case "4" -> {
                    LocalDate today = LocalDate.now();
                    LocalDate yearStart = today.withDayOfYear(1).minusYears(1);
                    LocalDate yearEnd = yearStart.withDayOfYear(yearStart.lengthOfYear());
                    filterTransactionsByDate(yearStart, yearEnd, columnWidths());
                }
                case "5" -> {
                    boolean hasSomething = false;
                    System.out.print("Vendor Search: ");
                    String vendor = scanner.nextLine();
                    columnSetUp(width);
                    for(Transaction t: transactions){
                        if(t.getVendor().equalsIgnoreCase(vendor)){
                            printRow(t, width);
                            hasSomething = true;
                        }
                    }
                    if (!hasSomething){
                        System.out.println("No transactions with this vendor");
                    }
                }
                case "6" -> customSearch(scanner);
                case "0" -> running = false;
                default -> System.out.println("Invalid option");
            }
        }
    }

    /* ------------------------------------------------------------------
       Reporting helpers
       ------------------------------------------------------------------ */
    private static void filterTransactionsByDate(LocalDate start, LocalDate end, ColumnWidth width) {
        boolean hasSomething = false;
        for (Transaction t: transactions) {
            LocalDate date = t.getDate();

            if (!date.isBefore(start) && !date.isAfter(end)) {
                printRow(t, width);
                hasSomething = true;
            }
        }
        if (!hasSomething){
            System.out.println("No transactions found");
        }

        // TODO – iterate transactions, print those within the range
    } //got do this and vendor

    private static void filterTransactionsByVendor(String vendor) {
        // TODO – iterate transactions, print those with matching vendor
    }

    private static void customSearch(Scanner scanner) {
        // TODO – prompt for any combination of date range, description,
        //        vendor, and exact amount, then display matches
    }

    /* ------------------------------------------------------------------
       Utility parsers (you can reuse in many places)
       ------------------------------------------------------------------ */
    private static LocalDate parseDate(String s) {
        /* TODO – return LocalDate or null */
        return null;
    }

    private static Double parseDouble(String s) {
        /* TODO – return Double   or null */
        return null;
    }
}
 
