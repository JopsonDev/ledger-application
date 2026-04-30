package com.pluralsight;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

public class FinancialTracker {

    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final ArrayList<Transaction> transactions = new ArrayList<>();

    //Checks to see if there's a transaction file, if not makes one before the main application runs
    static {
        try {
            File file = new File("transactions.csv");
            System.out.print("Checking for Transactions\n");
            loadingBar(50);
            if (!file.exists()) {
                file.createNewFile();
                System.out.println(GREEN + "New Transaction File created" + RESET);
            } else {
                System.out.println(GREEN + "Transactions found." + RESET);
            }
        } catch (Exception q) {
            System.out.print("Failed to write file");
        }
        System.out.println("====================================================");
    }

    private static final String FILE_NAME = "transactions.csv";

    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String TIME_PATTERN = "HH:mm:ss";
    private static final String DATETIME_PATTERN = DATE_PATTERN + " " + TIME_PATTERN;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern(DATE_PATTERN);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern(TIME_PATTERN);
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern(DATETIME_PATTERN);

    public static void main(String[] args) {
        mainMenuDisplay();
        loadTransactions(FILE_NAME, transactions);
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\nWelcome to TransactionApp");
            System.out.println("=========================");
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
    // Checks transactions file and adds any to transactions array
    public static void loadTransactions(String fileName, ArrayList<Transaction> transactions) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                LocalDate date = LocalDate.parse(parts[0], DATE_FMT);
                LocalTime time = LocalTime.parse(parts[1], TIME_FMT);
                String description = parts[2];
                String vendor = parts[3];
                double amount = Double.parseDouble(parts[4]);

                Transaction object = new Transaction(date, time, description, vendor, amount);
                transactions.add(object);
            }
            reader.close();
        } catch (Exception ignore) {
        }
    }

    //gathers info from user to build transactions and adds object
    private static Transaction getTransactionInfo(Scanner scanner) {
        LocalDate date;
        LocalTime time;

        while (true) {
            try {
                System.out.print("Date and Time (yyyy-MM-dd HH:mm:ss): ");
                String input = scanner.nextLine().trim();

                LocalDateTime dateTime = LocalDateTime.parse(input, DATETIME_FMT);
                date = dateTime.toLocalDate();
                time = dateTime.toLocalTime();

                break;
            } catch (Exception b) {
                System.out.println("Invalid Date input Use yyyy-MM-dd HH:mm:ss)");
            }
        }
        System.out.print("Description: ");
        String description = scanner.nextLine().trim();
        System.out.print("Vendor: ");
        String vendor = scanner.nextLine().trim();

        Double amount;
        while (true) {
            amount = parseDouble(scanner);

            if ((amount == null || amount < 0)) {
                System.out.println("Amount needs to be a positive number.");
            } else {
                break;
            }
        }
        Transaction object = new Transaction(date, time, description, vendor, amount);
        transactions.add(object);
        return object;
    }

    //add transaction as a deposit to the transaction file
    private static void addDeposit(Scanner scanner) {
        Transaction newDeposit = getTransactionInfo(scanner);
        addAmount(newDeposit);
    }

    //adjust amount to appear as a negative and adds to transaction file
    private static void addPayment(Scanner scanner) {
        Transaction newPayment = getTransactionInfo(scanner);
        newPayment.setAmount(newPayment.getAmount() * -1);
        addAmount(newPayment);
    }

    //method responsible for adding transactions to the file
    private static void addAmount(Transaction newAmount) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true));
            writer.write(newAmount + "\n");

            loadingBar(30);
            System.out.println(newAmount.getAmount() + " has been added to records");

            writer.close();
        } catch (Exception c) {
            System.out.println("failed to write");
        }
    }
    //Menu display for ledger
    private static void ledgerMenu(Scanner scanner) {
        boolean running = true;
        sorted();
        while (running) {
            System.out.println("\n         Ledger");
            System.out.println("=========================");
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
                case "R" -> reportsMenu(scanner);
                case "H" -> running = false;
                default -> System.out.println("Invalid option");
            }
        }
    }

    //Sets column lengths for how transactions will appear to user
    private static ColumnWidth columnWidths() {
        int dateLength = "Date".length();
        int timeLength = "Time".length();
        int descriptionLength = "Description".length();
        int vendorLength = "Vendor".length();

        for (Transaction t : transactions) {
            dateLength = Math.max(dateLength, t.getDate().toString().length());
            timeLength = Math.max(timeLength, t.getTime().toString().length());
            descriptionLength = Math.max(descriptionLength, t.getDescription().length());
            vendorLength = Math.max(vendorLength, t.getVendor().length());
        }
        int totalLength = dateLength + timeLength + descriptionLength + vendorLength + 9 + 8; // 9 = amount column width, 8 = spaces between columns
        return new ColumnWidth(dateLength, timeLength, descriptionLength, vendorLength, totalLength);
    }

    //handles displaying a print before reports print
    private static void printHeader(ColumnWidth width) {
        System.out.printf("%-" + width.date + "s %-" + width.time + "s %-" + width.description + "s %-" + width.vendor + "s $%10s%n", "Date", "Time", "Description", "Vendor", "Amount");
        System.out.println("=".repeat(width.total));
    }

    //responsible for the format of how the transactions get show to the user
    private static void printRow(Transaction t, ColumnWidth width) {
        if (t.getAmount() < 0) {
            System.out.printf("%-" + width.date + "s %-" + width.time + "s %-" + width.description + "s %-" + width.vendor + "s " + GREEN + "$" + RESET + RED + "%,10.2f%n" + RESET, t.getDate().format(DATE_FMT), t.getTime().format(TIME_FMT), t.getDescription(), t.getVendor(), t.getAmount());
        } else {
            System.out.printf("%-" + width.date + "s %-" + width.time + "s %-" + width.description + "s %-" + width.vendor + "s" + GREEN + " $%,10.2f%n" + RESET, t.getDate().format(DATE_FMT), t.getTime().format(TIME_FMT), t.getDescription(), t.getVendor(), t.getAmount());
        }
    }

    //sorts the transactions from newest to oldest
    private static void sorted() {
        transactions.sort((t1, t2) -> LocalDateTime.of(t2.getDate(), t2.getTime()).compareTo(LocalDateTime.of(t1.getDate(), t1.getTime())));
    }

    //shows all transactions
    private static void displayLedger(ColumnWidth width) {
        loadingBar(30);
        printHeader(width);

        for (Transaction t : transactions) {
            printRow(t, width);
        }
        System.out.println("\n");
    }

    //shows only deposits
    private static void displayDeposits(ColumnWidth width) {
        loadingBar(30);
        printHeader(width);
        for (Transaction t : transactions) {
            if (t.getAmount() > 0) {
                printRow(t, width);
            }
        }
        System.out.println("\n");
    }

    //shows only payments
    private static void displayPayments(ColumnWidth width) {
        loadingBar(30);
        printHeader(width);
        for (Transaction t : transactions) {
            if (t.getAmount() < 0) {
                printRow(t, width);
            }
        }
        System.out.println("\n");
    }

    //report menu with different options for user to choose from
    //has a switch case based on user response
    private static void reportsMenu(Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("         Reports");
            System.out.println("=========================");
            System.out.println("Choose an option:");
            System.out.println("1) Month To Date");
            System.out.println("2) Previous Month");
            System.out.println("3) Year To Date");
            System.out.println("4) Previous Year");
            System.out.println("5) Search by Vendor");
            System.out.println("6) Custom Search");
            System.out.println("7) Current Ledger Balance");
            System.out.println("0) Back");

            String input = scanner.nextLine().trim();
            LocalDate today = LocalDate.now();

            switch (input) {
                case "1" -> {
                    LocalDate startOfMonth = today.withDayOfMonth(1);

                    filterTransactionsByDate(startOfMonth, today, columnWidths());
                }
                case "2" -> {
                    LocalDate previousMonth = today.withDayOfMonth(1).minusMonths(1);
                    LocalDate endOfPreviousMonth = today.withDayOfMonth(1).minusDays(1);

                    filterTransactionsByDate(previousMonth, endOfPreviousMonth, columnWidths());
                }
                case "3" -> {
                    LocalDate yearStart = today.withDayOfYear(1);

                    filterTransactionsByDate(yearStart, today, columnWidths());
                }
                case "4" -> {
                    LocalDate yearStart = today.withDayOfYear(1).minusYears(1);
                    LocalDate yearEnd = yearStart.withDayOfYear(yearStart.lengthOfYear());

                    filterTransactionsByDate(yearStart, yearEnd, columnWidths());
                }
                case "5" -> {
                    System.out.print("Vendor Search: ");
                    String vendor = scanner.nextLine();

                    filterTransactionsByVendor(vendor, columnWidths());
                }
                case "6" -> customSearch(scanner, columnWidths());
                case "7" -> totaling(scanner);
                case "0" -> running = false;
                default -> System.out.println("Invalid option");
            }
        }
    }

    //checks if transaction dates are not before and not after the start and end date
    private static void filterTransactionsByDate(LocalDate start, LocalDate end, ColumnWidth width) {
        boolean hasSomething = false;
        loadingBar(30);
        printHeader(width);
        for (Transaction t : transactions) {
            LocalDate date = t.getDate();

            if (!date.isBefore(start) && !date.isAfter(end)) {
                printRow(t, width);
                hasSomething = true;
            }
        }
        if (!hasSomething) {
            System.out.println("No transactions found");
        }
        System.out.println("\n");
    }

    private static void filterTransactionsByVendor(String vendor, ColumnWidth width) {
        boolean hasSomething = false;
        loadingBar(30);
        printHeader(width);
        for (Transaction t : transactions) {
            if (t.getVendor().equalsIgnoreCase(vendor)) {
                printRow(t, width);
                hasSomething = true;
            }
        }
        if (!hasSomething) {
            System.out.println("No transactions with this vendor");
        }
        System.out.println("\n");
    }

    //allows the user to do a custom search with whatever paramiters they'd like or not like
    private static void customSearch(Scanner scanner, ColumnWidth width) {
        System.out.println("Please enter the information below, press enter to leave blank.");
        System.out.print("Start Date ");
        LocalDate startDate = parseDate(scanner);

        System.out.print("End Date ");
        LocalDate endDate = parseDate(scanner);


        System.out.print("Description: ");
        String description = scanner.nextLine().trim();

        System.out.print("Vendor: ");
        String vendor = scanner.nextLine().trim();

        Double amount = parseDouble(scanner);


        boolean found = false;
        loadingBar(30);
        printHeader(width);
        for (Transaction t : transactions) {
            LocalDate date = t.getDate();

            boolean matchesStartDate = (startDate == null || !date.isBefore(startDate));
            boolean matchesEndDate = (endDate == null || !date.isAfter(endDate));
            boolean matchesDescription = (description.isEmpty() || description.equalsIgnoreCase(t.getDescription()));
            boolean matchesVendor = (vendor.isEmpty() || vendor.equalsIgnoreCase(t.getVendor()));
            boolean matchesAmount = (amount == null || amount == t.getAmount());

            if (matchesStartDate && matchesEndDate && matchesDescription && matchesVendor && matchesAmount) {
                printRow(t, width);
                found = true;
            }
        }
        if (!found) {
            System.out.println("No transactions found");
        }
        System.out.println("\n");
    }

    //checks user input to convert a string into LocalDate or null and loops if user puts invalid input
    private static LocalDate parseDate(Scanner scanner) { // need to add fail message
        String dateString;
        LocalDate date = null;
        boolean isDone = false;
        while (!isDone) {
            System.out.print("Input Date Use yyyy-MM-dd: ");
            dateString = scanner.nextLine().trim();
            if (dateString.isEmpty()) {
                isDone = true;
            } else {
                try {
                    date = LocalDate.parse(dateString);
                } catch (Exception ignored) {
                }
                if (date != null) {
                    isDone = true;
                }
            }
        }
        return date;
    }

    // //checks user input to convert a string into double or null and loops if user puts invalid input
    private static Double parseDouble(Scanner scanner) {// need to add fail message
        String amountString;
        Double amount = null;
        boolean isDone = false;
        while (!isDone) {
            System.out.print("Exact Amount: ");
            amountString = scanner.nextLine().trim();
            if (amountString.isEmpty()) {
                isDone = true;
            } else {
                try {
                    amount = Double.parseDouble(amountString);
                } catch (Exception ignored) {
                }
                if (amount != null) {
                    isDone = true;
                }
            }
        }
        return amount;
    }

    //adds a cosmetic loading bar
    private static void loadingBar(int x) {
        int length = 20;
        for (int i = 0; i < length; i++) {
            String bar = "[" + "-".repeat(i) + GREEN + "LOADING" + RESET + "-".repeat(length - i - 1) + "]";
            System.out.print("\r" + bar);
            if (i == 19) {
                System.out.println("\r" + " ");
            }
            try {
                Thread.sleep(x);
            } catch (Exception Menu) {
                System.out.println("Critical Fail");
            }
        }
    }

    //totals values based on user selection
    private static void totaling(Scanner scanner) {
        boolean isDone = false;
        while(!isDone) {
            System.out.println(" Current Balance Reports");
            System.out.println("=========================");
            System.out.println("Choose an option:");
            System.out.println("P) Payment Total Dollar Amount");
            System.out.println("D) Deposit Total Dollar Amount");
            System.out.println("A) Current total");
            System.out.println("B) Go Back");
            String input = scanner.nextLine();
            double amount = 0;
            switch (input.toUpperCase()) {
                case "P" -> {
                    for (Transaction t : transactions) {
                        if (t.getAmount() < 0) {
                            amount += t.getAmount();
                        }
                    }
                }
                case "D" -> {
                    for (Transaction t : transactions) {
                        if (t.getAmount() > 0) {
                            amount += t.getAmount();
                        }
                    }
                }
                case "A" -> {
                    for (Transaction t : transactions) {
                        amount += t.getAmount();
                    }
                }
                case "B" -> isDone = true;
                default -> System.out.println("Invaild input");
            }
            System.out.printf("Current report balance: $%,.2f%n%n", amount);
        }
    }

    //adds a cosmetic logo screen
    private static void mainMenuDisplay(){
        System.out.println("""
                 ______  __                                    \s
                /\\__  _\\/\\ \\                                   \s
                \\/_/\\ \\/\\ \\ \\___      __                       \s
                   \\ \\ \\ \\ \\  _ `\\  /'__`\\                     \s
                    \\ \\ \\ \\ \\ \\ \\ \\/\\  __/                     \s
                     \\ \\_\\ \\ \\_\\ \\_\\ \\____\\                    \s
                      \\/_/  \\/_/\\/_/\\/____/  \s""");

        System.out.println("""
                 __                 __                         \s
                /\\ \\               /\\ \\                        \s
                \\ \\ \\         __   \\_\\ \\     __      __   _ __ \s
                 \\ \\ \\  __  /'__`\\ /'_` \\  /'_ `\\  /'__`\\/\\`'__\\
                  \\ \\ \\L\\ \\/\\  __//\\ \\L\\ \\/\\ \\L\\ \\/\\  __/\\ \\ \\/\s
                   \\ \\____/\\ \\____\\ \\___,_\\ \\____ \\ \\____\\\\ \\_\\\s
                    \\/___/  \\/____/\\/__,_ /\\/___L\\ \\/____/ \\/_/\s
                                             /\\____/           \s
                                             \\_/__/            \s""");
        System.out.println("====================================================");
        loadingBar(80);
        System.out.println("Welcome back!");


    }
}


 
