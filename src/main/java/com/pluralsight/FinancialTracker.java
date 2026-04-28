package com.pluralsight;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

public class FinancialTracker {

    private static final ArrayList<Transaction> transactions = new ArrayList<>();
    private static final String FILE_NAME = "transactions.csv";

    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String TIME_PATTERN = "HH:mm:ss";
    private static final String DATETIME_PATTERN = DATE_PATTERN + " " + TIME_PATTERN;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern(DATE_PATTERN);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern(TIME_PATTERN);
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern(DATETIME_PATTERN);

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
        } catch (Exception a) {
            System.out.println("Error 1: couldn't load transactions.");
        }
    }

    private static Transaction getTransactionInfo(Scanner scanner){
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
            System.out.print("Amount: ");
            amount = scanner.nextDouble();
            scanner.nextLine();

            if (amount < 0) {
                System.out.println("Amount needs to be a positive number.");
            } else { break;
            }
        }
        Transaction object = new Transaction(date, time, description, vendor, amount);
        transactions.add(object);
        return object;
    }

    private static void addDeposit(Scanner scanner) {
        Transaction newDeposit = getTransactionInfo(scanner);
        addAmount(newDeposit);
    }

    private static void addPayment(Scanner scanner) {
        Transaction newPayment = getTransactionInfo(scanner);
        newPayment.setAmount(newPayment.getAmount() * -1);
        addAmount(newPayment);
    }
    private static void addAmount(Transaction newAmount){
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true));
            writer.write(newAmount + "\n");

            System.out.println(newAmount.getAmount() + " added");

            writer.close();
        }catch (Exception c) {
            System.out.println("failed to write");
        }
    }
    private static void ledgerMenu(Scanner scanner) {
        boolean running = true;
        sorted();
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
                case "R" -> reportsMenu(scanner);
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

    private static void printHeader(ColumnWidth width){
        System.out.printf("%-" + width.date + "s %-" + width.time + "s %-" + width.description + "s %-" + width.vendor + "s $%10s%n", "Date", "Time", "Description", "Vendor", "Amount");
        System.out.println("=".repeat(width.total));
    }

    private static void printRow(Transaction t, ColumnWidth width){
        System.out.printf("%-" + width.date + "s %-" + width.time + "s %-" + width.description + "s %-" + width.vendor + "s $%10.2f%n", t.getDate().format(DATE_FMT), t.getTime().format(TIME_FMT), t.getDescription(), t.getVendor(), t.getAmount());
    }

    private static void sorted(){
        transactions.sort((t1, t2) -> LocalDateTime.of(t2.getDate(), t2.getTime()).compareTo(LocalDateTime.of(t1.getDate(), t1.getTime())));
    }

    private static void displayLedger(ColumnWidth width) {
        printHeader(width);

        for (Transaction t : transactions) {
            printRow(t, width);
        }
        System.out.println("\n");
    }

    private static void displayDeposits(ColumnWidth width) {
        printHeader(width);
        for (Transaction t: transactions){
            if (t.getAmount() > 0){
                printRow(t, width);
            }
        }
        System.out.println("\n");
    }

    private static void displayPayments(ColumnWidth width){
        printHeader(width);
        for(Transaction t: transactions){
            if (t.getAmount() < 0) {
                printRow(t, width);
            }
        }
        System.out.println("\n");
    }

    private static void reportsMenu(Scanner scanner) {
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
                    LocalDate yearEnd = today.withDayOfYear(today.lengthOfYear());

                    filterTransactionsByDate(yearStart, yearEnd, columnWidths());
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
                case "6" -> customSearch(scanner,columnWidths());
                case "0" -> running = false;
                default -> System.out.println("Invalid option");
            }
        }
    }

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
    }


    private static void filterTransactionsByVendor(String vendor, ColumnWidth width) {
        boolean hasSomething = false;

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

    private static void customSearch(Scanner scanner, ColumnWidth width) {
        System.out.println("Please enter the information below");
        System.out.print("Start Date(yyyy-MM-dd): ");
        LocalDate startDate = parseDate(scanner.nextLine());

        System.out.print("End Date(yyyy-MM-dd): ");
        LocalDate endDate = parseDate(scanner.nextLine());

        System.out.print("Description: ");
        String description = scanner.nextLine();

        System.out.print("Vendor: ");
        String vendor = scanner.nextLine();

        System.out.print("Exact Amount: ");
        String amountString = scanner.nextLine();
        Double amount = parseDouble(amountString);

        boolean found = false;
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
        if(!found){
            System.out.println("No transactions found");
        }
    }

    private static LocalDate parseDate(String s) {
        try{
            return LocalDate.parse(s);
        }catch(Exception y) {
            return null;
        }
    }

    private static Double parseDouble(String s) {
       try{
           return Double.parseDouble(s);
       } catch (Exception z) {
           return null;
       }
    }
}
 
