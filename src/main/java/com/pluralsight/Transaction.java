package com.pluralsight;

import java.time.LocalDate;
import java.time.LocalTime;

public class Transaction {
    private LocalDate date;
    private LocalTime time;
    private String description;
    private String vendor;
    private double price;

    public Transaction(LocalDate date, LocalTime time, String description, String vendor, double price) {
        this.date = date;
        this.time = time;
        this.description = description;
        this.vendor = vendor;
        this.price = price;
    }

    public void setDate(LocalDate date) {this.date = date;}
    public void setDescription(String description) {this.description = description;}
    public void setPrice(double price) {this.price = price;}
    public void setTime(LocalTime time) {this.time = time;}
    public void setVendor(String vendor) {this.vendor = vendor;}

    public LocalDate getDate() {return date;}
    public String getDescription() {return description;}
    public double getPrice() {return price;}
    public LocalTime getTime() {return time;}
    public String getVendor() {return vendor;}

    @Override
    public String toString() {
        return "Transaction{" +
                "date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", description='" + description + '\'' +
                ", vendor='" + vendor + '\'' +
                ", price=" + price +
                '}';
    }
}
